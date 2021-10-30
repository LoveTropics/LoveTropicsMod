package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.minigames.common.content.biodiversity_blitz.FriendlyExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public final class ProximityBombPlantBehavior implements IGameBehavior {
	public static final Codec<ProximityBombPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.DOUBLE.fieldOf("radius").forGetter(c -> c.radius)
	).apply(instance, ProximityBombPlantBehavior::new));

	private final double radius;

	public ProximityBombPlantBehavior(double radius) {
		this.radius = radius;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.TICK, (player, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % 5 != 0) {
				return;
			}

			ServerWorld world = game.getWorld();
			List<Plant> removedPlants = new ArrayList<>();

			for (Plant plant : plants) {
				AxisAlignedBB detonateBounds = plant.coverage().asBounds().grow(this.radius);
				List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, detonateBounds, BbMobEntity.PREDICATE);

				if (!entities.isEmpty()) {
					removedPlants.add(plant);

					explode(world, plant.coverage());
				}
			}

			for (Plant plant : removedPlants) {
				game.invoker(BbEvents.BREAK_PLANT).breakPlant(player, plot, plant);
			}
		});
	}

	// Kaboom!
	private static void explode(ServerWorld world, PlantCoverage coverage) {
		for (BlockPos pos : coverage) {
			world.removeBlock(pos, true);

			double x = pos.getX() + 0.5;
			double y = pos.getY() + 0.5;
			double z = pos.getZ() + 0.5;

			Explosion explosion = new FriendlyExplosion(world, null, null, null, x, y, z, 4.0f, false, Explosion.Mode.BREAK);
			explosion.doExplosionA();
			explosion.doExplosionB(false);

			for (ServerPlayerEntity player : world.getPlayers()) {
				if (player.getDistanceSq(x, y, z) < 4096.0) {
					player.connection.sendPacket(new SExplosionPacket(x, y, z, 4.0f, explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(player)));
				}
			}
		}
	}
}
