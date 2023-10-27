package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;

import java.util.Optional;

public record DonationPackageData(
		String id,
		PackageType packageType,
		Category category,
		PackagePlayerSelect playerSelect,
		Component name,
		Component description,
		Optional<Double> donationAmount
) {
	public static final MapCodec<DonationPackageData> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.fieldOf("id").forGetter(DonationPackageData::id),
			PackageType.CODEC.fieldOf("package_type").forGetter(DonationPackageData::packageType),
			Category.CODEC.fieldOf("category").orElse(Category.OTHER).forGetter(DonationPackageData::category),
			PackagePlayerSelect.CODEC.fieldOf("player_select").orElse(PackagePlayerSelect.RANDOM).forGetter(DonationPackageData::playerSelect),
			ExtraCodecs.COMPONENT.fieldOf("name").forGetter(DonationPackageData::name),
			ExtraCodecs.COMPONENT.fieldOf("description").forGetter(DonationPackageData::description),
			Codec.DOUBLE.optionalFieldOf("donation_amount").forGetter(DonationPackageData::donationAmount)
	).apply(i, DonationPackageData::new));

	public enum PackageType implements StringRepresentable {
		CARE_PACKAGE("care_package"),
		SABOTAGE_PACKAGE("sabotage_package"),
		CHAT_EVENT("chat_event"),
		;

		public static final Codec<PackageType> CODEC = StringRepresentable.fromEnum(PackageType::values);

		private final String key;

		PackageType(String key) {
			this.key = key;
		}

		@Override
		public String getSerializedName() {
			return key;
		}
	}

	public enum Category implements StringRepresentable {
		LARGE("large"),
		MEDIUM("medium"),
		SMALL("small"),
		OTHER("other"),
		;

		public static final Codec<Category> CODEC = StringRepresentable.fromEnum(Category::values);

		private final String key;

		Category(String key) {
			this.key = key;
		}

		@Override
		public String getSerializedName() {
			return key;
		}
	}
}
