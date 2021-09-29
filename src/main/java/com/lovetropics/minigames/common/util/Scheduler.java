package com.lovetropics.minigames.common.util;

import com.lovetropics.minigames.Constants;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class Scheduler {
	private static final ConcurrentMap<Integer, Tick> TICKS = new ConcurrentHashMap<>();
	private static int time = 0;

	@SubscribeEvent
	public static void onTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			MinecraftServer server = LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER);
			Scheduler.runTasks(server);
		}
	}

	public static Tick atTime(int time) {
		return Scheduler.TICKS.computeIfAbsent(time, t -> new Tick());
	}

	public static Tick inTicks(int delay) {
		return Scheduler.atTime(Scheduler.time + delay);
	}

	public static Tick nextTick() {
		return inTicks(1);
	}

	private static void runTasks(MinecraftServer server) {
		int time = server.getTickCounter();
		Scheduler.time = time;

		Tick tick = TICKS.remove(time);
		if (tick != null) {
			tick.run(server);
		}
	}

	public static final class Tick implements Executor {
		private final List<Consumer<MinecraftServer>> tasks = new ArrayList<>();

		public <T> CompletableFuture<T> supply(Function<MinecraftServer, T> task) {
			CompletableFuture<T> future = new CompletableFuture<>();
			this.run(server -> {
				T result = task.apply(server);
				future.complete(result);
			});
			return future;
		}

		public void run(Consumer<MinecraftServer> task) {
			this.tasks.add(task);
		}

		void run(MinecraftServer server) {
			for (Consumer<MinecraftServer> task : this.tasks) {
				task.accept(server);
			}
		}

		@Override
		public void execute(Runnable command) {
			this.run(server -> command.run());
		}
	}
}
