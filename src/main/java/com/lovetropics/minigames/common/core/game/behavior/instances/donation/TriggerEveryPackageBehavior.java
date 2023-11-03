package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.GamePackageState;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public record TriggerEveryPackageBehavior(Set<String> exclude) implements IGameBehavior {
	public static final MapCodec<TriggerEveryPackageBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf).fieldOf("exclude").forGetter(TriggerEveryPackageBehavior::exclude)
	).apply(i, TriggerEveryPackageBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final GamePackageState packages = game.getState().get(GamePackageState.KEY);
		events.listen(GameActionEvents.APPLY, context -> {
			final GamePackage sourcePackage = context.get(GameActionParameter.PACKAGE).orElse(null);
			if (sourcePackage == null) {
				return false;
			}

			boolean applied = false;
			for (final DonationPackageData donationPackage : packages.packages()) {
				if (exclude.contains(donationPackage.id()) || donationPackage.id().equals(sourcePackage.packageType())) {
					continue;
				}
				applied |= triggerPackage(game, donationPackage, sourcePackage) == InteractionResult.SUCCESS;
			}

			return applied;
		});
	}

	private static InteractionResult triggerPackage(final IGamePhase game, final DonationPackageData packageData, final GamePackage sourcePackage) {
		final Optional<UUID> targetPlayer = switch (packageData.playerSelect()) {
			case SPECIFIC -> {
				final List<ServerPlayer> participants = Lists.newArrayList(game.getParticipants());
				yield Util.getRandomSafe(participants, game.getRandom()).map(Entity::getUUID);
			}
			default -> Optional.empty();
		};

		final GamePackage gamePackage = new GamePackage(packageData.id(), sourcePackage.sendingPlayerName(), targetPlayer);
		return game.invoker(GamePackageEvents.RECEIVE_PACKAGE).onReceivePackage(gamePackage);
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.TRIGGER_EVERY_PACKAGE;
	}
}
