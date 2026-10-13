package com.pnjterror.registry;

import com.pnjterror.PnjTerror;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, PnjTerror.MODID);

    public static final RegistryObject<Item> CLOSET_ITEM = ITEMS.register("hiding_closet",
            () -> new BlockItem(ModBlocks.CLOSET.get(), new Item.Properties()));

    public static final RegistryObject<Item> WEEPER_EGG = ITEMS.register("weeper_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.WEEPER, 0xC8C8CD, 0x101015, new Item.Properties()));
}
