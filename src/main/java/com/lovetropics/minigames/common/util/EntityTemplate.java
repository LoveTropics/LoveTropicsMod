package com.lovetropics.minigames.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public record EntityTemplate(EntityType<?> type, CompoundTag tag) {
	public static final Codec<EntityTemplate> CODEC = RecordCodecBuilder.create(i -> i.group(
			ForgeRegistries.ENTITIES.getCodec().fieldOf("type").forGetter(EntityTemplate::type),
			CompoundTag.CODEC.optionalFieldOf("tag", new CompoundTag()).forGetter(EntityTemplate::tag)
	).apply(i, EntityTemplate::new));

	@Nullable
	public Entity spawn(ServerLevel level, double x, double y, double z, float yRot, float xRot) {
		if (type == EntityType.LIGHTNING_BOLT) {
			return spawnLightningBolt(level, x, y, z);
		}

		CompoundTag tag = this.tag.copy();
		tag.putString("id", type.getRegistryName().toString());

		Entity entity = EntityType.loadEntityRecursive(tag, level, e -> {
			e.moveTo(x, y, z, yRot, xRot);
			return e;
		});

		if (entity != null) {
			if (entity instanceof Mob mob) {
				mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null);
			}
			return level.tryAddFreshEntityWithPassengers(entity) ? entity : null;
		}

		return null;
	}

	@Nullable
	private static LightningBolt spawnLightningBolt(ServerLevel level, double x, double y, double z) {
		LightningBolt entity = EntityType.LIGHTNING_BOLT.create(level);
		if (entity != null) {
			entity.moveTo(new Vec3(x, y, z));
			level.addFreshEntity(entity);
			return entity;
		}
		return null;
	}
}
