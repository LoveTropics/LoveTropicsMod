package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.util.LTGameTestFakePlayer;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class PlayerIsolation {
	public static final PlayerIsolation INSTANCE = new PlayerIsolation();

	private static final String ISOLATED_TAG = Constants.MODID + ".isolated";

	private final Set<UUID> reloadingPlayers = new ObjectOpenHashSet<>();

	private PlayerIsolation() {
	}

	public ServerPlayer teleportTo(final ServerPlayer player, final ServerLevel newLevel, final Vec3 position, final float yRot, final float xRot) {
		final TransferableState transferableState = TransferableState.copyOf(player);
		return reloadPlayer(player, newPlayer -> {
			((PlayerListAccess) newPlayer.server.getPlayerList()).ltminigames$firePlayerLoading(newPlayer);
			newPlayer.setServerLevel(newLevel);
			newPlayer.moveTo(position.x, position.y, position.z, yRot, xRot);
			newPlayer.addTag(ISOLATED_TAG);
			transferableState.restore(newPlayer);
		});
	}

	public ServerPlayer restore(final ServerPlayer player) {
		if (isIsolated(player)) {
			return reloadPlayerFromDisk(player);
		}
		return player;
	}

	private ServerPlayer reloadPlayerFromDisk(final ServerPlayer player) {
		return reloadPlayer(player, newPlayer -> {
			final MinecraftServer server = player.getServer();
			final PlayerList playerList = server.getPlayerList();
			final Optional<CompoundTag> playerTag = playerList.load(newPlayer);

			final ResourceKey<Level> dimensionKey = playerTag.isPresent() ? getPlayerDimension(playerTag.get()) : Level.OVERWORLD;
			final ServerLevel newLevel = Objects.requireNonNullElse(server.getLevel(dimensionKey), server.overworld());
			newPlayer.setServerLevel(newLevel);

			playerTag.ifPresent(newPlayer::loadGameTypes);
		});
	}

	private ServerPlayer reloadPlayer(final ServerPlayer oldPlayer, final Consumer<ServerPlayer> initializer) {
		if (oldPlayer instanceof LTGameTestFakePlayer) {
			initializer.accept(oldPlayer);
			return oldPlayer;
		}

		final MinecraftServer server = oldPlayer.getServer();
		final PlayerList playerList = server.getPlayerList();

		reloadingPlayers.add(oldPlayer.getUUID());
		EventHooks.firePlayerLoggedOut(oldPlayer);

		if (!isIsolated(oldPlayer)) {
			((PlayerListAccess) playerList).ltminigames$save(oldPlayer);
		}

		oldPlayer.unRide();
		oldPlayer.serverLevel().removePlayerImmediately(oldPlayer, Entity.RemovalReason.DISCARDED);
		((PlayerListAccess) playerList).ltminigames$remove(oldPlayer);

		final ServerPlayer newPlayer = recreatePlayer(oldPlayer);
		initializer.accept(newPlayer);
		newPlayer.onUpdateAbilities();

		final ServerLevel newLevel = newPlayer.serverLevel();
		final LevelData levelData = newLevel.getLevelData();
		newPlayer.connection.send(new ClientboundRespawnPacket(
				newPlayer.createCommonSpawnInfo(newLevel),
				(byte) 0
		));
		newPlayer.connection.teleport(newPlayer.getX(), newPlayer.getY(), newPlayer.getZ(), newPlayer.getYRot(), newPlayer.getXRot());
		newPlayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(newLevel.getSharedSpawnPos(), newLevel.getSharedSpawnAngle()));
		newPlayer.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));

		sendGameRules(newPlayer, newLevel.getGameRules());

		newLevel.addRespawnedPlayer(newPlayer);

		playerList.sendPlayerPermissionLevel(newPlayer);
		playerList.sendLevelInfo(newPlayer, newLevel);
		playerList.sendAllPlayerInfo(newPlayer);

		newPlayer.initInventoryMenu();
		newPlayer.setHealth(newPlayer.getHealth());
		newPlayer.connection.send(new ClientboundSetHealthPacket(newPlayer.getHealth(), newPlayer.getFoodData().getFoodLevel(), newPlayer.getFoodData().getSaturationLevel()));

		((PlayerListAccess) playerList).ltminigames$add(newPlayer);

		playerList.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, newPlayer));

		EventHooks.firePlayerLoggedIn(newPlayer);
		final ResourceKey<Level> oldDimension = oldPlayer.level().dimension();
		final ResourceKey<Level> newDimension = newLevel.dimension();
		if (oldDimension != newDimension) {
			EventHooks.firePlayerChangedDimensionEvent(newPlayer, oldDimension, newDimension);
		}

		reloadingPlayers.remove(newPlayer.getUUID());

		return newPlayer;
	}

	private static ServerPlayer recreatePlayer(final ServerPlayer oldPlayer) {
		final ServerPlayer newPlayer = new ServerPlayer(oldPlayer.server, oldPlayer.serverLevel(), oldPlayer.getGameProfile(), oldPlayer.clientInformation());
		newPlayer.connection = oldPlayer.connection;
		newPlayer.connection.player = newPlayer;
		newPlayer.setId(oldPlayer.getId());
		if (oldPlayer.getChatSession() != null) {
			newPlayer.setChatSession(oldPlayer.getChatSession());
		}
		newPlayer.updateOptions(oldPlayer.clientInformation());

		return newPlayer;
	}

	private static ResourceKey<Level> getPlayerDimension(final CompoundTag playerTag) {
		final Tag dimensionTag = playerTag.get("Dimension");
		return DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, dimensionTag)).result().orElse(Level.OVERWORLD);
	}

	private static void sendGameRules(final ServerPlayer player, final GameRules gameRules) {
		final boolean immediateRespawn = gameRules.getRule(GameRules.RULE_DO_IMMEDIATE_RESPAWN).get();
		player.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.IMMEDIATE_RESPAWN, immediateRespawn ? 1.0f : 0.0F));
	}

	public boolean isIsolated(final ServerPlayer player) {
		return player.getTags().contains(ISOLATED_TAG);
	}

	public boolean isReloading(final ServerPlayer player) {
		return reloadingPlayers.contains(player.getUUID());
	}

	// State that can be transferred into isolation, but not back out
	private record TransferableState(
	) {
		public static TransferableState copyOf(final ServerPlayer player) {
			return new TransferableState();
		}

		public void restore(final ServerPlayer player) {
		}
	}
}
