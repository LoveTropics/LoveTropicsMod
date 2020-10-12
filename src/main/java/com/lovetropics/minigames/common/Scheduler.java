package com.lovetropics.minigames.common;

import com.lovetropics.minigames.Constants;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class Scheduler {
	public static final Scheduler INSTANCE = new Scheduler();

	private final ConcurrentLinkedQueue<Task> taskQueue = new ConcurrentLinkedQueue<>();
	private int tick = 0;

	@SubscribeEvent
	public static void onTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
			Scheduler.INSTANCE.runTasks(server);
		}
	}

	public <T> CompletableFuture<T> submit(Function<MinecraftServer, T> task) {
		return this.submit(task, 0);
	}

	public <T> CompletableFuture<T> submit(Function<MinecraftServer, T> task, int delay) {
		CompletableFuture<T> future = new CompletableFuture<>();
		this.submit(server -> {
			T result = task.apply(server);
			future.complete(result);
		}, delay);
		return future;
	}

	public void submit(Consumer<MinecraftServer> task) {
		this.submit(task, 0);
	}

	public void submit(Consumer<MinecraftServer> task, int delay) {
		this.taskQueue.add(new Task(task, this.tick + delay));
	}

	private void enqueue(Task task) {
		this.taskQueue.add(task);
	}

	private void runTasks(MinecraftServer server) {
		int time = server.getTickCounter();
		this.tick = time;

		Iterator<Task> iterator = this.taskQueue.iterator();
		while (iterator.hasNext()) {
			Task task = iterator.next();
			if (task.tryRun(server, time)) {
				iterator.remove();
			}
		}
	}

	static class Task {
		private final Consumer<MinecraftServer> action;
		private final int time;

		Task(Consumer<MinecraftServer> action, int time) {
			this.action = action;
			this.time = time;
		}

		boolean tryRun(MinecraftServer server, int time) {
			if (time >= this.time) {
				this.action.accept(server);
				return true;
			}
			return false;
		}
	}
}
