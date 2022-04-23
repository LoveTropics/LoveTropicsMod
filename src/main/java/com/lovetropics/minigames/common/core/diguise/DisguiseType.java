package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.common.core.diguise.ability.DisguiseAbilities;
import com.lovetropics.minigames.common.core.diguise.ability.DisguiseAbilitiesRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public final class DisguiseType {
	public static final Codec<DisguiseType> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ENTITY_TYPE.fieldOf("entity").forGetter(c -> c.type),
				CompoundNBT.CODEC.optionalFieldOf("tag").forGetter(c -> Optional.ofNullable(c.nbt)),
				Codec.BOOL.optionalFieldOf("apply_attributes", true).forGetter(c -> c.applyAttributes)
		).apply(instance, DisguiseType::create);
	});

	public final EntityType<?> type;

	public final CompoundNBT nbt;
	public final boolean applyAttributes;
	public final DisguiseAbilities abilities;

	private DisguiseType(EntityType<?> type, @Nullable CompoundNBT nbt, boolean applyAttributes, DisguiseAbilities abilities) {
		this.type = type;
		this.nbt = nbt;
		this.applyAttributes = applyAttributes;
		this.abilities = abilities;
	}

	public static DisguiseType create(EntityType<?> type, @Nullable CompoundNBT nbt, boolean applyAttributes) {
		DisguiseAbilities abilities = DisguiseAbilitiesRegistry.create(type);
		return new DisguiseType(type, nbt, applyAttributes, abilities);
	}

	public static DisguiseType create(EntityType<?> type, @Nullable CompoundNBT nbt) {
		return create(type, nbt, true);
	}

	private static DisguiseType create(EntityType<?> type, Optional<CompoundNBT> nbt, Boolean applyAttributes) {
		return create(type, nbt.orElse(null), applyAttributes);
	}

	@Nullable
	public Entity createEntityFor(PlayerEntity player) {
		if (shouldNeverCreate(player, this.type)) {
			return null;
		}

		Entity entity = this.type.create(player.level);
		if (entity == null) return null;

		if (this.nbt != null) {
			entity.load(this.nbt);
		}

		this.fixInvalidEntities(entity);

		entity.setCustomName(player.getDisplayName());
		entity.setCustomNameVisible(true);

		return entity;
	}

	private boolean shouldNeverCreate(PlayerEntity player, EntityType<?> type) {
		ResourceLocation location = ForgeRegistries.ENTITIES.getKey(type);

		if (location == null) {
			return true;
		}

		// Instantly crashes- prevent disguising as poison blots
		if (location.getNamespace().equals("tropicraft") && location.getPath().equals("poison_blot")) {
			return true;
		}

		// Crashes when riding- prevent
		Entity ridingEntity = player.getVehicle();
		if (ridingEntity != null) {
			EntityType<?> ridingType = ridingEntity.getType();
			ResourceLocation ridingLocation = ForgeRegistries.ENTITIES.getKey(ridingType);

			if (ridingLocation != null && ridingLocation.getNamespace().equals("tropicraft") && ridingLocation.getPath().equals("turtle")) {
				return true;
			}
		}

		return false;
	}

	private void fixInvalidEntities(Entity entity) {
		if (entity instanceof PaintingEntity) {
			((PaintingEntity) entity).motive = PaintingType.KEBAB;
		}
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeRegistryIdUnsafe(ForgeRegistries.ENTITIES, this.type);
		buffer.writeNbt(this.nbt);
		buffer.writeBoolean(this.applyAttributes);
	}

	public static DisguiseType decode(PacketBuffer buffer) {
		EntityType<?> type = buffer.readRegistryIdUnsafe(ForgeRegistries.ENTITIES);
		CompoundNBT nbt = buffer.readNbt();
		boolean applyAttributes = buffer.readBoolean();
		return DisguiseType.create(type, nbt, applyAttributes);
	}

	public void fixNbtFor(ServerPlayerEntity player) {
		if (this.nbt != null) {
			this.nbt.putString("CustomName", ITextComponent.Serializer.toJson(player.getDisplayName()));
			this.nbt.putBoolean("CustomNameVisible", true);
		}
	}
}
