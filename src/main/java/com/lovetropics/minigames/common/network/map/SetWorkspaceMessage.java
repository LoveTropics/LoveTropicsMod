package com.lovetropics.minigames.common.network.map;

import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import com.lovetropics.minigames.common.map.workspace.ClientWorkspaceRegions;
import com.lovetropics.minigames.common.map.workspace.WorkspaceRegions;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SetWorkspaceMessage {
	private WorkspaceRegions server;
	private ClientWorkspaceRegions client;

	public SetWorkspaceMessage(WorkspaceRegions server) {
		this.server = server;
	}

	public SetWorkspaceMessage(ClientWorkspaceRegions client) {
		this.client = client;
	}

	public void encode(PacketBuffer buffer) {
		this.server.write(buffer);
	}

	public static SetWorkspaceMessage decode(PacketBuffer buffer) {
		ClientWorkspaceRegions regions = ClientWorkspaceRegions.read(buffer);
		return new SetWorkspaceMessage(regions);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMapWorkspace.INSTANCE.setRegions(client);
		});
	}
}
