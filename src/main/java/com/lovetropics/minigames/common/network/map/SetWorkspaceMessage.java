package com.lovetropics.minigames.common.network.map;

import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import com.lovetropics.minigames.common.map.workspace.ClientWorkspaceRegions;
import com.lovetropics.minigames.common.map.workspace.WorkspaceRegions;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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

	public void encode(PacketBuffer buffer) {
		buffer.writeBoolean(server != null);
		if (this.server != null) {
			this.server.write(buffer);
		}
	}

	public static SetWorkspaceMessage decode(PacketBuffer buffer) {
		ClientWorkspaceRegions regions;
		if (buffer.readBoolean()) {
			regions = ClientWorkspaceRegions.read(buffer);
		} else {
			regions = ClientWorkspaceRegions.noop();
		}
		return new SetWorkspaceMessage(regions);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMapWorkspace.INSTANCE.setRegions(client);
		});
		ctx.get().setPacketHandled(true);
	}
}
