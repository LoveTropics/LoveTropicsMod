package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.common.core.diguise.ability.DisguiseAbilities;
import com.lovetropics.minigames.common.core.diguise.ability.DisguiseAbilitiesRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public final class DisguiseType {
	public static final Codec<DisguiseType> CODEC = RecordCodecBuilder.create(i -> i.group(
			ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity").forGetter(c -> c.type),
			Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(c -> c.scale),
			CompoundTag.CODEC.optionalFieldOf("tag").forGetter(c -> Optional.ofNullable(c.nbt)),
			Codec.BOOL.optionalFieldOf("apply_attributes", true).forGetter(c -> c.applyAttributes)
	).apply(i, DisguiseType::create));

	public final EntityType<?> type;

	public final float scale;
	public final CompoundTag nbt;
	public final boolean applyAttributes;
	public final DisguiseAbilities abilities;

	private DisguiseType(EntityType<?> type, float scale, @Nullable CompoundTag nbt, boolean applyAttributes, DisguiseAbilities abilities) {
		this.type = type;
		this.scale = scale;
		this.nbt = nbt;
		this.applyAttributes = applyAttributes;
		this.abilities = abilities;
	}

	public static DisguiseType create(EntityType<?> type, float scale, @Nullable CompoundTag nbt, boolean applyAttributes) {
		DisguiseAbilities abilities = DisguiseAbilitiesRegistry.create(type);
		return new DisguiseType(type, scale, nbt, applyAttributes, abilities);
	}

	public static DisguiseType create(EntityType<?> type, float scale, @Nullable CompoundTag nbt) {
		return create(type, scale, nbt, true);
	}

	private static DisguiseType create(EntityType<?> type, float scale, Optional<CompoundTag> nbt, Boolean applyAttributes) {
		return create(type, scale, nbt.orElse(null), applyAttributes);
	}

	@Nullable
	public Entity createEntityFor(Player player) {
		Entity entity = this.type.create(player.level());
		if (entity == null) return null;

		if (this.nbt != null) {
			entity.load(this.nbt);
		}

		this.fixInvalidEntities(entity);

		if (!entity.hasCustomName()) {
			entity.setCustomName(player.getDisplayName());
		}
		entity.setCustomNameVisible(true);

		return entity;
	}

	private void fixInvalidEntities(Entity entity) {
		entity.stopRiding();
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES, this.type);
        buffer.writeFloat(this.scale);
		buffer.writeNbt(this.nbt);
		buffer.writeBoolean(this.applyAttributes);
	}

	public static DisguiseType decode(FriendlyByteBuf buffer) {
		EntityType<?> type = buffer.readRegistryIdUnsafe(ForgeRegistries.ENTITY_TYPES);
        float scale = buffer.readFloat();
		CompoundTag nbt = buffer.readNbt();
		boolean applyAttributes = buffer.readBoolean();
		return DisguiseType.create(type, scale, nbt, applyAttributes);
	}
}
