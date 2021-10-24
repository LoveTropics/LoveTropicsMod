package com.lovetropics.minigames.client.tweaks;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakMap;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class ClientGameTweaksState {
	private static GameClientTweakMap tweaks;

	public static <T extends GameClientTweak> void set(T tweak) {
		GameClientTweakMap tweaks = ClientGameTweaksState.tweaks;
		if (tweaks == null) {
			ClientGameTweaksState.tweaks = tweaks = new GameClientTweakMap();
		}

		tweaks.add(tweak);
	}

	public static <T extends GameClientTweak> void remove(GameClientTweakType<T> type) {
		GameClientTweakMap tweaks = ClientGameTweaksState.tweaks;
		if (tweaks != null) {
			tweaks.remove(type);
			if (tweaks.isEmpty()) {
				ClientGameTweaksState.tweaks = null;
			}
		}
	}

	@Nullable
	public static <T extends GameClientTweak> T getOrNull(Supplier<GameClientTweakType<T>> type) {
		GameClientTweakMap tweaks = ClientGameTweaksState.tweaks;
		if (tweaks != null) {
			return tweaks.getOrNull(type.get());
		}
		return null;
	}

	@SubscribeEvent
	public static void onPlayerLoggedOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		tweaks = null;
	}
}
