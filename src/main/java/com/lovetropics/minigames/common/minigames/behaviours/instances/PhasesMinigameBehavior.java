package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.mojang.datafixers.Dynamic;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Optional;

public class PhasesMinigameBehavior implements IMinigameBehavior
{
	private final LinkedList<MinigamePhase> phases;
	private MinigamePhase currentPhase;
	private MinigamePhase previousPhase;
	private Iterator<MinigamePhase> phaseIterator;
	private int currentPhaseTicks;
	private boolean hasFinishedPhases = false;

	public PhasesMinigameBehavior(final LinkedList<MinigamePhase> phases) {
		this.phases = phases;
	}

	public static <T> PhasesMinigameBehavior parse(final Dynamic<T> root) {
		final LinkedList<MinigamePhase> phases = Lists.newLinkedList(root.get("phases").asList(MinigamePhase::parse));
		return new PhasesMinigameBehavior(phases);
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

			instance.getBehavior(MinigameBehaviorTypes.COMMANDS.get()).ifPresent(commands -> {
				commands.invoke(currentPhase.getKey(), instance.getCommandSource());
			});
			return true;
		}

		return false;
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
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

	@Override
	public void onPostFinish(final IMinigameInstance minigame) {
		phaseIterator = null;
		currentPhase = null;
		previousPhase = null;
	}

	public static class MinigamePhase {
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

		public static <T> MinigamePhase parse(final Dynamic<T> root) {
			final String key = root.get("key").asString("");
			final int lengthInTicks = root.get("length_in_ticks").asInt(0);

			return new MinigamePhase(key, lengthInTicks);
		}
	}

}
