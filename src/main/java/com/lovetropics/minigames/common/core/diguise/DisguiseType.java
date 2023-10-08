package com.lovetropics.minigames.common.core.diguise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public record DisguiseType(@Nullable EntityConfig entity, float scale) {
	public static final DisguiseType DEFAULT = new DisguiseType((EntityConfig) null, 1.0f);

	public static final Codec<DisguiseType> CODEC = RecordCodecBuilder.create(i -> i.group(
			EntityConfig.CODEC.optionalFieldOf("entity").forGetter(c -> Optional.ofNullable(c.entity)),
			Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(c -> c.scale)
	).apply(i, DisguiseType::new));

	private DisguiseType(Optional<EntityConfig> entity, float scale) {
		this(entity.orElse(null), scale);
	}

	@Nullable
	public Entity createEntityFor(Player player) {
		Entity entity = createEntity(player.level());
		if (entity == null) {
			return null;
		}

		if (!entity.hasCustomName()) {
			entity.setCustomName(player.getDisplayName());
		}
		entity.setCustomNameVisible(true);

		return entity;
	}

	@Nullable
	public Entity createEntity(Level level) {
		if (entity == null) {
			return null;
		}

		EntityType<?> type = entity.type();
		CompoundTag nbt = entity.nbt();
		Entity entity = type.create(level);
		if (entity == null) {
			return null;
		}

		if (nbt != null) {
			entity.load(nbt);
		}

		fixInvalidEntity(entity);
		return entity;
	}

	private void fixInvalidEntity(Entity entity) {
		entity.stopRiding();
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeNullable(entity, (b, e) -> e.encode(b));
		buffer.writeFloat(scale);
	}

	public static DisguiseType decode(FriendlyByteBuf buffer) {
		EntityConfig entity = buffer.readNullable(EntityConfig::decode);
		float scale = buffer.readFloat();
		return new DisguiseType(entity, scale);
	}

	public boolean isDefault() {
		return equals(DEFAULT);
	}

	@Nullable
	public EntityType<?> entityType() {
		return entity != null ? entity.type : null;
	}

	public DisguiseType clear(DisguiseType other) {
		DisguiseType result = this;
		if (Objects.equals(entity, other.entity)) {
			result = result.withEntity(null);
		}
		if (scale == other.scale) {
			result = result.withScale(1.0f);
		}
		return result;
	}

	public DisguiseType withEntity(@Nullable EntityConfig entity) {
		return new DisguiseType(entity, scale);
	}

	public DisguiseType withScale(float scale) {
		return new DisguiseType(entity, scale);
	}

	public record EntityConfig(EntityType<?> type, @Nullable CompoundTag nbt, boolean applyAttributes) {
		public static final Codec<EntityConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("type").forGetter(c -> c.type),
				CompoundTag.CODEC.optionalFieldOf("tag").forGetter(c -> Optional.ofNullable(c.nbt)),
				Codec.BOOL.optionalFieldOf("apply_attributes", true).forGetter(c -> c.applyAttributes)
		).apply(i, EntityConfig::new));

		private EntityConfig(EntityType<?> type, Optional<CompoundTag> nbt, boolean applyAttributes) {
			this(type, nbt.orElse(null), applyAttributes);
		}

		public EntityConfig withNbt(@Nullable CompoundTag nbt) {
			return new EntityConfig(type, nbt, applyAttributes);
		}

		public void encode(FriendlyByteBuf buffer) {
			buffer.writeRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES, type);
			buffer.writeNbt(nbt);
			buffer.writeBoolean(applyAttributes);
		}

		public static EntityConfig decode(FriendlyByteBuf buffer) {
			EntityType<?> type = buffer.readRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES);
			CompoundTag nbt = buffer.readNbt();
			boolean applyAttributes = buffer.readBoolean();
			return new EntityConfig(type, nbt, applyAttributes);
		}
	}
}
