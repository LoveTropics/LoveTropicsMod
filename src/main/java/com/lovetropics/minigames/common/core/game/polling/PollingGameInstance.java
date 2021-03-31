package com.lovetropics.minigames.common.core.game.polling;

import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePollingEvents;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public final class PollingGameInstance implements ProtoGame, GameControllable {
	private final MinecraftServer server;
	private final IGameDefinition definition;

	private final PlayerKey initiator;

	/**
	 * A list of players that are currently registered for the currently polling minigame.
	 */
	private final MinigameRegistrations registrations = new MinigameRegistrations();

	private final BehaviorMap behaviors;

	private final Map<String, ControlCommand> controlCommands = new Object2ObjectOpenHashMap<>();

	private final GameEventListeners events = new GameEventListeners();

	private PollingGameInstance(MinecraftServer server, IGameDefinition definition, PlayerKey initiator) {
		this.server = server;
		this.definition = definition;
		this.behaviors = definition.createBehaviors();
		this.initiator = initiator;
	}

	public static GameResult<PollingGameInstance> create(MinecraftServer server, IGameDefinition definition, PlayerKey initiator) {
		PollingGameInstance instance = new PollingGameInstance(server, definition, initiator);

		GameResult<Unit> result = instance.registerBehaviors();
		return result.mapValue(instance);
	}

	private GameResult<Unit> registerBehaviors() {
		for (IGameBehavior behavior : getBehaviors()) {
			try {
				behavior.registerPolling(this, events);
			} catch (GameException e) {
				return GameResult.error(e.getTextMessage());
			}
		}

		return GameResult.ok();
	}

	@Override
	public GameStatus getStatus() {
		return GameStatus.POLLING;
	}

	public GameResult<ITextComponent> joinPlayerAs(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		if (registrations.contains(player.getUniqueID())) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED));
		}

		try {
			invoker(GamePollingEvents.PLAYER_REGISTER).onPlayerRegister(this, player, requestedRole);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch player register event", e);
		}

		registrations.add(player.getUniqueID(), requestedRole);

		if (registrations.participantCount() == definition.getMinimumParticipantCount()) {
			broadcastMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_ENOUGH_PLAYERS).mergeStyle(TextFormatting.AQUA));
		}

		ITextComponent playerName = player.getDisplayName().deepCopy().mergeStyle(TextFormatting.GOLD);
		ITextComponent minigameName = definition.getName().deepCopy().mergeStyle(TextFormatting.GREEN);

		String message = requestedRole != PlayerRole.SPECTATOR ? "%s has joined the %s minigame!" : "%s has joined to spectate the %s minigame!";
		broadcastMessage(new TranslationTextComponent(message, playerName, minigameName).mergeStyle(TextFormatting.AQUA));
		PlayerRole trueRole = requestedRole == null ? PlayerRole.PARTICIPANT : requestedRole;
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(trueRole));
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(trueRole, getMemberCount(trueRole)));

		return GameResult.ok(
				new TranslationTextComponent(
						TropicraftLangKeys.COMMAND_REGISTERED_FOR_MINIGAME,
						minigameName.deepCopy().mergeStyle(TextFormatting.AQUA)
				).mergeStyle(TextFormatting.GREEN)
		);
	}

	public GameResult<ITextComponent> removePlayer(ServerPlayerEntity player) {
		if (!registrations.contains(player.getUniqueID())) {
			return GameResult.error(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NOT_REGISTERED_FOR_MINIGAME));
		}

		registrations.remove(player.getUniqueID());

		if (registrations.participantCount() == definition.getMinimumParticipantCount() - 1) {
			broadcastMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_LONGER_ENOUGH_PLAYERS).mergeStyle(TextFormatting.RED));
		}
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(null));
		for (PlayerRole role : PlayerRole.values()) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(role, getMemberCount(role)));
		}

		ITextComponent minigameName = definition.getName().deepCopy().mergeStyle(TextFormatting.AQUA);
		return GameResult.ok(new TranslationTextComponent(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, minigameName).mergeStyle(TextFormatting.RED));
	}

	public CompletableFuture<GameResult<GameInstance>> start() {
		return definition.getMapProvider().open(server)
				.thenComposeAsync(result -> {
					if (result.isOk()) {
						GameMap map = result.getOk();
						return GameInstance.start(definition, server, map, behaviors, initiator, registrations);
					}
					return CompletableFuture.completedFuture(result.castError());
				}, server)
				.handleAsync((result, throwable) -> {
					if (throwable instanceof Exception) {
						return GameResult.fromException("Unknown error starting minigame", (Exception) throwable);
					}
					return result;
				}, server);
	}

	private void broadcastMessage(ITextComponent message) {
		for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
			player.sendStatusMessage(message, false);
		}
	}

	@Override
	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public IGameDefinition getDefinition() {
		return definition;
	}

	@Override
	public BehaviorMap getBehaviors() {
		return behaviors;
	}

	@Override
	public void addControlCommand(String name, ControlCommand command) {
		this.controlCommands.put(name, command);
	}

	@Override
	public void invokeControlCommand(String name, CommandSource source) throws CommandSyntaxException {
		ControlCommand command = this.controlCommands.get(name);
		if (command != null) {
			command.invoke(this, source);
		}
	}

	@Override
	public Stream<String> controlCommandsFor(CommandSource source) {
		return this.controlCommands.entrySet().stream()
				.filter(entry -> entry.getValue().canUse(this, source))
				.map(Map.Entry::getKey);
	}

	@Nullable
	@Override
	public PlayerKey getInitiator() {
		return initiator;
	}

	@Override
	public GameEventListeners getEvents() {
		return this.events;
	}

	@Override
	public int getMemberCount(PlayerRole role) {
		// TODO extensible
		return role == PlayerRole.PARTICIPANT ? registrations.participantCount() : registrations.spectatorCount();
	}

	public boolean isPlayerRegistered(ServerPlayerEntity player) {
		return registrations.contains(player.getUniqueID());
	}
}
