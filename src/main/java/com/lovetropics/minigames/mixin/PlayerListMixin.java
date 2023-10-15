package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.common.core.game.PlayerIsolation;
import com.lovetropics.minigames.common.core.game.PlayerListAccess;
import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
	private Map<UUID, ServerStatsCounter> stats;
	@Shadow
	@Final
	private Map<UUID, PlayerAdvancements> advancements;

	@Shadow
	protected abstract void save(ServerPlayer player);

	@Shadow
	@Nullable
	public abstract CompoundTag getSingleplayerData();

	@Unique
	@Nullable
	private CompoundTag ltminigames$emptyPlayerTag;

	@Inject(method = "save", at = @At("HEAD"), cancellable = true)
	private void save(final ServerPlayer player, final CallbackInfo ci) {
		if (PlayerIsolation.INSTANCE.isIsolated(player)) {
			ci.cancel();
		}
	}

	@Unique
	private CompoundTag ltminigames$createEmptyPlayerTag() {
		final GameProfile profile = new GameProfile(Util.NIL_UUID, "null");

		final ServerPlayer player = new ServerPlayer(server, server.overworld(), profile);
		stats.remove(Util.NIL_UUID);
		advancements.remove(Util.NIL_UUID);

		final CompoundTag tag = new CompoundTag();
		player.save(tag);
		tag.remove("UUID");
		tag.remove("Pos");
		tag.put("ForgeData", new CompoundTag());

		return tag;
	}

	@Override
	public void ltminigames$clear(final ServerPlayer player) {
		if (ltminigames$emptyPlayerTag == null) {
			ltminigames$emptyPlayerTag = ltminigames$createEmptyPlayerTag();
		}

		player.setCamera(player);
		player.stopRiding();
		ltminigames$clearAttributeModifiers(player);
		player.removeAllEffects();
		player.getTags().clear();
		player.load(ltminigames$emptyPlayerTag);
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

	@Unique
	private void ltminigames$clearAttributeModifiers(final ServerPlayer player) {
		final AttributeMap attributes = player.getAttributes();
		for (final Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
			if (attributes.hasAttribute(attribute)) {
				final AttributeInstance instance = attributes.getInstance(attribute);
				if (instance != null) {
					instance.getModifiers().forEach(instance::removeModifier);
				}
			}
		}
	}
}
