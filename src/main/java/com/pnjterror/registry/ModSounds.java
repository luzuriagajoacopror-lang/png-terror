package com.pnjterror.registry;

import com.pnjterror.PnjTerror;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PnjTerror.MODID);

    public static final RegistryObject<SoundEvent> CRY = reg("entity.weeper.cry");
    public static final RegistryObject<SoundEvent> WHISPER = reg("entity.weeper.whisper");
    public static final RegistryObject<SoundEvent> SCREAM = reg("entity.weeper.scream");
    public static final RegistryObject<SoundEvent> HURT = reg("entity.weeper.hurt");
    public static final RegistryObject<SoundEvent> DEATH = reg("entity.weeper.death");
    public static final RegistryObject<SoundEvent> STEP = reg("entity.weeper.step");
    public static final RegistryObject<SoundEvent> CLOSET_OPEN = reg("block.closet.open");
    public static final RegistryObject<SoundEvent> CLOSET_CLOSE = reg("block.closet.close");
    public static final RegistryObject<SoundEvent> KNOCK = reg("block.closet.knock");
    public static final RegistryObject<SoundEvent> HEARTBEAT = reg("ambient.heartbeat");
    public static final RegistryObject<SoundEvent> DRONE = reg("ambient.drone");

    private static RegistryObject<SoundEvent> reg(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(PnjTerror.MODID, name)));
    }
}
