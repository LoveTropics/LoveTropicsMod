package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.util.ActionResultType;

public final class CancelPlayerDamageBehavior implements IGameBehavior {
	public static final Codec<CancelPlayerDamageBehavior> CODEC = Codec.unit(CancelPlayerDamageBehavior::new);

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) {
		events.listen(GamePlayerEvents.DAMAGE, (game, player, damageSource, amount) -> ActionResultType.FAIL);
	}
}
