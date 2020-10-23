package com.lovetropics.minigames.common.minigames.statistics;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;

public final class CauseOfDeath {
	public final String type;
	public final String source;

	public CauseOfDeath(String type, @Nullable String source) {
		this.type = type;
		this.source = source;
	}

	public static CauseOfDeath from(DamageSource damage) {
		String source = null;

		Entity entitySource = damage.getTrueSource();
		if (entitySource != null) {
			source = EntityType.getKey(entitySource.getType()).toString();
		}

		return new CauseOfDeath(damage.damageType, source);
	}

	public JsonObject serialize() {
		JsonObject root = new JsonObject();
		root.addProperty("type", type);
		if (source != null) {
			root.addProperty("source", source);
		}

		return root;
	}
}
