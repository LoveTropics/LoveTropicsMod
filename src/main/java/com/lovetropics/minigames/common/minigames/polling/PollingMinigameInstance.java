package com.lovetropics.minigames.common.minigames.polling;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.client.minigame.ClientJoinLeaveMessage;
import com.lovetropics.minigames.common.minigames.ControlCommandHandler;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.MinigameControllable;
import com.lovetropics.minigames.common.minigames.MinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.lovetropics.minigames.common.minigames.MinigameStatus;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.ProtoMinigame;
import com.lovetropics.minigames.common.minigames.behaviours.BehaviorDispatcher;
import com.lovetropics.minigames.common.minigames.behaviours.BehaviorMap;
import com.lovetropics.minigames.common.minigames.behaviours.IPollingMinigameBehavior;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import com.lovetropics.minigames.common.network.LTNetwork;
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

public final class PollingMinigameInstance implements ProtoMinigame, MinigameControllable, BehaviorDispatcher<IPollingMinigameBehavior, PollingMinigameInstance> {
	private final MinecraftServer server;
	private final IMinigameDefinition definition;

	private final PlayerKey initiator;

	/**
	 * A list of players that are currently registered for the currently polling minigame.
	 */
	private final MinigameRegistrations registrations = new MinigameRegistrations();

	private final BehaviorMap behaviors;

	private final Map<String, ControlCommandHandler> controlCommands = new Object2ObjectOpenHashMap<>();

	private PollingMinigameInstance(MinecraftServer server, IMinigameDefinition definition, PlayerKey initiator) {
		this.server = server;
		this.definition = definition;
		this.behaviors = definition.createBehaviors();
		this.initiator = initiator;
	}

	public static MinigameResult<PollingMinigameInstance> create(MinecraftServer server, IMinigameDefinition definition, PlayerKey initiator) {
		PollingMinigameInstance instance = new PollingMinigameInstance(server, definition, initiator);

		MinigameResult<Unit> result = instance.dispatchToBehaviors(IPollingMinigameBehavior::onStartPolling);
		return result.mapValue(instance);
	}

	@Override
	public MinigameStatus getStatus() {
		return MinigameStatus.POLLING;
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

		String message = requestedRole != PlayerRole.SPECTATOR ? "%s has joined the %s minigame!" : "%s has joined to spectate the %s minigame!";
		broadcastMessage(new TranslationTextComponent(message, playerName, minigameName).applyTextStyle(TextFormatting.AQUA));
		LTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientJoinLeaveMessage(true));

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
		LTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientJoinLeaveMessage(false));

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
				.thenComposeAsync(map -> MinigameInstance.start(definition, server, map, behaviors, initiator, registrations), server)
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
