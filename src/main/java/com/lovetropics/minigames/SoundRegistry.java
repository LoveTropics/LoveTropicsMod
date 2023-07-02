package com.lovetropics.minigames;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SoundRegistry {
	public static final DeferredRegister<SoundEvent> REGISTER = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Constants.MODID);

	public static final RegistryObject<SoundEvent> SWAP_PLAYERS = register("swap_players");
	public static final RegistryObject<SoundEvent> PACKAGE_RECEIVE = register("package_receive");
	public static final RegistryObject<SoundEvent> SABOTAGE_RECEIVE = register("sabotage_receive");

	private static RegistryObject<SoundEvent> register(String name) {
		return REGISTER.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Constants.MODID, name)));
	}
}
