package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.world.World;

public final class TimedMinigameBehavior implements IMinigameBehavior {
	private final long length;
	private long time = 0;

	public TimedMinigameBehavior(long length) {
		this.length = length;
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		time = 0;
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		if (time++ >= length) {
			MinigameManager.getInstance().finishCurrentMinigame();
		}
	}
}
