package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.GamePhase;
import com.lovetropics.minigames.common.core.game.state.GamePhaseState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.InteractionResult;

public final class BlockPackagesDuringPhaseBehavior implements IGameBehavior {
	public static final Codec<BlockPackagesDuringPhaseBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.listOrUnit(Codec.STRING).fieldOf("blocked_phases").forGetter(c -> new ArrayList<>(c.blockedPhases))
		).apply(instance, BlockPackagesDuringPhaseBehavior::new);
	});

	private final Set<String> blockedPhases;

	public BlockPackagesDuringPhaseBehavior(Collection<String> blockedPhases) {
		this.blockedPhases = new ObjectOpenHashSet<>(blockedPhases);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		GamePhaseState phases = game.getState().getOrThrow(GamePhaseState.KEY);

		events.listen(GamePackageEvents.RECEIVE_PACKAGE, ($, gamePackage) -> {
			GamePhase phase = phases.get();
			if (this.blockedPhases.contains(phase.key)) {
				return InteractionResult.FAIL;
			} else {
				return InteractionResult.PASS;
			}
		});
	}
}
