package com.lovetropics.minigames.common.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public record EntityTemplate(EntityType<?> type, CompoundTag tag) {
	public static final Codec<EntityTemplate> CODEC = RecordCodecBuilder.create(i -> i.group(
			ForgeRegistries.ENTITIES.getCodec().fieldOf("entity").forGetter(EntityTemplate::type),
			CompoundTag.CODEC.optionalFieldOf("tag", new CompoundTag()).forGetter(EntityTemplate::tag)
	).apply(i, EntityTemplate::new));

	@Nullable
	public Entity spawn(ServerLevel level, double x, double y, double z, float yRot, float xRot) {
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
}
