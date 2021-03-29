package com.lovetropics.minigames.common.core.network.workspace;

import java.util.function.Supplier;

import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import com.lovetropics.minigames.common.core.map.MapRegion;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class AddWorkspaceRegionMessage {
	private final int id;
	private final String key;
	private final MapRegion region;

	public AddWorkspaceRegionMessage(int id, String key, MapRegion region) {
		this.id = id;
		this.key = key;
		this.region = region;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeString(key, 64);
		region.write(buffer);
	}

	public static AddWorkspaceRegionMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		String key = buffer.readString(64);
		MapRegion region = MapRegion.read(buffer);
		return new AddWorkspaceRegionMessage(id, key, region);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMapWorkspace.INSTANCE.addRegion(id, key, region);
		});
		ctx.get().setPacketHandled(true);
	}
}
