package com.lovetropics.minigames.common.minigames.polling;

import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.Scheduler;
import com.lovetropics.minigames.common.minigames.*;
import com.lovetropics.minigames.common.minigames.behaviours.*;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class PollingMinigameInstance implements MinigameControllable, BehaviorDispatcher<IPollingMinigameBehavior, PollingMinigameInstance> {
	private final MinecraftServer server;
	private final IMinigameDefinition definition;

	/**
	 * A list of players that are currently registered for the currently polling minigame.
	 */
	private final MinigameRegistrations registrations = new MinigameRegistrations();

	private final BehaviorMap behaviors;

	private final Map<String, ControlCommandHandler> controlCommands = new Object2ObjectOpenHashMap<>();

	private PollingMinigameInstance(MinecraftServer server, IMinigameDefinition definition) {
		this.server = server;
		this.definition = definition;
		this.behaviors = definition.createBehaviors();
	}

	public static MinigameResult<PollingMinigameInstance> create(MinecraftServer server, IMinigameDefinition definition) {
		PollingMinigameInstance instance = new PollingMinigameInstance(server, definition);

		MinigameResult<Unit> result = instance.dispatchToBehaviors(IPollingMinigameBehavior::onStartPolling);
		return result.mapValue(instance);
	}

	public MinigameResult<ITextComponent> registerPlayerAs(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		if (registrations.contains(player.getUniqueID())) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED));
		}

		MinigameResult<Unit> result = dispatchToBehaviors((b, m) -> b.onPlayerRegister(m, player, requestedRole));
		if (result.isError()) {
			return result.castError();
		}

		registrations.add(player.getUniqueID(), requestedRole);

		if (registrations.participantCount() == definition.getMinimumParticipantCount()) {
			broadcastMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_ENOUGH_PLAYERS).applyTextStyle(TextFormatting.AQUA));
		}

		ITextComponent playerName = player.getDisplayName().deepCopy().applyTextStyle(TextFormatting.GOLD);
		ITextComponent minigameName = definition.getName().applyTextStyle(TextFormatting.GREEN);

		broadcastMessage(new TranslationTextComponent("%s has joined the %s minigame!", playerName, minigameName).applyTextStyle(TextFormatting.AQUA));

		return MinigameResult.ok(
				new TranslationTextComponent(
						TropicraftLangKeys.COMMAND_REGISTERED_FOR_MINIGAME,
						minigameName.applyTextStyle(TextFormatting.AQUA)
				).applyTextStyle(TextFormatting.GREEN)
		);
	}

	public MinigameResult<ITextComponent> unregisterPlayer(ServerPlayerEntity player) {
		if (!registrations.contains(player.getUniqueID())) {
			return MinigameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NOT_REGISTERED_FOR_MINIGAME));
		}

		registrations.remove(player.getUniqueID());

		if (registrations.participantCount() == definition.getMinimumParticipantCount() - 1) {
			broadcastMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_LONGER_ENOUGH_PLAYERS).applyTextStyle(TextFormatting.RED));
		}

		ITextComponent minigameName = definition.getName().applyTextStyle(TextFormatting.AQUA);
		return MinigameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, minigameName).applyTextStyle(TextFormatting.RED));
	}

	public CompletableFuture<MinigameResult<MinigameInstance>> start() {
		IMinigameMapProvider mapProvider = definition.getMapProvider();
		MinigameResult<Unit> openResult = mapProvider.canOpen(definition, server);
		if (openResult.isError()) {
			return CompletableFuture.completedFuture(openResult.castError());
		}

		return mapProvider.open(server)
				.thenApplyAsync(map -> MinigameInstance.create(definition, server, map, behaviors), server)
				.thenComposeAsync(minigameResult -> {
					if (minigameResult.isError()) {
						return CompletableFuture.completedFuture(minigameResult.<MinigameInstance>castError());
					}

					MinigameInstance minigame = minigameResult.getOk();

					List<ServerPlayerEntity> participants = new ArrayList<>();
					List<ServerPlayerEntity> spectators = new ArrayList<>();

					registrations.collectInto(server, participants, spectators, definition.getMaximumParticipantCount());

					for (ServerPlayerEntity player : participants) {
						minigame.addPlayer(player, PlayerRole.PARTICIPANT);
					}

					for (ServerPlayerEntity player : spectators) {
						minigame.addPlayer(player, PlayerRole.SPECTATOR);
					}

					return Scheduler.INSTANCE.submit(server -> {
						MinigameResult<Unit> startResult = minigame.dispatchToBehaviors(IMinigameBehavior::onStart);
						if (startResult.isError()) {
							return startResult.<MinigameInstance>castError();
						}

						return MinigameResult.ok(minigame);
					}, 1);
				}, server)
				.handleAsync((result, throwable) -> {
					if (throwable instanceof Exception) {
						return MinigameResult.fromException("Unknown error starting minigame", (Exception) throwable);
					}
					return result;
				}, server);
	}

	private void broadcastMessage(ITextComponent message) {
		for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
			player.sendMessage(message);
		}
	}

	public MinecraftServer getServer() {
		return server;
	}

	public IMinigameDefinition getDefinition() {
		return definition;
	}

	@Override
	public Collection<IPollingMinigameBehavior> getBehaviors() {
		return behaviors.getPollingBehaviors();
	}

	@Override
	public void addControlCommand(String name, ControlCommandHandler task) {
		this.controlCommands.put(name, task);
	}

	@Override
	public void invokeControlCommand(String name, CommandSource source) throws CommandSyntaxException {
		ControlCommandHandler task = this.controlCommands.get(name);
		if (task != null) {
			task.run(source);
		}
	}

	@Override
	public Set<String> getControlCommands() {
		return this.controlCommands.keySet();
	}
}
