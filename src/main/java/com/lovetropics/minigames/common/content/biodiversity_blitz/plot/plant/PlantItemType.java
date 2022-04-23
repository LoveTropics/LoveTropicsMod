package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant;

import com.mojang.serialization.Codec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public final class PlantItemType {
	public static final Codec<PlantItemType> CODEC = Codec.STRING.xmap(PlantItemType::new, PlantItemType::id);

	private static final String ITEM_NBT_KEY = "ltminigames:plant";

	private final String id;

	public PlantItemType(String id) {
		this.id = id;
	}

	public void applyTo(ItemStack item) {
		item.getOrCreateTag().putString(ITEM_NBT_KEY, this.id);
	}

	public boolean matches(ItemStack item) {
		if (item.isEmpty()) {
			return false;
		}

		CompoundTag tag = item.getTag();
		return tag != null && this.id.equals(tag.getString(ITEM_NBT_KEY));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		PlantItemType type = (PlantItemType) obj;
		return id.equals(type.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	public String id() {
		return id;
	}
}
