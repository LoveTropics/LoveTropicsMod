package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.TimeInterpolationClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientTimeInterpolationHandler {
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) return;

		ClientLevel world = Minecraft.getInstance().level;
		if (!isTimePaused() && world != null) {
			handleTick(world);
		}
	}

	private static void handleTick(ClientLevel world) {
		TimeInterpolationClientState time = ClientGameStateManager.getOrNull(GameClientStateTypes.TIME_INTERPOLATION);
		if (time != null) {
			int speed = time.getSpeed();
			world.setDayTime(world.getDayTime() + speed - 1);
		}
	}

	private static boolean isTimePaused() {
		return Minecraft.getInstance().isPaused() && Minecraft.getInstance().hasSingleplayerServer();
	}
}
