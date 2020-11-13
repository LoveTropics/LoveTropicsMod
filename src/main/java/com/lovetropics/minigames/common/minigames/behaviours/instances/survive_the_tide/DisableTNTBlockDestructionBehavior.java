package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraftforge.event.world.ExplosionEvent;

public class DisableTNTBlockDestructionBehavior implements IMinigameBehavior {

	public DisableTNTBlockDestructionBehavior() {
	}

	public static <T> DisableTNTBlockDestructionBehavior parse(Dynamic<T> root) {
		return new DisableTNTBlockDestructionBehavior();
	}

	@Override
	public void onExplosionDetonate(IMinigameInstance minigame, ExplosionEvent.Detonate event) {
		event.getAffectedBlocks().clear();
	}
}
