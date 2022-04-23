package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantPlacement;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SAnimateHandPacket;
import net.minecraft.state.properties.AttachFace;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.function.Predicate;

public final class ScareTrapPlantBehavior implements IGameBehavior {
	public static final Codec<ScareTrapPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("trigger_radius").forGetter(c -> c.triggerRadius),
			Codec.DOUBLE.fieldOf("scare_radius").forGetter(c -> c.scareRadius)
	).apply(instance, ScareTrapPlantBehavior::new));

	private static final Predicate<MobEntity> SCARE_PREDICATE = BbMobEntity.PREDICATE;

	private final double triggerRadius;
	private final double scareRadius;

	private IGamePhase game;
	private PlotsState plots;

	public ScareTrapPlantBehavior(double triggerRadius, double scareRadius) {
		this.triggerRadius = triggerRadius;
		this.scareRadius = scareRadius;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(BbPlantEvents.ADD, (player, plot, plant) -> {
			plant.state().put(Trap.KEY, new Trap());
		});

		events.listen(BbPlantEvents.PLACE, this::place);
		events.listen(BbPlantEvents.TICK, this::tick);
		events.listen(GamePlayerEvents.USE_BLOCK, this::useBlock);
	}

	private PlantPlacement place(ServerPlayerEntity player, Plot plot, BlockPos pos) {
		return new PlantPlacement()
				.covers(this.buildPlantCoverage(plot, pos))
				.places((world, coverage) -> {
					this.placeReadyTrap(plot, pos);
					return true;
				});
	}

	private ActionResultType useBlock(ServerPlayerEntity player, ServerWorld world, BlockPos pos, Hand hand, BlockRayTraceResult traceResult) {
		if (world.getBlockState(pos).is(Blocks.LEVER)) {
			return this.useLever(player, pos);
		}

		return ActionResultType.PASS;
	}

	private ActionResultType useLever(ServerPlayerEntity player, BlockPos pos) {
		Plot plot = plots.getPlotFor(player);
		if (plot != null && plot.bounds.contains(pos)) {
			Plant plant = plot.plants.getPlantAt(pos);
			if (plant != null && this.resetTrap(plot, plant)) {
				return ActionResultType.SUCCESS;
			}
		}

		return ActionResultType.FAIL;
	}

	private void tick(ServerPlayerEntity player, Plot plot, List<Plant> plants) {
		long ticks = game.ticks();
		if (ticks % 10 != 0) return;

		for (Plant plant : plants) {
			Trap trap = plant.state(Trap.KEY);
			if (trap == null || !trap.ready) {
				continue;
			}

			if (this.tickTrap(plot, plant)) {
				trap.ready = false;
			}
		}
	}

	private boolean tickTrap(Plot plot, Plant plant) {
		// TODO: mobs should run around in panic after being scared

		AxisAlignedBB bounds = plant.coverage().asBounds();
		AxisAlignedBB triggerBounds = bounds.inflate(this.triggerRadius);

		ServerWorld world = game.getWorld();

		List<MobEntity> triggerEntities = world.getEntitiesOfClass(MobEntity.class, triggerBounds, SCARE_PREDICATE);
		if (triggerEntities.isEmpty()) {
			return false;
		}

		AxisAlignedBB scareBounds = bounds.inflate(this.scareRadius);
		List<MobEntity> entities = world.getEntitiesOfClass(MobEntity.class, scareBounds, SCARE_PREDICATE);
		this.triggerTrap(plot, plant, entities);

		return true;
	}

	private void triggerTrap(Plot plot, Plant plant, List<MobEntity> entities) {
		BlockPos origin = plant.coverage().getOrigin();
		Vector3d pushFrom = Vector3d.atCenterOf(origin);

		for (MobEntity entity : entities) {
			this.scareEntity(origin, pushFrom, entity);
		}

		this.extendTrap(plot, plant);
	}

	private void scareEntity(BlockPos pos, Vector3d pushFrom, MobEntity entity) {
		Vector3d entityPos = entity.position();

		// Scaled so that closer values are higher, with a max of 5
		double dist = 2.0 / (0.1 + entityPos.distanceTo(pushFrom));

		// Angle between entity and center of lantern
		double theta = Math.atan2(entityPos.z - pushFrom.z, entityPos.x - pushFrom.x);

		// zoooooom
		entity.push(dist * Math.cos(theta), 0.25, dist * Math.sin(theta));

		// Prevent mobs from flying to the moon due to too much motion
		Vector3d motion = entity.getDeltaMovement();
		entity.setDeltaMovement(Math.min(motion.x, 5), Math.min(motion.y, 0.25), Math.min(motion.z, 5));

		if (entity instanceof BbMobEntity) {
			((BbMobEntity) entity).getMobBrain().addScarySource(pos);
		}

		// Spawn critical hit particles around the entity
		game.getAllPlayers().sendPacket(new SAnimateHandPacket(entity, 4));
	}

	private void extendTrap(Plot plot, Plant plant) {
		Trap trap = plant.state(Trap.KEY);
		if (trap == null || !trap.trigger(game)) {
			return;
		}

		BlockPos origin = plant.coverage().getOrigin();

		game.getWorld().playSound(null, origin, SoundEvents.PISTON_EXTEND, SoundCategory.BLOCKS, 1.0F, 1.0F);

		this.clearTrap(plant);
		this.placeExtendedTrap(plot, origin);
	}

	private boolean resetTrap(Plot plot, Plant plant) {
		Trap trap = plant.state(Trap.KEY);
		if (trap == null || !trap.tryReset(game)) {
			return false;
		}

		BlockPos origin = plant.coverage().getOrigin();

		game.getWorld().playSound(null, origin, SoundEvents.PISTON_CONTRACT, SoundCategory.BLOCKS, 1.0F, 1.0F);

		this.clearTrap(plant);
		this.placeReadyTrap(plot, origin);

		return true;
	}

	private void clearTrap(Plant plant) {
		ServerWorld world = game.getWorld();
		for (BlockPos pos : plant.coverage()) {
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), Constants.BlockFlags.DEFAULT | Constants.BlockFlags.UPDATE_NEIGHBORS);
		}
	}

	private void placeExtendedTrap(Plot plot, BlockPos pos) {
		ServerWorld world = game.getWorld();

		world.setBlockAndUpdate(pos, Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, Direction.UP));
		world.setBlockAndUpdate(pos.above(), Blocks.JACK_O_LANTERN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, plot.forward));

		BlockState lever = Blocks.LEVER.defaultBlockState()
				.setValue(LeverBlock.FACING, plot.forward.getOpposite())
				.setValue(LeverBlock.FACE, AttachFace.WALL);
		world.setBlockAndUpdate(pos.above().relative(plot.forward.getOpposite()), lever);
	}

	private void placeReadyTrap(Plot plot, BlockPos pos) {
		ServerWorld world = game.getWorld();
		world.setBlockAndUpdate(pos, Blocks.JACK_O_LANTERN.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, plot.forward));
	}

	private PlantCoverage buildPlantCoverage(Plot plot, BlockPos pos) {
		// TODO: duplication
		return new PlantCoverage.Builder()
				.add(pos).add(pos.above())
				.add(pos.above().relative(plot.forward.getOpposite()))
				.build();
	}

	static final class Trap {
		private static final long TRIGGER_FROZEN_TICKS = 5 * 20;

		static final PlantState.Key<Trap> KEY = PlantState.Key.create();

		boolean ready = true;
		long frozenExpiry;

		boolean trigger(IGamePhase game) {
			if (ready) {
				frozenExpiry = game.ticks() + TRIGGER_FROZEN_TICKS;
				ready = false;
				return true;
			} else {
				return false;
			}
		}

		boolean tryReset(IGamePhase game) {
			if (!ready && game.ticks() > frozenExpiry) {
				ready = true;
				return true;
			} else {
				return false;
			}
		}
	}
}
