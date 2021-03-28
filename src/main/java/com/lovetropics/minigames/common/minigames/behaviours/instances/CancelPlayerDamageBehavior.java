package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class CancelPlayerDamageBehavior implements IMinigameBehavior {
	public static final Codec<CancelPlayerDamageBehavior> CODEC = Codec.unit(CancelPlayerDamageBehavior::new);

	@Override
	public void onPlayerHurt(IMinigameInstance minigame, LivingHurtEvent event) {
		event.setCanceled(true);
	}
}
