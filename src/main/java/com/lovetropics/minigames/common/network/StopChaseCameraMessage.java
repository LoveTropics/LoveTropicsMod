package com.lovetropics.minigames.common.network;

import com.lovetropics.minigames.client.chase.ChaseCameraManager;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class StopChaseCameraMessage {
	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ChaseCameraManager.stop();
		});
	}
}
