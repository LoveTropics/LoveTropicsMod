package com.lovetropics.minigames.common.core.chat;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class ChatChannelStore implements ICapabilityProvider {
	private final LazyOptional<ChatChannelStore> instance = LazyOptional.of(() -> this);

	private ChatChannel channel = ChatChannel.GLOBAL;

	@SubscribeEvent
	public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof ServerPlayer) {
			event.addCapability(new ResourceLocation(Constants.MODID, "chat_channel"), new ChatChannelStore());
		}
	}

	public static void set(ServerPlayer player, ChatChannel channel) {
		player.getCapability(LoveTropics.CHAT_CHANNEL).ifPresent(store -> store.channel = channel);
	}

	public static ChatChannel get(ServerPlayer player) {
		return player.getCapability(LoveTropics.CHAT_CHANNEL).map(store -> store.channel).orElse(ChatChannel.GLOBAL);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
		return LoveTropics.CHAT_CHANNEL.orEmpty(cap, instance);
	}
}
