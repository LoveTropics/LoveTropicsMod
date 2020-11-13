package com.lovetropics.minigames.common.game_actions;

import javax.annotation.Nullable;
import java.util.UUID;

public final class GamePackage {
	private final String packageType;
	@Nullable
	private final String sendingPlayerName;
	@Nullable
	private final UUID receivingPlayer;

	public GamePackage(String packageType, @Nullable String sendingPlayerName, @Nullable UUID receivingPlayer) {
		this.packageType = packageType;
		this.sendingPlayerName = sendingPlayerName;
		this.receivingPlayer = receivingPlayer;
	}

	public String getPackageType() {
		return packageType;
	}

	@Nullable
	public String getSendingPlayerName() {
		return sendingPlayerName;
	}

	@Nullable
	public UUID getReceivingPlayer() {
		return receivingPlayer;
	}
}
