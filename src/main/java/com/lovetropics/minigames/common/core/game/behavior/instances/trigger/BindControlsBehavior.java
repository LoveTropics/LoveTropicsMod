package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommand;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Map;

public record BindControlsBehavior(Map<ControlCommand.Scope, Map<String, GameActionList>> scopedActions) implements IGameBehavior {
	public static final Codec<BindControlsBehavior> CODEC = Codec.unboundedMap(ControlCommand.Scope.CODEC, Codec.unboundedMap(Codec.STRING, GameActionList.CODEC))
			.xmap(BindControlsBehavior::new, b -> b.scopedActions);

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		scopedActions.forEach((scope, scopedActions) -> scopedActions.forEach((control, actions) -> {
			actions.register(game, events);

			game.getControlCommands().add(control, new ControlCommand(scope, source -> {
				Entity entity = source.getEntity();
				if (entity instanceof ServerPlayer player) {
					actions.apply(game, GameActionContext.EMPTY, player);
				} else {
					actions.apply(game, GameActionContext.EMPTY);
				}
			}));
		}));
	}
}
