package com.lovetropics.minigames;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(Constants.MODID)
@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoundRegistry {

    @SubscribeEvent
    public static void register(RegistryEvent.Register<SoundEvent> evt){
        ResourceLocation swapPlayers = new ResourceLocation(Constants.MODID, "swap_players");
        evt.getRegistry().register(new SoundEvent(swapPlayers).setRegistryName(swapPlayers));

        ResourceLocation packageReceive = new ResourceLocation(Constants.MODID, "package_receive");
        evt.getRegistry().register(new SoundEvent(packageReceive).setRegistryName(packageReceive));

        ResourceLocation sabotageReceive = new ResourceLocation(Constants.MODID, "sabotage_receive");
        evt.getRegistry().register(new SoundEvent(sabotageReceive).setRegistryName(sabotageReceive));
    }
}
