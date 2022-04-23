package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
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
	public static final Capability.IStorage<PlayerDisguise> STORAGE = new Capability.IStorage<PlayerDisguise>() {
		@Nullable
		@Override
		public Tag writeNBT(Capability<PlayerDisguise> capability, PlayerDisguise instance, Direction side) {
			return null;
		}

		@Override
		public void readNBT(Capability<PlayerDisguise> capability, PlayerDisguise instance, Direction side, Tag nbt) {
		}
	};

	private final LazyOptional<PlayerDisguise> instance = LazyOptional.of(() -> this);

	private final Player player;

	private DisguiseType disguiseType;
	private Entity disguiseEntity;

	PlayerDisguise(Player player) {
		this.player = player;
	}

	@SubscribeEvent
	public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Player) {
			event.addCapability(Util.resource("player_disguise"), new PlayerDisguise((Player) entity));
		}
	}

	public static LazyOptional<PlayerDisguise> get(Player player) {
		return player.getCapability(LoveTropics.playerDisguiseCap());
	}

	@Nullable
	public static DisguiseType getDisguiseType(Player player) {
		PlayerDisguise disguise = get(player).orElse(null);
		return disguise != null ? disguise.getDisguiseType() : null;
	}

	@Nullable
	public static Entity getDisguiseEntity(Player player) {
		PlayerDisguise disguise = get(player).orElse(null);
		return disguise != null ? disguise.getDisguiseEntity() : null;
	}

	public void clearDisguise() {
		this.setDisguise(null);
	}

	public void clearDisguise(DisguiseType disguise) {
		if (this.disguiseType == disguise) {
			this.clearDisguise();
		}
	}

	public void setDisguise(@Nullable DisguiseType disguise) {
		if (disguise != null) {
			this.disguiseType = disguise;
			this.disguiseEntity = disguise.createEntityFor(this.player);
		} else {
			this.disguiseType = null;
			this.disguiseEntity = null;
		}

		this.player.refreshDimensions();
	}

	@Nullable
	public DisguiseType getDisguiseType() {
		return disguiseType;
	}

	@Nullable
	public Entity getDisguiseEntity() {
		return this.disguiseEntity;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return LoveTropics.playerDisguiseCap().orEmpty(cap, instance);
	}

	public void copyFrom(PlayerDisguise from) {
		this.setDisguise(from.getDisguiseType());
	}
}
