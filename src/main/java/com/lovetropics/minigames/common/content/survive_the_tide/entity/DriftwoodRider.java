package com.lovetropics.minigames.common.content.survive_the_tide.entity;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.Util;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class DriftwoodRider implements ICapabilityProvider {
	public static final Capability.IStorage<DriftwoodRider> STORAGE = new Capability.IStorage<DriftwoodRider>() {
		@Nullable
		@Override
		public INBT writeNBT(Capability<DriftwoodRider> capability, DriftwoodRider instance, Direction side) {
			return null;
		}

		@Override
		public void readNBT(Capability<DriftwoodRider> capability, DriftwoodRider instance, Direction side, INBT nbt) {
		}
	};

	private final PlayerEntity player;
	private final LazyOptional<DriftwoodRider> instance = LazyOptional.of(() -> this);
	private DriftwoodEntity ridingDriftwood;
	private int ridingTime;

	DriftwoodRider(PlayerEntity player) {
		this.player = player;
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}

		PlayerEntity player = event.player;
		player.getCapability(LoveTropics.driftwoodRiderCap()).ifPresent(DriftwoodRider::tick);
	}

	@SubscribeEvent
	public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof PlayerEntity) {
			event.addCapability(Util.resource("driftwood_rider"), new DriftwoodRider((PlayerEntity) entity));
		}
	}

	private void tick() {
		DriftwoodEntity ridingDriftwood = this.ridingDriftwood;
		if (ridingTime <= 0 || ridingDriftwood == null) {
			return;
		}

		if (--ridingTime <= 0) {
			this.ridingDriftwood = null;
		}

		if (player.isLocalPlayer()) {
			double deltaX = ridingDriftwood.getX() - ridingDriftwood.xo;
			double deltaZ = ridingDriftwood.getZ() - ridingDriftwood.zo;
			double deltaY = Math.max(ridingDriftwood.getY() - ridingDriftwood.yo, 0.0);
			move(deltaX, deltaY, deltaZ);
		}
	}

	private void move(double deltaX, double deltaY, double deltaZ) {
		if (deltaX == 0.0 && deltaY == 0.0 && deltaZ == 0.0) {
			return;
		}

		Vector3d motion = player.getDeltaMovement();
		boolean onGround = player.isOnGround();

		player.move(MoverType.SELF, new Vector3d(deltaX, deltaY, deltaZ));

		player.setDeltaMovement(motion);
		player.setOnGround(onGround);
	}

	public void setRiding(DriftwoodEntity driftwood) {
		ridingDriftwood = driftwood;
		ridingTime = 10;
	}

	@Nullable
	public DriftwoodEntity getRiding() {
		return ridingDriftwood;
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		return LoveTropics.driftwoodRiderCap().orEmpty(cap, instance);
	}
}
