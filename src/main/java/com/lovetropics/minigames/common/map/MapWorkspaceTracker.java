package com.lovetropics.minigames.common.map;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.network.LTNetwork;
import com.lovetropics.minigames.common.network.map.UpdateMapWorkspaceMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class MapWorkspaceTracker {
	@SubscribeEvent
	public static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		PlayerEntity player = event.getPlayer();
		MinecraftServer server = player.world.getServer();
		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(server);

		DimensionType dimension = event.getTo();
		MapWorkspace workspace = workspaceManager.getWorkspace(dimension);

		if (workspace != null) {
			UpdateMapWorkspaceMessage message = new UpdateMapWorkspaceMessage(workspace.getRegions());
			LTNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimension), message);
		}
	}
}
