package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.base.Strings;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.integration.game_actions.Donation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DonationThresholdBehavior(double threshold, GameActionList actions) implements IGameBehavior {
	public static final Codec<DonationThresholdBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.DOUBLE.fieldOf("threshold").forGetter(DonationThresholdBehavior::threshold),
			GameActionList.CODEC.fieldOf("actions").forGetter(DonationThresholdBehavior::actions)
	).apply(i, DonationThresholdBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		actions.register(game, events);

		events.listen(GamePackageEvents.RECEIVE_DONATION, donation -> {
			if (donation.amount() >= threshold) {
				GameActionContext context = actionContext(donation);
				actions.applyPlayer(game, context);
			}
		});
	}

	private static GameActionContext actionContext(Donation donation) {
		GameActionContext.Builder context = GameActionContext.builder();
		if (!Strings.isNullOrEmpty(donation.name()) && !donation.anonymous()) {
			context.set(GameActionParameter.PACKAGE_SENDER, donation.name());
		}
		return context.build();
	}
}
