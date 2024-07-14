package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.InteractionResult;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.List;

public record BlockPackagesDuringPhaseBehavior(List<ProgressionPeriod> blockedPeriods) implements IGameBehavior {
	public static final MapCodec<BlockPackagesDuringPhaseBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.listOrUnit(ProgressionPeriod.CODEC).fieldOf("block_periods").forGetter(BlockPackagesDuringPhaseBehavior::blockedPeriods)
	).apply(i, BlockPackagesDuringPhaseBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		GameProgressionState progression = game.state().getOrNull(GameProgressionState.KEY);
		if (progression == null) {
			return;
		}

		MutableBoolean gameOver = new MutableBoolean();
		events.listen(GameLogicEvents.GAME_OVER, gameOver::setTrue);

		events.listen(GamePackageEvents.RECEIVE_PACKAGE, gamePackage -> {
			if (progression.is(blockedPeriods) || gameOver.isTrue()) {
				return InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
	}
}
