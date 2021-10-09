package com.lovetropics.minigames.client.chase;

import com.lovetropics.minigames.Constants;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ChaseCameraManager {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	static final double MAX_CHASE_DISTANCE = 16.0;

	static ChaseCameraSession session;

	public static void update(List<UUID> players) {
		ChaseCameraSession session = ChaseCameraManager.session;
		if (session == null) {
			ChaseCameraManager.session = new ChaseCameraSession(players);
		} else {
			session.updatePlayers(players);
		}
	}

	public static void stop() {
		ChaseCameraSession session = ChaseCameraManager.session;
		ChaseCameraManager.session = null;

		if (session != null) {
			session.close();
		}
	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().isRemote()) {
			stop();
		}
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		ChaseCameraSession session = ChaseCameraManager.session;
		if (session == null || event.phase == TickEvent.Phase.START) {
			return;
		}

		if (CLIENT.player != null) {
			session.tick();
		}
	}

	@SubscribeEvent
	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		ChaseCameraSession session = ChaseCameraManager.session;
		if (session == null || event.phase == TickEvent.Phase.END) {
			return;
		}

		if (CLIENT.player != null) {
			session.renderTick();
		}
	}

	@SubscribeEvent
	public static void onPositionCamera(EntityViewRenderEvent.CameraSetup event) {
		ChaseCameraSession session = ChaseCameraManager.session;
		if (session == null) {
			return;
		}

		session.applyToCamera(event.getInfo(), (float) event.getRenderPartialTicks(), event);
	}
}
