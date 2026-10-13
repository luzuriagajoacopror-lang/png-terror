package com.pnjterror.logic;

import com.pnjterror.entity.WeeperEntity;
import com.pnjterror.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

/** Hace aparecer al Llorón cerca de los jugadores, de noche o en lugares oscuros. */
public final class HorrorDirector {
    private HorrorDirector() {
    }

    public static void trySpawn(ServerPlayer p) {
        ServerLevel level = p.serverLevel();
        if (level.dimension() != Level.OVERWORLD || level.getDifficulty() == Difficulty.PEACEFUL) return;
        if (p.isCreative() || p.isSpectator() || HidingHandler.isHiding(p)) return;
        if (level.random.nextFloat() > 0.3F) return;

        boolean dark = level.isNight() || level.getMaxLocalRawBrightness(p.blockPosition()) <= 6;
        if (!dark) return;
        if (!level.getEntitiesOfClass(WeeperEntity.class, p.getBoundingBox().inflate(96.0D)).isEmpty()) return;

        double ang = level.random.nextDouble() * Math.PI * 2.0D;
        double dist = 28.0D + level.random.nextDouble() * 8.0D;
        int x = Mth.floor(p.getX() + Math.cos(ang) * dist);
        int z = Mth.floor(p.getZ() + Math.sin(ang) * dist);
        if (!level.hasChunkAt(new BlockPos(x, p.getBlockY(), z))) return;

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        WeeperEntity w = ModEntities.WEEPER.get().create(level);
        if (w == null) return;
        w.moveTo(x + 0.5D, y, z + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(w);
    }
}
