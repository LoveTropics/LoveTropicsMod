package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.server.ServerWorld;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

// TODO: separate phase holder and phase controller behaviors- and maybe we have a system separate to behaviors that stores state like phases?
public class PhasesMinigameBehavior implements IMinigameBehavior
{
	public static final Codec<PhasesMinigameBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MinigamePhase.CODEC.listOf().fieldOf("phases").forGetter(c -> c.phases)
		).apply(instance, PhasesMinigameBehavior::new);
	});

	private final List<MinigamePhase> phases;
	private MinigamePhase currentPhase;
	private MinigamePhase previousPhase;
	private Iterator<MinigamePhase> phaseIterator;
	private int currentPhaseTicks;
	private boolean hasFinishedPhases = false;

	public PhasesMinigameBehavior(final List<MinigamePhase> phases) {
		this.phases = phases;
	}

	public MinigamePhase getFirstPhase() {
		if (phases.isEmpty()) {
			return null;
		}

		return phases.get(0);
	}

	public MinigamePhase getCurrentPhase() {
		return currentPhase;
	}

	public Optional<MinigamePhase> getPreviousPhase() {
		return Optional.ofNullable(previousPhase);
	}

	public int getCurrentPhaseTicks() {
		return currentPhaseTicks;
	}

	private boolean nextPhase(final IMinigameInstance instance) {
		if (phaseIterator.hasNext()) {
			previousPhase = currentPhase;
			currentPhase = phaseIterator.next();
			currentPhaseTicks = 0;

			for (CommandEventsBehavior commands : instance.getBehaviors(MinigameBehaviorTypes.COMMANDS.get())) {
				commands.invoke(currentPhase.getKey(), instance.getCommandSource());
			}
			return true;
		}

		return false;
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, ServerWorld world) {
		currentPhaseTicks++;

		if (!hasFinishedPhases && currentPhaseTicks >= currentPhase.lengthInTicks) {
			if (!nextPhase(minigame)) {
				hasFinishedPhases = true;
			}
		}
	}

	@Override
	public void onStart(final IMinigameInstance minigame) {
		hasFinishedPhases = false;
		currentPhaseTicks = 0;
		phaseIterator = phases.iterator();
		nextPhase(minigame);
	}

	public static class MinigamePhase {
		public static final Codec<MinigamePhase> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.STRING.fieldOf("key").forGetter(c -> c.key),
					Codec.INT.fieldOf("length_in_ticks").forGetter(c -> c.lengthInTicks)
			).apply(instance, MinigamePhase::new);
		});

		private final String key;
		private final int lengthInTicks;

		public MinigamePhase(final String key, final int lengthInTicks) {
			this.key = key;
			this.lengthInTicks = lengthInTicks;
		}

		public boolean is(final String key) {
			return this.key.equals(key);
		}

		public int getLengthInTicks()
		{
			return lengthInTicks;
		}

		public String getKey()
		{
			return key;
		}
	}

}
