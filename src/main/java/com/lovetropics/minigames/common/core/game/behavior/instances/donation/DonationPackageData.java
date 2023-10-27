package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record DonationPackageData(
		String packageType,
		PackagePlayerSelect playerSelect
) {
	public static final MapCodec<DonationPackageData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("package_type").forGetter(DonationPackageData::packageType),
			PackagePlayerSelect.CODEC.optionalFieldOf("player_select", PackagePlayerSelect.RANDOM).forGetter(DonationPackageData::playerSelect)
	).apply(i, DonationPackageData::new));
}
