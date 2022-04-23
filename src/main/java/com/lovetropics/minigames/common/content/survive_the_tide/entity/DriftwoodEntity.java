package com.lovetropics.minigames.common.content.survive_the_tide.entity;

import com.lovetropics.minigames.LoveTropics;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

public final class DriftwoodEntity extends Entity {
	private static final EntityDataAccessor<Boolean> FLOATING = SynchedEntityData.defineId(DriftwoodEntity.class, EntityDataSerializers.BOOLEAN);

	private static final float START_FLOAT_DEPTH = 0.5F;
	private static final int FLOAT_TICKS = 20 * 60;
	private static final float SINK_PER_TICK = (1.0F - START_FLOAT_DEPTH) / FLOAT_TICKS;

	private double lerpX;
	private double lerpY;
	private double lerpZ;
	private float lerpYaw;
	private float lerpPitch;
	private int lerpTicks;

	private float floatDepth = START_FLOAT_DEPTH;

	private Vec3 steerDirection;
	private float steerYaw;
	private int steerTimer;

	private final List<Player> riders = new ArrayList<>();

	public DriftwoodEntity(EntityType<?> type, Level world) {
		super(type, world);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(FLOATING, true);
	}

	@Override
	protected Entity.MovementEmission getMovementEmission() {
		return Entity.MovementEmission.NONE;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public boolean isPickable() {
		return !this.isRemoved();
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	public boolean canCollideWith(Entity entity) {
		return (entity.canBeCollidedWith() || entity.isPushable()) && !this.isPassengerOfSameVehicle(entity);
	}

	public boolean paddle(float direction) {
		if (steerDirection == null) {
			double directionRadians = Math.toRadians(direction);
			steerDirection = new Vec3(
					-Math.sin(directionRadians),
					0.0,
					Math.cos(directionRadians)
			);

			steerYaw = direction;
			steerTimer = 10;

			return true;
		}

		return false;
	}

	@Override
	public void tick() {
		xo = getX();
		yo = getY();
		zo = getZ();

		super.tick();

		if (!riders.isEmpty() || tickCount % 10 == 0) {
			riders.clear();
			riders.addAll(collectRiders());
		}

		for (Player player : riders) {
			player.getCapability(LoveTropics.DRIFTWOOD_RIDER).ifPresent(rider -> {
				rider.setRiding(this);
			});
		}

		if (!level.isClientSide) {
			tickSteering();
			tickMovement();
		} else {
			tickLerp();
		}
	}

	private void tickMovement() {
		float floatHeight = getFloatHeight();

		if (floatHeight != -1.0F) {
			Vec3 motion = getDeltaMovement();

			boolean atSurface = getY() >= floatHeight - 0.05;
			if (atSurface) {
				setPos(getX(), floatHeight, getZ());

				if (!riders.isEmpty()) {
					floatDepth = Math.min(floatDepth + SINK_PER_TICK, 1.0F);
					setFloatDepth(floatDepth);
				}

				if (steerDirection != null) {
					setYRot(getYRot() + (steerYaw - getYRot()) * 0.25f);
					motion = motion.add(steerDirection.x * 0.008, 0.0, steerDirection.z * 0.008);
				}
			} else {
				motion = new Vec3(motion.x, 0.1, motion.z);
			}

			setDeltaMovement(motion.x * 0.95, motion.y, motion.z * 0.95);
		} else {
			double accelerationY = wasTouchingWater ? -0.01 : -0.08;

			Vec3 motion = getDeltaMovement();
			motion = motion.multiply(0.7, 0.7, 0.7);
			motion = motion.add(0.0, accelerationY, 0.0);

			setDeltaMovement(motion);
		}

		move(MoverType.SELF, getDeltaMovement());
	}

	private void tickLerp() {
		if (lerpTicks <= 0) {
			return;
		}

		double lerpTicks = this.lerpTicks--;

		setPos(
				getX() + (lerpX - getX()) / lerpTicks,
				getY() + (lerpY - getY()) / lerpTicks,
				getZ() + (lerpZ - getZ()) / lerpTicks
		);

		setRot(
				(float) (getYRot() + Mth.wrapDegrees(lerpYaw - getYRot()) / lerpTicks),
				(float) (getXRot() + (lerpPitch - getXRot()) / lerpTicks)
		);
	}

	private void tickSteering() {
		if (steerDirection != null) {
			if (--steerTimer <= 0) {
				steerDirection = null;
			}
		}
	}

	private List<Player> collectRiders() {
		AABB bounds = getBoundingBox();
		AABB ridingBounds = new AABB(bounds.minX, bounds.maxY, bounds.minZ, bounds.maxX, bounds.maxY + 0.2, bounds.maxZ);
		return level.getEntities(EntityType.PLAYER, ridingBounds, EntitySelector.NO_SPECTATORS);
	}

	private float getFloatHeight() {
		if (!canFloat()) return -1.0F;

		float waterSurface = getWaterSurface();
		if (waterSurface == -1.0F) return -1.0F;

		return waterSurface - floatDepth;
	}

	private float getWaterSurface() {
		if (!wasTouchingWater) return -1.0F;

		int minY = Mth.floor(getBoundingBox().minY - 0.5);
		int maxY = Mth.ceil(getBoundingBox().maxY);

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
		mutablePos.set(this.blockPosition());

		float waterHeight = -1.0F;

		for (int y = minY; y <= maxY; y++) {
			mutablePos.setY(y);
			waterHeight = Math.max(getWaterSurfaceAt(mutablePos), waterHeight);
		}

		return waterHeight;
	}

	private float getWaterSurfaceAt(BlockPos pos) {
		FluidState fluid = level.getFluidState(pos);
		if (fluid.is(FluidTags.WATER)) {
			return pos.getY() + fluid.getHeight(level, pos);
		}
		return -1.0F;
	}

	private void setFloatDepth(float floatDepth) {
		this.floatDepth = floatDepth;
		getEntityData().set(FLOATING, floatDepth < 1.0F);
	}

	private boolean canFloat() {
		return getEntityData().get(FLOATING);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void lerpTo(double x, double y, double z, float yaw, float pitch, int lerpLength, boolean teleport) {
		lerpX = x;
		lerpY = y;
		lerpZ = z;
		lerpYaw = yaw;
		lerpPitch = pitch;
		lerpTicks = lerpLength;
	}

	@Override
	public void push(Entity entity) {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {
		if (compound.contains("float_depth", Tag.TAG_FLOAT)) {
			setFloatDepth(compound.getFloat("float_depth"));
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {
		compound.putFloat("float_depth", floatDepth);
	}

	@Override
	public Packet<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
