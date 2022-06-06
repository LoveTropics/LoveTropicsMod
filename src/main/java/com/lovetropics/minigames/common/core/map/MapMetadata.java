package com.lovetropics.minigames.common.core.map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record MapMetadata(ResourceLocation id, MapWorldSettings settings, MapRegions regions) {
	public CompoundTag write(CompoundTag root) {
		root.putString("id", this.id.toString());
		root.put("settings", this.settings.write(new CompoundTag()));
		root.put("regions", this.regions.write(new CompoundTag()));
		return root;
	}

	public static MapMetadata read(CompoundTag root) {
		ResourceLocation id = new ResourceLocation(root.getString("id"));
		MapWorldSettings settings = new MapWorldSettings();
		MapRegions regions = new MapRegions();

		settings.read(root.getCompound("settings"));
		regions.read(root.getCompound("regions"));

		return new MapMetadata(id, settings, regions);
	}
}
