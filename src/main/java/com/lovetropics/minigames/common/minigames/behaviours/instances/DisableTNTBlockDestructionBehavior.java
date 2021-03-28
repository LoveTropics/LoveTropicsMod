package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import net.minecraftforge.event.world.ExplosionEvent;

public class DisableTNTBlockDestructionBehavior implements IMinigameBehavior {
	public static final Codec<DisableTNTBlockDestructionBehavior> CODEC = Codec.unit(DisableTNTBlockDestructionBehavior::new);

	@Override
	public void onExplosionDetonate(IMinigameInstance minigame, ExplosionEvent.Detonate event) {
		event.getAffectedBlocks().clear();
	}
}
