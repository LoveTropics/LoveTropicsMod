package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public record CancelPlayerDamageBehavior(boolean knockback) implements IGameBehavior {
	public static final Codec<CancelPlayerDamageBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.BOOL.optionalFieldOf("knockback", false).forGetter(c -> c.knockback)
	).apply(i, CancelPlayerDamageBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		if (this.knockback) {
			events.listen(GamePlayerEvents.DAMAGE_AMOUNT, (player, damageSource, amount, originalAmount) -> 0.0F);
		} else {
			events.listen(GamePlayerEvents.ATTACK, (player, target) -> target instanceof Player ? InteractionResult.FAIL : InteractionResult.PASS);
			events.listen(GamePlayerEvents.DAMAGE, (player, damageSource, amount) -> InteractionResult.FAIL);
		}
	}
}
