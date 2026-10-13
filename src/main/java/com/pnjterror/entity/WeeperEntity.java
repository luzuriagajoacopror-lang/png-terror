package com.pnjterror.entity;

import com.pnjterror.logic.HidingHandler;
import com.pnjterror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * El Llorón. Tres fases:
 * STALK  - te sigue de lejos y se congela si lo mirás (estilo stalker). Se te acerca de a poco.
 * HUNT   - te caza a toda velocidad. Hay que esconderse en un armario.
 * SEARCH - te perdió. Si vio dónde te escondiste, golpea el armario y te saca a la fuerza.
 */
public class WeeperEntity extends Monster {
    public static final int STALK = 0;
    public static final int HUNT = 1;
    public static final int SEARCH = 2;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(WeeperEntity.class, EntityDataSerializers.INT);

    private int stateTicks;
    private int watchedTicks;
    private int huntTimer;
    private int soundTimer;
    private int attackTimer;
    private int knockTicks;
    private int noTargetTicks;
    private BlockPos hideSpot;
    private boolean knowsHide;

    public WeeperEntity(EntityType<? extends WeeperEntity> type, Level level) {
        super(type, level);
        this.xpReward = 20;
        this.huntTimer = 1200 + this.random.nextInt(1200);
        this.soundTimer = 60;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_STATE, STALK);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    private void setState(int state) {
        this.entityData.set(DATA_STATE, state);
        this.stateTicks = 0;
    }

    // ------------------------------------------------------------------ IA

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        stateTicks++;
        if (attackTimer > 0) attackTimer--;

        Player target = this.level().getNearestPlayer(getX(), getY(), getZ(), 64.0D, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        if (target == null) {
            getNavigation().stop();
            if (++noTargetTicks > 200) discard();
            return;
        }
        noTargetTicks = 0;

        double dist = distanceTo(target);
        boolean hiding = HidingHandler.isHiding(target);

        switch (getState()) {
            case HUNT -> tickHunt(target, dist, hiding);
            case SEARCH -> tickSearch(target, dist, hiding);
            default -> tickStalk(target, dist, hiding);
        }

        if (target instanceof ServerPlayer sp) {
            tickAudio(sp, dist);
        }
    }

    private void tickStalk(Player target, double dist, boolean hiding) {
        getLookControl().setLookAt(target, 40.0F, 40.0F);
        huntTimer--;
        if (hiding) {
            getNavigation().stop();
            return;
        }

        boolean watched = isWatchedBy(target);
        if (watched) {
            // congelado mientras lo mirás
            getNavigation().stop();
            setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
            watchedTicks++;
            if (dist < 8.0D && watchedTicks > 30) {
                startHunt();
                return;
            }
            if (watchedTicks > 80) {
                vanish(target);
                watchedTicks = 0;
            }
        } else {
            watchedTicks = Math.max(0, watchedTicks - 2);
            double want = Math.max(5.0D, 20.0D - stateTicks / 80.0D);
            if (dist > want) {
                getNavigation().moveTo(target, 0.7D);
            } else {
                getNavigation().stop();
            }
        }

        if ((dist < 6.5D && !watched) || huntTimer <= 0) {
            startHunt();
        }
    }

    private void tickHunt(Player target, double dist, boolean hiding) {
        getLookControl().setLookAt(target, 60.0F, 60.0F);
        if (hiding) {
            setState(SEARCH);
            knockTicks = 0;
            return;
        }
        if (stateTicks > 700) {
            backToStalk(target);
            return;
        }
        getNavigation().moveTo(target, 1.15D);
        if (dist < 2.3D && attackTimer <= 0) {
            swing(InteractionHand.MAIN_HAND);
            doHurtTarget(target);
            attackTimer = 25;
        }
    }

    private void tickSearch(Player target, double dist, boolean hiding) {
        if (!hiding) {
            // saliste del armario
            if (dist < 24.0D) {
                startHunt();
            } else {
                backToStalk(target);
            }
            return;
        }

        if (knowsHide && hideSpot != null) {
            double cx = hideSpot.getX() + 0.5D;
            double cz = hideSpot.getZ() + 0.5D;
            double d = distanceToSqr(cx, hideSpot.getY(), cz);
            if (d > 6.25D) {
                getNavigation().moveTo(cx, hideSpot.getY(), cz, 1.0D);
            } else {
                getNavigation().stop();
                getLookControl().setLookAt(cx, hideSpot.getY() + 0.5D, cz);
                knockTicks++;
                if (knockTicks % 30 == 1) {
                    level().playSound(null, hideSpot, ModSounds.KNOCK.get(), SoundSource.HOSTILE, 1.6F, 1.0F);
                }
                if (knockTicks >= 120 && target instanceof ServerPlayer sp) {
                    HidingHandler.stopHiding(sp, true);
                    target.hurt(damageSources().mobAttack(this), 8.0F);
                    startHunt();
                }
            }
        } else {
            // no vio dónde te metiste: da vueltas buscándote
            if (stateTicks % 50 == 1) {
                Vec3 v = DefaultRandomPos.getPos(this, 8, 3);
                if (v != null) getNavigation().moveTo(v.x, v.y, v.z, 0.8D);
            }
            if (stateTicks % 70 == 0) {
                level().playSound(null, blockPosition(), ModSounds.WHISPER.get(), SoundSource.HOSTILE, 1.0F, 0.8F);
            }
            if (stateTicks > 200) {
                backToStalk(target);
            }
        }
    }

    private void tickAudio(ServerPlayer sp, double dist) {
        int s = getState();
        if (s == HUNT && tickCount % 18 == 0) {
            sp.playNotifySound(ModSounds.HEARTBEAT.get(), SoundSource.HOSTILE, 1.0F, 1.0F);
        } else if (s == STALK && dist < 32.0D && tickCount % 200 == 0) {
            sp.playNotifySound(ModSounds.DRONE.get(), SoundSource.AMBIENT, 0.7F, 1.0F);
        }
        if (s == STALK && --soundTimer <= 0) {
            soundTimer = 100 + random.nextInt(200);
            SoundEvent snd = random.nextBoolean() ? ModSounds.WHISPER.get() : ModSounds.CRY.get();
            level().playSound(null, blockPosition(), snd, SoundSource.HOSTILE, 1.5F, 0.9F + random.nextFloat() * 0.2F);
        }
    }

    // ------------------------------------------------------------------ helpers

    private void startHunt() {
        setState(HUNT);
        knowsHide = false;
        hideSpot = null;
        watchedTicks = 0;
        attackTimer = 20;
        huntTimer = 1200 + random.nextInt(1200);
        level().playSound(null, blockPosition(), ModSounds.SCREAM.get(), SoundSource.HOSTILE, 2.0F, 0.9F + random.nextFloat() * 0.2F);
    }

    private void backToStalk(Player target) {
        setState(STALK);
        knowsHide = false;
        hideSpot = null;
        huntTimer = 1800 + random.nextInt(2400);
        vanish(target);
    }

    /** Llamado por el armario cuando un jugador se esconde cerca. */
    public void onPlayerHid(Player player, BlockPos closet, boolean seen) {
        if (getState() == HUNT || getState() == SEARCH) {
            if (seen) {
                knowsHide = true;
                hideSpot = closet;
                knockTicks = 0;
            }
        }
    }

    private boolean isWatchedBy(Player p) {
        Vec3 look = p.getViewVector(1.0F).normalize();
        Vec3 to = new Vec3(getX() - p.getX(), getEyeY() - p.getEyeY(), getZ() - p.getZ());
        double len = to.length();
        if (len < 0.01D) return true;
        return look.dot(to.scale(1.0D / len)) > 0.93D && p.hasLineOfSight(this);
    }

    /** Desaparece y reaparece lejos, detrás del jugador. */
    private void vanish(Player target) {
        Vec3 look = target.getViewVector(1.0F);
        double ang = Math.atan2(-look.z, -look.x) + (random.nextDouble() - 0.5D) * 1.6D;
        for (int i = 0; i < 8; i++) {
            double dd = 20.0D + random.nextDouble() * 8.0D;
            int x = Mth.floor(target.getX() + Math.cos(ang) * dd);
            int z = Mth.floor(target.getZ() + Math.sin(ang) * dd);
            if (!level().hasChunkAt(new BlockPos(x, 64, z))) {
                ang += 0.6D;
                continue;
            }
            int y = level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            if (randomTeleport(x + 0.5D, y, z + 0.5D, false)) {
                getNavigation().stop();
                return;
            }
            ang += 0.6D;
        }
    }

    // ------------------------------------------------------------------ eventos / sonidos

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean result = super.hurt(source, amount);
        if (result && !level().isClientSide && source.getEntity() instanceof Player p
                && getState() == STALK && !p.isCreative()) {
            startHunt();
        }
        return result;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(ModSounds.STEP.get(), 0.7F, 0.9F + random.nextFloat() * 0.2F);
    }
}
