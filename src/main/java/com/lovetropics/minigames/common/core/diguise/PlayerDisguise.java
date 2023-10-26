package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class PlayerDisguise implements ICapabilitySerializable<Tag> {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final LazyOptional<PlayerDisguise> instance = LazyOptional.of(() -> this);

	private final LivingEntity entity;

	private DisguiseType disguise = DisguiseType.DEFAULT;
	@Nullable
	private Entity disguisedEntity;

	private PlayerDisguise(LivingEntity entity) {
		this.entity = entity;
	}

	@SubscribeEvent
	public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof final LivingEntity living) {
			event.addCapability(Util.resource("player_disguise"), new PlayerDisguise(living));
		}
	}

	@Nullable
	public static PlayerDisguise getOrNull(Entity entity) {
		return entity.getCapability(LoveTropics.PLAYER_DISGUISE).orElse(null);
	}

	public boolean isDisguised() {
		return !type().isDefault();
	}

	public void clear() {
		set(DisguiseType.DEFAULT);
	}

	public void clear(DisguiseType disguise) {
		set(this.disguise.clear(disguise));
	}

	public void set(DisguiseType disguise) {
		if (this.disguise.equals(disguise)) {
			return;
		}
		this.disguise = disguise;
		disguisedEntity = disguise.createEntityFor(entity);
		entity.refreshDimensions();
	}

	public DisguiseType type() {
		return disguise;
	}

	@Nullable
	public Entity entity() {
		return disguisedEntity;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return LoveTropics.PLAYER_DISGUISE.orEmpty(cap, instance);
	}

	public void copyFrom(PlayerDisguise from) {
		set(from.type());
	}

	public float getEffectiveScale() {
		if (disguisedEntity != null) {
			float entityScale = disguisedEntity.getBbHeight() / EntityType.PLAYER.getHeight();
			return disguise.scale() * entityScale;
		} else {
			return disguise.scale();
		}
	}

	@Override
	public Tag serializeNBT() {
		return net.minecraft.Util.getOrThrow(DisguiseType.CODEC.encodeStart(NbtOps.INSTANCE, disguise), IllegalStateException::new);
	}

	@Override
	public void deserializeNBT(Tag tag) {
		DisguiseType.CODEC.parse(NbtOps.INSTANCE, tag).resultOrPartial(LOGGER::error).ifPresent(this::set);
	}
}
