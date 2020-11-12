package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class DisableHungerBehavior implements IMinigameBehavior {
	public static <T> DisableHungerBehavior parse(Dynamic<T> root) {
		return new DisableHungerBehavior();
	}

	@Override
	public void onParticipantUpdate(IMinigameInstance minigame, ServerPlayerEntity player) {
		if (player.ticksExisted % 20 == 0) {
			player.getFoodStats().setFoodLevel(20);
			player.getFoodStats().setFoodSaturationLevel(5.0F);
		}
	}
}
