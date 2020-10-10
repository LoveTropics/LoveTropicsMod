package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class CancelPlayerDamageBehavior implements IMinigameBehavior {
	public static <T> CancelPlayerDamageBehavior parse(Dynamic<T> root) {
		return new CancelPlayerDamageBehavior();
	}

	@Override
	public void onPlayerHurt(IMinigameInstance minigame, LivingHurtEvent event) {
		event.setCanceled(true);
	}
}
