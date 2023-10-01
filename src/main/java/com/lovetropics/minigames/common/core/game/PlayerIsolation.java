package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.Constants;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class PlayerIsolation {
	public static final PlayerIsolation INSTANCE = new PlayerIsolation();

	private final Set<UUID> isolatedPlayers = new ObjectOpenHashSet<>();
	private final Set<UUID> pendingIsolation = new ObjectOpenHashSet<>();

	private PlayerIsolation() {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onServerTick(final TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			INSTANCE.finishTick(event.getServer());
		}
	}

	@SubscribeEvent
	public static void onPlayerChangeDimension(final PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof final ServerPlayer player) {
			INSTANCE.onPlayerChangeDimension(player);
		}
	}

	private void finishTick(final MinecraftServer server) {
		if (pendingIsolation.isEmpty()) {
			return;
		}
		final PlayerList playerList = server.getPlayerList();
		for (final UUID playerId : pendingIsolation) {
			final ServerPlayer player = playerList.getPlayer(playerId);
			if (player != null) {
				reloadPlayer(player, player.serverLevel());
			}
		}
		pendingIsolation.clear();
	}

	private void onPlayerChangeDimension(final ServerPlayer player) {
		if (pendingIsolation.remove(player.getUUID())) {
			final PlayerList playerList = player.getServer().getPlayerList();
			playerList.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, player));
		}
	}

	public void isolateAndClear(final ServerPlayer player) {
		final PlayerListAccess playerList = (PlayerListAccess) player.getServer().getPlayerList();
		if (isolatedPlayers.add(player.getUUID())) {
			playerList.ltminigames$save(player);
		}
		playerList.ltminigames$clear(player);
		pendingIsolation.add(player.getUUID());
	}

	public void restore(final ServerPlayer player) {
		if (isolatedPlayers.remove(player.getUUID())) {
			pendingIsolation.remove(player.getUUID());
			reloadPlayerFromDisk(player);
		}
	}

	private static void reloadPlayerFromDisk(final ServerPlayer player) {
		final MinecraftServer server = player.getServer();
		final PlayerList playerList = server.getPlayerList();

		((PlayerListAccess) playerList).ltminigames$clear(player);

		final CompoundTag playerTag = playerList.load(player);
		final ResourceKey<Level> dimensionKey = playerTag != null ? getPlayerDimension(playerTag) : Level.OVERWORLD;

		final ServerLevel newLevel = Objects.requireNonNullElse(server.getLevel(dimensionKey), server.overworld());
		player.loadGameTypes(playerTag);

		reloadPlayer(player, newLevel);
	}

	private static void reloadPlayer(final ServerPlayer player, final ServerLevel newLevel) {
		final MinecraftServer server = player.getServer();
		final PlayerList playerList = server.getPlayerList();

		final ServerLevel oldLevel = player.serverLevel();

		final LevelData levelData = newLevel.getLevelData();
		player.connection.send(new ClientboundRespawnPacket(
				newLevel.dimensionTypeId(),
				newLevel.dimension(),
				BiomeManager.obfuscateSeed(newLevel.getSeed()),
				player.gameMode.getGameModeForPlayer(),
				player.gameMode.getPreviousGameModeForPlayer(),
				newLevel.isDebug(),
				newLevel.isFlat(),
				ClientboundRespawnPacket.KEEP_ALL_DATA,
				player.getLastDeathLocation(),
				player.getPortalCooldown()
		));
		player.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));

		oldLevel.removePlayerImmediately(player, Entity.RemovalReason.CHANGED_DIMENSION);
		player.revive();
		player.setServerLevel(newLevel);
		newLevel.addDuringCommandTeleport(player);

		playerList.sendPlayerPermissionLevel(player);
		player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
		playerList.sendLevelInfo(player, newLevel);
		playerList.sendAllPlayerInfo(player);

		playerList.broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_GAME_MODE, player));
	}

	private static ResourceKey<Level> getPlayerDimension(final CompoundTag playerTag) {
		final Tag dimensionTag = playerTag.get("Dimension");
		return DimensionType.parseLegacy(new Dynamic<>(NbtOps.INSTANCE, dimensionTag)).result().orElse(Level.OVERWORLD);
	}

	public boolean isIsolated(final ServerPlayer player) {
		return isolatedPlayers.contains(player.getUUID());
	}
}
