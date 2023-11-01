package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.util.LTGameTestFakePlayer;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;
import java.util.Objects;
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
		final PlayerListAccess playerList = (PlayerListAccess) player.getServer().getPlayerList();
		if (!isIsolated(player)) {
			playerList.ltminigames$save(player);
		}
		final TransferableState transferableState = TransferableState.copyOf(player);
		return reloadPlayer(player, newPlayer -> {
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
			final CompoundTag playerTag = playerList.load(newPlayer);

			final ResourceKey<Level> dimensionKey = playerTag != null ? getPlayerDimension(playerTag) : Level.OVERWORLD;
			final ServerLevel newLevel = Objects.requireNonNullElse(server.getLevel(dimensionKey), server.overworld());
			newPlayer.setServerLevel(newLevel);

			newPlayer.loadGameTypes(playerTag);
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
		ForgeEventFactory.firePlayerLoggedOut(oldPlayer);

		oldPlayer.unRide();
		oldPlayer.serverLevel().removePlayerImmediately(oldPlayer, Entity.RemovalReason.DISCARDED);
		((PlayerListAccess) playerList).ltminigames$remove(oldPlayer);

		final ServerPlayer newPlayer = recreatePlayer(oldPlayer);
		initializer.accept(newPlayer);
		newPlayer.onUpdateAbilities();

		final ServerLevel newLevel = newPlayer.serverLevel();
		final LevelData levelData = newLevel.getLevelData();
		newPlayer.connection.send(new ClientboundRespawnPacket(
				newLevel.dimensionTypeId(),
				newLevel.dimension(),
				BiomeManager.obfuscateSeed(newLevel.getSeed()),
				newPlayer.gameMode.getGameModeForPlayer(),
				newPlayer.gameMode.getPreviousGameModeForPlayer(),
				newLevel.isDebug(),
				newLevel.isFlat(),
				(byte) 0,
				newPlayer.getLastDeathLocation(),
				newPlayer.getPortalCooldown()
		));
		newPlayer.connection.teleport(newPlayer.getX(), newPlayer.getY(), newPlayer.getZ(), newPlayer.getYRot(), newPlayer.getXRot());
		newPlayer.connection.send(new ClientboundSetDefaultSpawnPositionPacket(newLevel.getSharedSpawnPos(), newLevel.getSharedSpawnAngle()));
		newPlayer.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));

		newLevel.addRespawnedPlayer(newPlayer);

		playerList.sendPlayerPermissionLevel(newPlayer);
		playerList.sendLevelInfo(newPlayer, newLevel);
		playerList.sendAllPlayerInfo(newPlayer);

		newPlayer.initInventoryMenu();
		newPlayer.setHealth(newPlayer.getHealth());
		newPlayer.connection.send(new ClientboundSetHealthPacket(newPlayer.getHealth(), newPlayer.getFoodData().getFoodLevel(), newPlayer.getFoodData().getSaturationLevel()));

		((PlayerListAccess) playerList).ltminigames$add(newPlayer);

		playerList.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, newPlayer));

		ForgeEventFactory.firePlayerLoggedIn(newPlayer);
		final ResourceKey<Level> oldDimension = oldPlayer.level().dimension();
		final ResourceKey<Level> newDimension = newLevel.dimension();
		if (oldDimension != newDimension) {
			ForgeEventFactory.firePlayerChangedDimensionEvent(newPlayer, oldDimension, newDimension);
		}

		reloadingPlayers.remove(newPlayer.getUUID());

		return newPlayer;
	}

	private static ServerPlayer recreatePlayer(final ServerPlayer oldPlayer) {
		final ServerPlayer newPlayer = new ServerPlayer(oldPlayer.server, oldPlayer.serverLevel(), oldPlayer.getGameProfile());
		newPlayer.connection = oldPlayer.connection;
		newPlayer.connection.player = newPlayer;
		newPlayer.setId(oldPlayer.getId());
		if (oldPlayer.getChatSession() != null) {
			newPlayer.setChatSession(oldPlayer.getChatSession());
		}
		restoreClientOptions(oldPlayer, newPlayer);

		return newPlayer;
	}

	private static void restoreClientOptions(final ServerPlayer oldPlayer, final ServerPlayer newPlayer) {
		int skinOptions = 0;
		for (final PlayerModelPart part : PlayerModelPart.values()) {
			if (oldPlayer.isModelPartShown(part)) {
				skinOptions |= part.getMask();
			}
		}
		newPlayer.updateOptions(new ServerboundClientInformationPacket(
				oldPlayer.getLanguage(),
				0,
				oldPlayer.getChatVisibility(),
				oldPlayer.canChatInColor(),
				skinOptions,
				oldPlayer.getMainArm(),
				oldPlayer.isTextFilteringEnabled(),
				oldPlayer.allowsListing()
		));
	}

	private static ResourceKey<Level> getPlayerDimension(final CompoundTag playerTag) {
		final Tag dimensionTag = playerTag.get("Dimension");
		return DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, dimensionTag)).result().orElse(Level.OVERWORLD);
	}

	public boolean isIsolated(final ServerPlayer player) {
		return player.getTags().contains(ISOLATED_TAG);
	}

	public boolean isReloading(final ServerPlayer player) {
		return reloadingPlayers.contains(player.getUUID());
	}

	// State that can be transferred into isolation, but not back out
	private record TransferableState(
			// We use tags in the datapack to track whether a player is joining for the first time
			List<String> tags
	) {
		public static TransferableState copyOf(final ServerPlayer player) {
			return new TransferableState(List.copyOf(player.getTags()));
		}

		public void restore(final ServerPlayer player) {
			tags.forEach(player::addTag);
		}
	}
}
