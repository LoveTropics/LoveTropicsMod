package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record DonationPackageData(
		String packageType,
		Optional<DonationPackageNotification> notification,
		DonationPackageBehavior.PlayerSelect playerSelect
) {
	public static final MapCodec<DonationPackageData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("package_type").forGetter(DonationPackageData::packageType),
			DonationPackageNotification.CODEC.optionalFieldOf("notification").forGetter(DonationPackageData::notification),
			DonationPackageBehavior.PlayerSelect.CODEC.optionalFieldOf("player_select", DonationPackageBehavior.PlayerSelect.RANDOM).forGetter(DonationPackageData::playerSelect)
	).apply(i, DonationPackageData::new));
}
