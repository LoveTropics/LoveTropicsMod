package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.GamePhase;
import com.lovetropics.minigames.common.core.game.state.GamePhaseState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.InteractionResult;

import java.util.List;

public record BlockPackagesDuringPhaseBehavior(List<String> blockedPhases) implements IGameBehavior {
	public static final Codec<BlockPackagesDuringPhaseBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.listOrUnit(Codec.STRING).fieldOf("blocked_phases").forGetter(BlockPackagesDuringPhaseBehavior::blockedPhases)
	).apply(i, BlockPackagesDuringPhaseBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		GamePhaseState phases = game.getState().getOrNull(GamePhaseState.KEY);
		if (phases == null) {
			return;
		}

		events.listen(GamePackageEvents.RECEIVE_PACKAGE, ($, gamePackage) -> {
			GamePhase phase = phases.get();
			if (this.blockedPhases.contains(phase.key())) {
				return InteractionResult.FAIL;
			} else {
				return InteractionResult.PASS;
			}
		});
	}
}
