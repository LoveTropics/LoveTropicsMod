package com.lovetropics.minigames.common.minigames.statistics;

import com.google.gson.JsonObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

import javax.annotation.Nullable;

public final class CauseOfDeath {
	public final String type;
	public final String typeName;
	public final String source;
	public final String sourceName;

	public CauseOfDeath(String type, String typeName, @Nullable String source, @Nullable String sourceName) {
		this.type = type;
		this.typeName = typeName;
		this.source = source;
		this.sourceName = sourceName;
	}

	public static CauseOfDeath from(DamageSource damage) {
		String type = damage.damageType;
		String typeName = getDamageTypeName(damage);

		String source = null;
		String sourceName = null;

		Entity entitySource = damage.getTrueSource();
		if (entitySource != null) {
			EntityType<?> sourceType = entitySource.getType();
			source = EntityType.getKey(sourceType).toString();
			sourceName = sourceType.getName().getString();
		}

		return new CauseOfDeath(type, typeName, source, sourceName);
	}

	public JsonObject serialize() {
		JsonObject root = new JsonObject();
		root.addProperty("type", type);
		root.addProperty("type_name", typeName);

		if (source != null) {
			root.addProperty("source", source);
			root.addProperty("source_name", sourceName);
		}

		return root;
	}

	private static String getDamageTypeName(DamageSource damage) {
		if (damage == DamageSource.IN_FIRE) return "went up in flames";
		else if (damage == DamageSource.LIGHTNING_BOLT) return "was struck by lightning";
		else if (damage == DamageSource.ON_FIRE) return "burned to death";
		else if (damage == DamageSource.LAVA) return "tried to swim in lava";
		else if (damage == DamageSource.HOT_FLOOR) return "discovered the floor was lava";
		else if (damage == DamageSource.IN_WALL) return "suffocated in a wall";
		else if (damage == DamageSource.CRAMMING) return "squished too much";
		else if (damage == DamageSource.DROWN) return "drowned";
		else if (damage == DamageSource.STARVE) return "starved to death";
		else if (damage == DamageSource.CACTUS) return "pricked to death";
		else if (damage == DamageSource.FALL) return "hit the ground too hard";
		else if (damage == DamageSource.FLY_INTO_WALL) return "experienced kinetic energy";
		else if (damage == DamageSource.OUT_OF_WORLD) return "fell out of the world";
		else if (damage == DamageSource.MAGIC) return "killed by magic";
		else if (damage == DamageSource.WITHER) return "withered to death";
		else if (damage == DamageSource.ANVIL) return "squashed by a falling anvil";
		else if (damage == DamageSource.FALLING_BLOCK) return "squashed by a falling block";
		else if (damage == DamageSource.DRAGON_BREATH) return "roasted in dragon breath";
		else if (damage == DamageSource.SWEET_BERRY_BUSH) return "poked to death by a sweet berry bush";
		else if (damage instanceof EntityDamageSource) return "was slain";

		return "died";
	}
}
