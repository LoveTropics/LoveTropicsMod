package com.lovetropics.minigames.common.core.game.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Schedule events for within a game
 */
public final class GameScheduler {

	/**
	 * This is to avoid concurrent modification exceptions
	 */
	private final ArrayList<DelayedGameTickEvent> newTickEvents = new ArrayList<>();
	private final ArrayList<DelayedGameTickEvent> delayedTickEvents = new ArrayList<>();

	private final ArrayList<BlockChangeNotify> blockChangeNotifies = new ArrayList<>();

	public void tick() {
		this.delayedTickEvents.addAll(this.newTickEvents);
		this.newTickEvents.clear();
		Iterator<DelayedGameTickEvent> tickEventIterator = this.delayedTickEvents.iterator();
		while (tickEventIterator.hasNext()) {
			DelayedGameTickEvent event = tickEventIterator.next();
			event.tick();
			if (event.shouldRun()) {
				event.run();
				if(!(event instanceof DelayedGameIntervalEvent))
					tickEventIterator.remove();
			}
		}
		for (BlockChangeNotify blockChangeNotify : this.blockChangeNotifies) {
			blockChangeNotify.level.blockUpdated(blockChangeNotify.pos, blockChangeNotify.block);
		}
		this.blockChangeNotifies.clear();
	}

	/**
	 * Just to make sure that the clients are notified of block changes happening from the scheduler
	 *
	 * Should be slightly more efficient than firing this on each change during delayed events.
	 *
	 * @param pos
	 * @param level
	 */
	public record BlockChangeNotify(Level level, BlockPos pos, Block block) {
	}

	public void notifyBlockChange(BlockPos pos, Level level, Block block) {
		this.blockChangeNotifies.add(new BlockChangeNotify(level, pos, block));
	}

	public void delayedTickEvent(String name, Runnable consumer, int tickDelay) {
		this.newTickEvents.add(new DelayedGameTickEvent(name, consumer, tickDelay));
	}

	public void intervalTickEvent(String name, Runnable consumer, int tickDelay, int interval) {
		this.newTickEvents.add(new DelayedGameIntervalEvent(name, consumer, tickDelay, interval));
	}

	public void clearAllEvents() {
		this.newTickEvents.clear();
		this.delayedTickEvents.clear();
	}

	public static class DelayedGameTickEvent {

		// So we can find it later and remove it if needed
		public final String name;
		public final Runnable consumer;
		public int ticks;

		public DelayedGameTickEvent(String name, Runnable consumer, int ticks) {
			this.name = name;
			this.consumer = consumer;
			this.ticks = ticks;
		}

		public void tick() {
			this.ticks--;
		}

		public boolean shouldRun() {
			return this.ticks <= 0;
		}

		public void run() {
			this.consumer.run();
		}
	}

	public static class DelayedGameIntervalEvent extends DelayedGameTickEvent {

		public final int interval;

		public DelayedGameIntervalEvent(String name, Runnable consumer, int ticks, int interval) {
			super(name, consumer, ticks);
			this.interval = interval;
		}

		@Override
        public void run() {
			this.ticks = interval;
			super.run();
		}

	}
}
