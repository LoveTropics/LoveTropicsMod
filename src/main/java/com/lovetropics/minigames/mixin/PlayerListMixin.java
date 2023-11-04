package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.game.PlayerIsolation;
import com.lovetropics.minigames.common.core.game.PlayerListAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin implements PlayerListAccess {
	@Shadow
	@Final
	private MinecraftServer server;
	@Shadow
	@Final
	private List<ServerPlayer> players;
	@Shadow
	@Final
	private Map<UUID, ServerPlayer> playersByUUID;
	@Shadow
	@Final
	private PlayerDataStorage playerIo;

	@Shadow
	protected abstract void save(ServerPlayer player);

	@Shadow
	@Nullable
	public abstract CompoundTag getSingleplayerData();

	@Inject(method = "save", at = @At("HEAD"), cancellable = true)
	private void save(final ServerPlayer player, final CallbackInfo ci) {
		if (PlayerIsolation.INSTANCE.isIsolated(player)) {
			ci.cancel();
		}
	}

	@Override
	public void ltminigames$save(ServerPlayer player) {
		save(player);

		// We usually don't load the singleplayer player multiple times, so we need to overwrite this value with what we serialised
		if (server.isSingleplayerOwner(player.getGameProfile())) {
			CompoundTag loadedPlayerTag = server.getWorldData().getLoadedPlayerTag();
			CompoundTag singleplayerData = getSingleplayerData();
			if (loadedPlayerTag != null && singleplayerData != null) {
				for (String key : List.copyOf(loadedPlayerTag.getAllKeys())) {
					loadedPlayerTag.remove(key);
				}
				loadedPlayerTag.merge(singleplayerData);
			}
		}
	}

	@Override
	public void ltminigames$remove(final ServerPlayer player) {
		players.remove(player);
	}

	@Override
	public void ltminigames$add(final ServerPlayer player) {
		players.add(player);
		playersByUUID.put(player.getUUID(), player);
	}

	@Override
	public void ltminigames$firePlayerLoading(final ServerPlayer player) {
		ForgeEventFactory.firePlayerLoadingEvent(player, playerIo.getPlayerDataFolder(), player.getStringUUID());
	}
}
