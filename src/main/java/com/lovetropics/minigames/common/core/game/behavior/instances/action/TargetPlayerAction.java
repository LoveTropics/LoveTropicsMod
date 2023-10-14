package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.ActionTargetTypes;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.PlayerActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.UUID;

public record TargetPlayerAction(UUID id, GameActionList<ServerPlayer> actions) implements IGameBehavior {
	public static final Codec<TargetPlayerAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(TargetPlayerAction::id),
			GameActionList.mandateTypeDefaultingTargetMap(ActionTargetTypes.PLAYER, PlayerActionTarget.SOURCE).forGetter(TargetPlayerAction::actions)
	).apply(i, TargetPlayerAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		actions.register(game, events);

		events.listen(GameActionEvents.APPLY, (context) -> {
			ServerPlayer player = game.getAllPlayers().getPlayerBy(id);
			if (player == null) {
				return false;
			}
			return actions.apply(game, context, List.of(player));
		});
	}
}
