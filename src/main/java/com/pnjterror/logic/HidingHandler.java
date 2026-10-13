package com.pnjterror.logic;

import com.pnjterror.PnjTerror;
import com.pnjterror.block.HidingClosetBlock;
import com.pnjterror.entity.WeeperEntity;
import com.pnjterror.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Lógica de esconderse en el armario. */
@Mod.EventBusSubscriber(modid = PnjTerror.MODID)
public class HidingHandler {
    private static final String HIDING = "pnj_hiding";
    private static final String POS = "pnj_hide_pos";
    private static final String FACE = "pnj_hide_face";
    private static final String TICKS = "pnj_hide_ticks";

    public static boolean isHiding(Player p) {
        return p.getPersistentData().getBoolean(HIDING);
    }

    public static void startHiding(ServerPlayer p, BlockPos pos, Direction facing) {
        CompoundTag t = p.getPersistentData();
        t.putBoolean(HIDING, true);
        t.putLong(POS, pos.asLong());
        t.putInt(FACE, facing.get2DDataValue());
        t.putInt(TICKS, 0);

        p.teleportTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        p.setDeltaMovement(Vec3.ZERO);
        applyEffects(p);

        ServerLevel level = p.serverLevel();
        level.playSound(null, pos, ModSounds.CLOSET_OPEN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        p.displayClientMessage(Component.translatable("message.pnjterror.hiding"), true);

        // ¿Algún Llorón te vio entrar?
        for (WeeperEntity w : level.getEntitiesOfClass(WeeperEntity.class, p.getBoundingBox().inflate(32.0D))) {
            boolean seen = w.hasLineOfSight(p) && w.distanceTo(p) < 18.0F;
            w.onPlayerHid(p, pos, seen);
        }
    }

    public static void stopHiding(ServerPlayer p, boolean forced) {
        if (!isHiding(p)) return;
        CompoundTag t = p.getPersistentData();
        BlockPos pos = BlockPos.of(t.getLong(POS));
        Direction face = Direction.from2DDataValue(t.getInt(FACE));
        clear(p);

        BlockPos out = pos.relative(face);
        p.teleportTo(out.getX() + 0.5D, out.getY(), out.getZ() + 0.5D);
        p.serverLevel().playSound(null, pos, ModSounds.CLOSET_CLOSE.get(), SoundSource.BLOCKS, 1.0F, forced ? 0.7F : 1.0F);
    }

    private static void clear(Player p) {
        CompoundTag t = p.getPersistentData();
        t.remove(HIDING);
        t.remove(POS);
        t.remove(FACE);
        t.remove(TICKS);
        p.removeEffect(MobEffects.INVISIBILITY);
        p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        p.removeEffect(MobEffects.JUMP);
    }

    private static void applyEffects(ServerPlayer p) {
        p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0, false, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9, false, false, false));
        p.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 128, false, false, false));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer p)) return;

        if (isHiding(p)) {
            CompoundTag t = p.getPersistentData();
            int ticks = t.getInt(TICKS) + 1;
            t.putInt(TICKS, ticks);
            BlockPos pos = BlockPos.of(t.getLong(POS));

            if (!(p.level().getBlockState(pos).getBlock() instanceof HidingClosetBlock)) {
                stopHiding(p, true);
                return;
            }
            double cx = pos.getX() + 0.5D;
            double cz = pos.getZ() + 0.5D;
            if (p.distanceToSqr(cx, pos.getY(), cz) > 0.16D) {
                p.teleportTo(cx, pos.getY(), cz);
            }
            p.setDeltaMovement(Vec3.ZERO);
            if (ticks % 20 == 0) applyEffects(p);
            if (ticks > 15 && p.isShiftKeyDown()) {
                stopHiding(p, false);
            }
        }

        if (p.tickCount % 600 == 0) {
            HorrorDirector.trySpawn(p);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer p && isHiding(p)) {
            clear(p);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        clear(event.getEntity());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player p) {
            clear(p);
        }
    }
}
