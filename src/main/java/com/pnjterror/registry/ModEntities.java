package com.pnjterror.registry;

import com.pnjterror.PnjTerror;
import com.pnjterror.entity.WeeperEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PnjTerror.MODID);

    public static final RegistryObject<EntityType<WeeperEntity>> WEEPER = ENTITIES.register("weeper",
            () -> EntityType.Builder.of(WeeperEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.95F)
                    .clientTrackingRange(10)
                    .build(new ResourceLocation(PnjTerror.MODID, "weeper").toString()));
}
