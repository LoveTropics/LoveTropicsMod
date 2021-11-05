package com.lovetropics.minigames.common.core.integration.game_actions;

import com.google.common.base.Strings;
import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public final class GamePackage {
	public static final MapCodec<GamePackage> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("package_type").forGetter(c -> c.packageType),
				Codec.STRING.optionalFieldOf("sending_player_name", "").forGetter(c -> c.sendingPlayerName),
				MoreCodecs.UUID_STRING.optionalFieldOf("receiving_player").forGetter(c -> Optional.ofNullable(c.receivingPlayer))
		).apply(instance, GamePackage::new);
	});

	private final String packageType;
	private final String sendingPlayerName;
	@Nullable
	private final UUID receivingPlayer;

	private GamePackage(String packageType, String sendingPlayerName, Optional<UUID> receivingPlayer) {
		this(packageType, sendingPlayerName, receivingPlayer.orElse(null));
	}

	public GamePackage(String packageType, String sendingPlayerName, @Nullable UUID receivingPlayer) {
		this.packageType = packageType;
		this.sendingPlayerName = sendingPlayerName;
		this.receivingPlayer = receivingPlayer;
	}

	public String getPackageType() {
		return packageType;
	}

	@Nullable
	public String getSendingPlayerName() {
		return !Strings.isNullOrEmpty(sendingPlayerName) ? sendingPlayerName : null;
	}

	@Nullable
	public UUID getReceivingPlayer() {
		return receivingPlayer;
	}
}
