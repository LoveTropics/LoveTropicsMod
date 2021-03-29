package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import net.minecraftforge.event.world.ExplosionEvent;

public class DisableTNTBlockDestructionBehavior implements IGameBehavior {
	public static final Codec<DisableTNTBlockDestructionBehavior> CODEC = Codec.unit(DisableTNTBlockDestructionBehavior::new);

	@Override
	public void onExplosionDetonate(IGameInstance minigame, ExplosionEvent.Detonate event) {
		event.getAffectedBlocks().clear();
	}
}
