package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant;

import com.mojang.serialization.Codec;

public final class PlantType {
	public static final Codec<PlantType> CODEC = Codec.STRING.xmap(PlantType::new, PlantType::id);

	private final String id;

	public PlantType(String id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;

		PlantType type = (PlantType) obj;
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
