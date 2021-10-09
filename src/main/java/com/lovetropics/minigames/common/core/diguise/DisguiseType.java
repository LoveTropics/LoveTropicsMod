package com.lovetropics.minigames.common.core.diguise;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public final class DisguiseType {
	public static final Codec<DisguiseType> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ENTITY_TYPE.fieldOf("entity").forGetter(c -> c.type),
				CompoundNBT.CODEC.optionalFieldOf("tag").forGetter(c -> Optional.ofNullable(c.nbt)),
				Codec.BOOL.optionalFieldOf("apply_attributes", true).forGetter(c -> c.applyAttributes)
		).apply(instance, DisguiseType::new);
	});

	public final EntityType<?> type;
	public final CompoundNBT nbt;
	public final boolean applyAttributes;

	public DisguiseType(EntityType<?> type, CompoundNBT nbt) {
		this(type, nbt, true);
	}

	public DisguiseType(EntityType<?> type, @Nullable CompoundNBT nbt, boolean applyAttributes) {
		this.type = type;
		this.nbt = nbt;
		this.applyAttributes = applyAttributes;
	}

	private DisguiseType(EntityType<?> type, Optional<CompoundNBT> nbt, boolean applyAttributes) {
		this(type, nbt.orElse(null), applyAttributes);
	}

	@Nullable
	public Entity createEntityFor(PlayerEntity player) {
		Entity entity = this.type.create(player.world);
		if (entity == null) return null;

		entity.setCustomName(player.getDisplayName());
		entity.setCustomNameVisible(true);

		this.fixInvalidEntities(entity);

		if (this.nbt != null) {
			entity.read(this.nbt);
		}

		return entity;
	}

	private void fixInvalidEntities(Entity entity) {
		if (entity instanceof PaintingEntity) {
			((PaintingEntity) entity).art = PaintingType.KEBAB;
		}
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeRegistryIdUnsafe(ForgeRegistries.ENTITIES, this.type);
		buffer.writeCompoundTag(this.nbt);
		buffer.writeBoolean(this.applyAttributes);
	}

	public static DisguiseType decode(PacketBuffer buffer) {
		EntityType<?> type = buffer.readRegistryIdUnsafe(ForgeRegistries.ENTITIES);
		CompoundNBT nbt = buffer.readCompoundTag();
		boolean applyAttributes = buffer.readBoolean();
		return new DisguiseType(type, nbt, applyAttributes);
	}
}
