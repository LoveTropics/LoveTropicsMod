package com.lovetropics.minigames.client.tweaks;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakTypes;
import com.lovetropics.minigames.common.core.game.client_tweak.instance.TimeInterpolationTweak;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientTimeInterpolationManager {
	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) return;

		ClientWorld world = Minecraft.getInstance().world;
		if (!isTimePaused() && world != null) {
			handleTick(world);
		}
	}

	private static void handleTick(ClientWorld world) {
		TimeInterpolationTweak time = ClientGameTweaksState.getOrNull(GameClientTweakTypes.TIME_INTERPOLATION);
		if (time != null) {
			int speed = time.getSpeed();
			world.setDayTime(world.getDayTime() + speed - 1);
		}
	}

	private static boolean isTimePaused() {
		return Minecraft.getInstance().isGamePaused() && Minecraft.getInstance().isSingleplayer();
	}
}
