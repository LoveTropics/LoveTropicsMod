package com.lovetropics.minigames.common.network.map;

import java.util.function.Supplier;

import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.workspace.MapWorkspace;
import com.lovetropics.minigames.common.map.workspace.MapWorkspaceManager;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

public class UpdateWorkspaceRegionMessage {
	private final int id;
	private final MapRegion region;

	public UpdateWorkspaceRegionMessage(int id, MapRegion region) {
		this.id = id;
		this.region = region;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		if (region != null) {
			buffer.writeBoolean(true);
			region.write(buffer);
		} else {
			buffer.writeBoolean(false);
		}
	}

	public static UpdateWorkspaceRegionMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		MapRegion region = buffer.readBoolean() ? MapRegion.read(buffer) : null;
		return new UpdateWorkspaceRegionMessage(id, region);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			NetworkDirection direction = ctx.get().getDirection();
			if (direction.getReceptionSide() == LogicalSide.SERVER) {
				handleServer(ctx.get());
			} else {
				handleClient();
			}
		});
	}

	private void handleServer(NetworkEvent.Context ctx) {
		ServerPlayerEntity sender = ctx.getSender();
		if (sender == null) {
			return;
		}

		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(sender.server);
		MapWorkspace workspace = workspaceManager.getWorkspace(sender.dimension);
		if (workspace != null) {
			workspace.getRegions().set(id, region);
		}
	}

	private void handleClient() {
		ClientMapWorkspace.INSTANCE.updateRegion(id, region);
	}
}
