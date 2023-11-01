package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.util.Codecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import java.util.Optional;

public record OnDeathTrigger(GameActionList<ServerPlayer> actions, Optional<EntityPredicate> killedPredicate, Optional<EntityPredicate> killerPredicate, boolean onKiller) implements IGameBehavior {
	public static final MapCodec<OnDeathTrigger> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			GameActionList.PLAYER_MAP_CODEC.forGetter(OnDeathTrigger::actions),
			Codecs.ENTITY_PREDICATE.optionalFieldOf("killed_predicate").forGetter(OnDeathTrigger::killedPredicate),
			Codecs.ENTITY_PREDICATE.optionalFieldOf("killer_predicate").forGetter(OnDeathTrigger::killerPredicate),
			Codec.BOOL.optionalFieldOf("on_killer", false).forGetter(OnDeathTrigger::onKiller)
	).apply(i, OnDeathTrigger::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		actions.register(game, events);

		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			if (killerPredicate.isPresent() && !killerPredicate.get().matches(player, damageSource.getEntity())) {
				return InteractionResult.PASS;
			}
			if (killedPredicate.isPresent() && !killedPredicate.get().matches(player, player)) {
				return InteractionResult.PASS;
			}
			final GameActionContext.Builder context = GameActionContext.builder().set(GameActionParameter.KILLED, player);
			if (damageSource.getEntity() instanceof final ServerPlayer killer) {
				actions.apply(game, context.set(GameActionParameter.KILLER, killer).build(), onKiller ? killer : player);
			} else if (!onKiller) {
				actions.apply(game, context.build(), player);
			}
			return InteractionResult.PASS;
		});
	}
}
