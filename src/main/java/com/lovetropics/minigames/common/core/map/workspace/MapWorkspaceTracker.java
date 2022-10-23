package com.lovetropics.minigames.common.core.map.workspace;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.workspace.SetWorkspaceMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class MapWorkspaceTracker {
	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		Player player = event.getPlayer();
		if (player instanceof ServerPlayer) {
			trySendWorkspace((ServerPlayer) player, player.level.dimension());
		}
	}

	@SubscribeEvent
	public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		Player player = event.getPlayer();
		ResourceKey<Level> dimension = event.getTo();
		trySendWorkspace((ServerPlayer) player, dimension);
	}

	private static void trySendWorkspace(ServerPlayer player, ResourceKey<Level> dimension) {
		if (dimension == null) return;

		MinecraftServer server = player.level.getServer();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(server);

		MapWorkspace workspace = workspaceManager.getWorkspace(dimension);

		if (workspace != null) {
			SetWorkspaceMessage message = workspace.regions().createSetWorkspaceMessage();
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
		}
	}
}
