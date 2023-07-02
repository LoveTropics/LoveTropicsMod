package com.lovetropics.minigames.common.core.network.workspace;

import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import com.lovetropics.minigames.common.core.map.workspace.ClientWorkspaceRegions;
import com.lovetropics.minigames.common.core.map.workspace.WorkspaceRegions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class SetWorkspaceMessage {

	private static final SetWorkspaceMessage HIDDEN = new SetWorkspaceMessage((WorkspaceRegions) null);

	public static SetWorkspaceMessage hidden() {
		return HIDDEN;
	}

	private @Nullable WorkspaceRegions server;
	private ClientWorkspaceRegions client;

	public SetWorkspaceMessage(@Nullable WorkspaceRegions server) {
		this.server = server;
	}

	public SetWorkspaceMessage(ClientWorkspaceRegions client) {
		this.client = client;
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBoolean(server != null);
		if (this.server != null) {
			this.server.write(buffer);
		}
	}

	public static SetWorkspaceMessage decode(FriendlyByteBuf buffer) {
		ClientWorkspaceRegions regions;
		if (buffer.readBoolean()) {
			regions = ClientWorkspaceRegions.read(buffer);
		} else {
			regions = ClientWorkspaceRegions.noop();
		}
		return new SetWorkspaceMessage(regions);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ClientMapWorkspace.INSTANCE.setRegions(client);
	}
}
