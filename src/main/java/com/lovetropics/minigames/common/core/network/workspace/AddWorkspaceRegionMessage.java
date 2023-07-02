package com.lovetropics.minigames.common.core.network.workspace;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record AddWorkspaceRegionMessage(int id, String key, BlockBox region) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeVarInt(id);
		buffer.writeUtf(key, 64);
		region.write(buffer);
	}

	public static AddWorkspaceRegionMessage decode(FriendlyByteBuf buffer) {
		int id = buffer.readVarInt();
		String key = buffer.readUtf(64);
		BlockBox region = BlockBox.read(buffer);
		return new AddWorkspaceRegionMessage(id, key, region);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ClientMapWorkspace.INSTANCE.addRegion(id, key, region);
	}
}
