package com.lovetropics.minigames.common.network.map;

import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import com.lovetropics.minigames.common.map.MapRegion;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.tropicraft.core.common.network.TropicraftMessage;

import java.util.function.Supplier;

public class UpdateWorkspaceRegionMessage implements TropicraftMessage {
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
			ClientMapWorkspace.INSTANCE.updateRegion(id, region);
		});
	}
}
