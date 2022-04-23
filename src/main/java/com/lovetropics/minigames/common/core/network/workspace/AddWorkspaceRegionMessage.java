package com.lovetropics.minigames.common.core.network.workspace;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AddWorkspaceRegionMessage {
	private final int id;
	private final String key;
	private final BlockBox region;

	public AddWorkspaceRegionMessage(int id, String key, BlockBox region) {
		this.id = id;
		this.key = key;
		this.region = region;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeVarInt(id);
		buffer.writeUtf(key, 64);
		region.write(buffer);
	}

	public static AddWorkspaceRegionMessage decode(PacketBuffer buffer) {
		int id = buffer.readVarInt();
		String key = buffer.readUtf(64);
		BlockBox region = BlockBox.read(buffer);
		return new AddWorkspaceRegionMessage(id, key, region);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMapWorkspace.INSTANCE.addRegion(id, key, region);
		});
		ctx.get().setPacketHandled(true);
	}
}
