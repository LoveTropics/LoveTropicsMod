package com.lovetropics.minigames.common.core.map.workspace;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.workspace.SetWorkspaceMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class MapWorkspaceTracker {
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		PlayerEntity player = event.getPlayer();
		if (player instanceof ServerPlayerEntity) {
			trySendWorkspace((ServerPlayerEntity) player, player.world.getDimensionKey());
		}
	}

	@SubscribeEvent
	public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		PlayerEntity player = event.getPlayer();
		RegistryKey<World> dimension = event.getTo();
		trySendWorkspace((ServerPlayerEntity) player, dimension);
	}

	private static void trySendWorkspace(ServerPlayerEntity player, RegistryKey<World> dimension) {
		if (dimension == null) return;

		MinecraftServer server = player.world.getServer();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(server);

		MapWorkspace workspace = workspaceManager.getWorkspace(dimension);

		if (workspace != null) {
			SetWorkspaceMessage message = new SetWorkspaceMessage(workspace.getRegions());
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
		}
	}
}
