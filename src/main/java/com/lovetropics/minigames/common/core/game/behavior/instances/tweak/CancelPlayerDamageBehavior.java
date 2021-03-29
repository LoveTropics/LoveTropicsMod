package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class CancelPlayerDamageBehavior implements IGameBehavior {
	public static final Codec<CancelPlayerDamageBehavior> CODEC = Codec.unit(CancelPlayerDamageBehavior::new);

	@Override
	public void onPlayerHurt(IGameInstance minigame, LivingHurtEvent event) {
		event.setCanceled(true);
	}
}
