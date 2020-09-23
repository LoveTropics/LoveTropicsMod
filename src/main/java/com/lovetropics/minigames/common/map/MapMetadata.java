package com.lovetropics.minigames.common.map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;

public final class MapMetadata {
	public final ResourceLocation id;
	public final MapWorldSettings settings;
	public final MapRegions regions;

	public MapMetadata(ResourceLocation id, MapWorldSettings settings, MapRegions regions) {
		this.id = id;
		this.settings = settings;
		this.regions = regions;
	}

	public CompoundNBT write(CompoundNBT root) {
		root.putString("id", this.id.toString());
		root.put("settings", this.settings.write(new CompoundNBT()));
		root.put("regions", this.regions.write(new CompoundNBT()));
		return root;
	}
}
