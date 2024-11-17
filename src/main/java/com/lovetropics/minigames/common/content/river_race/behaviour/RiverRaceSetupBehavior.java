package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.minigames.common.content.river_race.RiverRaceState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;

public final class RiverRaceSetupBehavior implements IGameBehavior {
	public static final MapCodec<RiverRaceSetupBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Direction.CODEC.fieldOf("forward_direction").forGetter(b -> b.forwardDirection)
	).apply(i, RiverRaceSetupBehavior::new));

	private final Direction forwardDirection;

	public RiverRaceSetupBehavior(Direction forwardDirection) {
		this.forwardDirection = forwardDirection;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		RiverRaceState riverRace = game.state().get(RiverRaceState.KEY);
		riverRace.setForwardDirection(forwardDirection);
	}
}
