package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.Util;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class PlayerDisguise implements ICapabilityProvider {
	private final LazyOptional<PlayerDisguise> instance = LazyOptional.of(() -> this);

	private final Player player;

	private DisguiseType disguise = DisguiseType.DEFAULT;
	@Nullable
	private Entity entity;

	private PlayerDisguise(Player player) {
		this.player = player;
	}

	@SubscribeEvent
	public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof Player player) {
			event.addCapability(Util.resource("player_disguise"), new PlayerDisguise(player));
		}
	}

	public static PlayerDisguise get(Player player) {
		return player.getCapability(LoveTropics.PLAYER_DISGUISE).orElseThrow(IllegalStateException::new);
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
		entity = disguise.createEntityFor(player);
		player.refreshDimensions();
	}

	public DisguiseType type() {
		return disguise;
	}

	@Nullable
	public Entity entity() {
		return entity;
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
		if (entity != null) {
			float entityScale = entity.getBbHeight() / EntityType.PLAYER.getHeight();
			return disguise.scale() * entityScale;
		} else {
			return disguise.scale();
		}
	}
}
