package com.lovetropics.minigames.common.content.survive_the_tide.entity;

import com.lovetropics.minigames.LoveTropics;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

public final class DriftwoodEntity extends Entity {
	private static final DataParameter<Boolean> FLOATING = EntityDataManager.defineId(DriftwoodEntity.class, DataSerializers.BOOLEAN);

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

	private Vector3d steerDirection;
	private float steerYaw;
	private int steerTimer;

	private final List<PlayerEntity> riders = new ArrayList<>();

	public DriftwoodEntity(EntityType<?> type, World world) {
		super(type, world);
	}

	@Override
	protected void defineSynchedData() {
		this.entityData.define(FLOATING, true);
	}

	@Override
	protected boolean isMovementNoisy() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return true;
	}

	@Override
	public boolean isPickable() {
		return !this.removed;
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
			steerDirection = new Vector3d(
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

		for (PlayerEntity player : riders) {
			player.getCapability(LoveTropics.driftwoodRiderCap()).ifPresent(rider -> {
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
			Vector3d motion = getDeltaMovement();

			boolean atSurface = getY() >= floatHeight - 0.05;
			if (atSurface) {
				setPos(getX(), floatHeight, getZ());

				if (!riders.isEmpty()) {
					floatDepth = Math.min(floatDepth + SINK_PER_TICK, 1.0F);
					setFloatDepth(floatDepth);
				}

				if (steerDirection != null) {
					yRot += (steerYaw - yRot) * 0.25;
					motion = motion.add(steerDirection.x * 0.008, 0.0, steerDirection.z * 0.008);
				}
			} else {
				motion = new Vector3d(motion.x, 0.1, motion.z);
			}

			setDeltaMovement(motion.x * 0.95, motion.y, motion.z * 0.95);
		} else {
			double accelerationY = wasTouchingWater ? -0.01 : -0.08;

			Vector3d motion = getDeltaMovement();
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
				(float) (yRot + MathHelper.wrapDegrees(lerpYaw - yRot) / lerpTicks),
				(float) (xRot + (lerpPitch - xRot) / lerpTicks)
		);
	}

	private void tickSteering() {
		if (steerDirection != null) {
			if (--steerTimer <= 0) {
				steerDirection = null;
			}
		}
	}

	private List<PlayerEntity> collectRiders() {
		AxisAlignedBB bounds = getBoundingBox();
		AxisAlignedBB ridingBounds = new AxisAlignedBB(bounds.minX, bounds.maxY, bounds.minZ, bounds.maxX, bounds.maxY + 0.2, bounds.maxZ);
		return level.getEntities(EntityType.PLAYER, ridingBounds, EntityPredicates.NO_SPECTATORS);
	}

	private float getFloatHeight() {
		if (!canFloat()) return -1.0F;

		float waterSurface = getWaterSurface();
		if (waterSurface == -1.0F) return -1.0F;

		return waterSurface - floatDepth;
	}

	private float getWaterSurface() {
		if (!wasTouchingWater) return -1.0F;

		int minY = MathHelper.floor(getBoundingBox().minY - 0.5);
		int maxY = MathHelper.ceil(getBoundingBox().maxY);

		BlockPos.Mutable mutablePos = new BlockPos.Mutable();
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
	protected void readAdditionalSaveData(CompoundNBT compound) {
		if (compound.contains("float_depth", Constants.NBT.TAG_FLOAT)) {
			setFloatDepth(compound.getFloat("float_depth"));
		}
	}

	@Override
	protected void addAdditionalSaveData(CompoundNBT compound) {
		compound.putFloat("float_depth", floatDepth);
	}

	@Override
	public IPacket<?> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}
