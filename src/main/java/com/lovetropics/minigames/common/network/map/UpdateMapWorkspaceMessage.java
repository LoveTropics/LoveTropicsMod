package com.lovetropics.minigames.common.network.map;

import com.lovetropics.minigames.client.map.ClientMapWorkspace;
import com.lovetropics.minigames.common.map.MapRegionSet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.tropicraft.core.common.network.TropicraftMessage;

import java.util.function.Supplier;

public class UpdateMapWorkspaceMessage implements TropicraftMessage {
	private final MapRegionSet regions;

	public UpdateMapWorkspaceMessage(MapRegionSet regions) {
		this.regions = regions;
	}

	public void encode(PacketBuffer buffer) {
		this.regions.write(buffer);
	}

	public static UpdateMapWorkspaceMessage decode(PacketBuffer buffer) {
		MapRegionSet regions = new MapRegionSet();
		regions.read(buffer);

		return new UpdateMapWorkspaceMessage(regions);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ClientMapWorkspace.INSTANCE.setRegions(regions);
		});
	}
}
