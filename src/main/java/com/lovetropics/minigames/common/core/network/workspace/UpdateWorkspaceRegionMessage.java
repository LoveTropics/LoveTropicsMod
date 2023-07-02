package com.lovetropics.minigames.common.core.network.workspace;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import com.lovetropics.minigames.common.core.map.workspace.MapWorkspace;
import com.lovetropics.minigames.common.core.map.workspace.MapWorkspaceManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record UpdateWorkspaceRegionMessage(int id, BlockBox region) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeVarInt(id);
		if (region != null) {
			buffer.writeBoolean(true);
			region.write(buffer);
		} else {
			buffer.writeBoolean(false);
		}
	}

	public static UpdateWorkspaceRegionMessage decode(FriendlyByteBuf buffer) {
		int id = buffer.readVarInt();
		BlockBox region = buffer.readBoolean() ? BlockBox.read(buffer) : null;
		return new UpdateWorkspaceRegionMessage(id, region);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		NetworkDirection direction = ctx.get().getDirection();
		if (direction.getReceptionSide() == LogicalSide.SERVER) {
			handleServer(ctx.get());
		} else {
			handleClient();
		}
	}

	private void handleServer(NetworkEvent.Context ctx) {
		ServerPlayer sender = ctx.getSender();
		if (sender == null) {
			return;
		}

		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(sender.server);
		MapWorkspace workspace = workspaceManager.getWorkspace(sender.level().dimension());
		if (workspace != null) {
			workspace.regions().set(id, region);
		}
	}

	private void handleClient() {
		ClientMapWorkspace.INSTANCE.updateRegion(id, region);
	}
}
