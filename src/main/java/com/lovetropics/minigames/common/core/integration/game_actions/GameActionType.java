package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.minigames.common.config.ConfigLT;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.SharedConstants;

import java.util.Optional;
import java.util.function.Supplier;

public enum GameActionType {
	DONATION("donation", "payment_time", DonationGameAction.CODEC, ConfigLT.GENERAL.donationPackageDelay, false),
	DONATION_PACKAGE("donation_package", "trigger_time", DonationPackageGameAction.CODEC, ConfigLT.GENERAL.donationPackageDelay, true),
	CHAT_EVENT("chat_event", "trigger_time", ChatEventGameAction.CODEC, ConfigLT.GENERAL.chatEventDelay, true);

	public static final GameActionType[] VALUES = values();

	private final String id;
	private final String timeFieldName;
	private final Codec<GameActionRequest> codec;
	private final Supplier<Integer> pollingIntervalSeconds;
	private final boolean sendsAcknowledgement;

	@SuppressWarnings("unchecked")
	GameActionType(final String id, String timeFieldName, final MapCodec<? extends GameAction> codec, final Supplier<Integer> pollingIntervalTicks, final boolean sendsAcknowledgement) {
		this.id = id;
		this.timeFieldName = timeFieldName;
		this.codec = GameActionRequest.codec(this, (MapCodec<GameAction>) codec);
		pollingIntervalSeconds = pollingIntervalTicks;
		this.sendsAcknowledgement = sendsAcknowledgement;
	}

	public String getId() {
		return id;
	}

	public String getTimeFieldName() {
		return timeFieldName;
	}

	public Codec<GameActionRequest> getCodec() {
		return codec;
	}

	public int getPollingIntervalSeconds() {
		return pollingIntervalSeconds.get();
	}

	public int getPollingIntervalTicks() {
		return getPollingIntervalSeconds() * SharedConstants.TICKS_PER_SECOND;
	}

	public boolean sendsAcknowledgement() {
		return sendsAcknowledgement;
	}

	public static Optional<GameActionType> getFromId(final String id) {
		for (final GameActionType request : VALUES) {
			if (request.getId().equals(id)) {
				return Optional.of(request);
			}
		}

		return Optional.empty();
	}
}
