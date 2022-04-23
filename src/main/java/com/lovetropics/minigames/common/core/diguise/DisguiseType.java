package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.common.core.diguise.ability.DisguiseAbilities;
import com.lovetropics.minigames.common.core.diguise.ability.DisguiseAbilitiesRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public final class DisguiseType {
	public static final Codec<DisguiseType> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				ForgeRegistries.ENTITIES.getCodec().fieldOf("entity").forGetter(c -> c.type),
				CompoundTag.CODEC.optionalFieldOf("tag").forGetter(c -> Optional.ofNullable(c.nbt)),
				Codec.BOOL.optionalFieldOf("apply_attributes", true).forGetter(c -> c.applyAttributes)
		).apply(instance, DisguiseType::create);
	});

	public final EntityType<?> type;

	public final CompoundTag nbt;
	public final boolean applyAttributes;
	public final DisguiseAbilities abilities;

	private DisguiseType(EntityType<?> type, @Nullable CompoundTag nbt, boolean applyAttributes, DisguiseAbilities abilities) {
		this.type = type;
		this.nbt = nbt;
		this.applyAttributes = applyAttributes;
		this.abilities = abilities;
	}

	public static DisguiseType create(EntityType<?> type, @Nullable CompoundTag nbt, boolean applyAttributes) {
		DisguiseAbilities abilities = DisguiseAbilitiesRegistry.create(type);
		return new DisguiseType(type, nbt, applyAttributes, abilities);
	}

	public static DisguiseType create(EntityType<?> type, @Nullable CompoundTag nbt) {
		return create(type, nbt, true);
	}

	private static DisguiseType create(EntityType<?> type, Optional<CompoundTag> nbt, Boolean applyAttributes) {
		return create(type, nbt.orElse(null), applyAttributes);
	}

	@Nullable
	public Entity createEntityFor(Player player) {
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

	private boolean shouldNeverCreate(Player player, EntityType<?> type) {
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
		if (entity instanceof Painting) {
			((Painting) entity).motive = Motive.KEBAB;
		}
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeRegistryIdUnsafe(ForgeRegistries.ENTITIES, this.type);
		buffer.writeNbt(this.nbt);
		buffer.writeBoolean(this.applyAttributes);
	}

	public static DisguiseType decode(FriendlyByteBuf buffer) {
		EntityType<?> type = buffer.readRegistryIdUnsafe(ForgeRegistries.ENTITIES);
		CompoundTag nbt = buffer.readNbt();
		boolean applyAttributes = buffer.readBoolean();
		return DisguiseType.create(type, nbt, applyAttributes);
	}

	public void fixNbtFor(ServerPlayer player) {
		if (this.nbt != null) {
			this.nbt.putString("CustomName", Component.Serializer.toJson(player.getDisplayName()));
			this.nbt.putBoolean("CustomNameVisible", true);
		}
	}
}
