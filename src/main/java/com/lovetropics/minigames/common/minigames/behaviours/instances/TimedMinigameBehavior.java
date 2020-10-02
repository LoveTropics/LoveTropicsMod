package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.world.World;

public final class TimedMinigameBehavior implements IMinigameBehavior {
	private final long length;

	public TimedMinigameBehavior(long length) {
		this.length = length;
	}

	public static <T> TimedMinigameBehavior parse(Dynamic<T> root) {
		long length = root.get("length").asLong(20 * 60);
		return new TimedMinigameBehavior(length);
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		if (minigame.ticks() >= length) {
			MinigameManager.getInstance().finish();
		}
	}
}
