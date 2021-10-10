package com.lovetropics.minigames.common.core.diguise;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public final class DisguiseType {
	public final EntityType<?> type;
	public final CompoundNBT nbt;

	public DisguiseType(EntityType<?> type, @Nullable CompoundNBT nbt) {
		this.type = type;
		this.nbt = nbt;
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
	}

	public static DisguiseType decode(PacketBuffer buffer) {
		EntityType<?> type = buffer.readRegistryIdUnsafe(ForgeRegistries.ENTITIES);
		CompoundNBT nbt = buffer.readCompoundTag();
		return new DisguiseType(type, nbt);
	}
}
