package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.diguise.DisguiseType;
import com.lovetropics.minigames.common.core.diguise.ServerPlayerDisguises;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;
import net.minecraft.world.entity.EntityType;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record SetDisguiseAction(DisguiseType disguise, boolean applyDonorName) implements IGameBehavior {
	public static final MapCodec<SetDisguiseAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			DisguiseType.MAP_CODEC.forGetter(SetDisguiseAction::disguise),
			Codec.BOOL.optionalFieldOf("apply_donor_name", false).forGetter(SetDisguiseAction::applyDonorName)
	).apply(i, SetDisguiseAction::new));

	private static final ResourceLocation DUMMY_PLAYER = new ResourceLocation("dummyplayers", "dummy_player");

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			final CompletableFuture<DisguiseType> future = resolveDisguise(game, context);
			if (ServerPlayerDisguises.set(player, disguise)) {
				// This future might not complete, but we should have already
				future.thenAcceptAsync(resolvedDisguise -> applyResolvedDisguise(player, resolvedDisguise), game);
				return true;
			}
			return false;
		});
	}

	private CompletableFuture<DisguiseType> resolveDisguise(final IGamePhase game, final GameActionContext context) {
		final Optional<String> packageSender = context.get(GameActionParameter.PACKAGE_SENDER);
		final DisguiseType.EntityConfig entityDisguise = disguise.entity();
		if (entityDisguise == null) {
			return CompletableFuture.completedFuture(disguise);
		}

		final ResourceLocation id = EntityType.getKey(entityDisguise.type());
		if (applyDonorName && packageSender.isPresent() && DUMMY_PLAYER.equals(id)) {
			return resolveDummyDisguise(game, entityDisguise, packageSender.get()).thenApply(disguise::withEntity);
		}

		return CompletableFuture.completedFuture(disguise);
	}

	private CompletableFuture<DisguiseType.EntityConfig> resolveDummyDisguise(final IGamePhase game, final DisguiseType.EntityConfig entity, final String packageSender) {
		final GameProfileCache profileCache = game.getServer().getProfileCache();
		if (profileCache == null) {
			return CompletableFuture.completedFuture(entity);
		}
		final CompletableFuture<DisguiseType.EntityConfig> future = new CompletableFuture<>();
		final CompoundTag nbt = entity.nbt() != null ? entity.nbt().copy() : new CompoundTag();
		nbt.putString("ProfileName", packageSender);
		profileCache.getAsync(packageSender, result -> result.ifPresent(profile -> {
			nbt.putUUID("ProfileID", profile.getId());
			future.complete(entity.withNbt(nbt));
		}));
		return future;
	}

	private void applyResolvedDisguise(final ServerPlayer player, final DisguiseType resolvedDisguise) {
		if (resolvedDisguise != disguise) {
			ServerPlayerDisguises.update(player, playerDisguise -> {
				if (playerDisguise.type() == disguise) {
					playerDisguise.set(resolvedDisguise);
				}
			});
		}
	}
}
