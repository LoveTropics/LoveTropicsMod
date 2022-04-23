package com.lovetropics.minigames.client.game.handler.spectate;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.handler.ClientGameStateHandler;
import com.lovetropics.minigames.common.core.game.client_state.instance.SpectatingClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientSpectatingManager implements ClientGameStateHandler<SpectatingClientState> {
	public static final ClientSpectatingManager INSTANCE = new ClientSpectatingManager();

	private static final Minecraft CLIENT = Minecraft.getInstance();

	static final double MAX_CHASE_DISTANCE = 16.0;

	SpectatingSession session;

	@Override
	public void accept(SpectatingClientState state) {
		SpectatingSession session = this.session;
		if (session == null) {
			this.session = new SpectatingSession(state.getPlayers());
		} else {
			session.updatePlayers(state.getPlayers());
		}
	}

	@Override
	public void disable(SpectatingClientState state) {
		SpectatingSession session = this.session;
		this.session = null;

		if (session != null) {
			session.close();
		}
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.START && CLIENT.player != null) {
			SpectatingSession session = INSTANCE.session;
			if (session != null) {
				session.tick();

				// keep the vanilla spectator gui closed
				SpectatorGui spectatorGui = CLIENT.gui.getSpectatorGui();
				spectatorGui.onSpectatorMenuClosed(null);
			}
		}
	}

	@SubscribeEvent
	public static void onRenderTick(TickEvent.RenderTickEvent event) {
		if (event.phase != TickEvent.Phase.END && CLIENT.player != null) {
			SpectatingSession session = INSTANCE.session;
			if (session != null) {
				session.renderTick();
			}
		}
	}

	@SubscribeEvent
	public static void onPositionCamera(EntityViewRenderEvent.CameraSetup event) {
		SpectatingSession session = INSTANCE.session;
		if (session != null) {
			session.applyToCamera(event.getCamera(), (float) event.getPartialTicks(), event);
		}
	}
}
