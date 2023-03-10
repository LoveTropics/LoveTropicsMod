package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.explosion.FilteredExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public record ProximityBombPlantBehavior(double radius) implements IGameBehavior {
	public static final Codec<ProximityBombPlantBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.DOUBLE.fieldOf("radius").forGetter(c -> c.radius)
	).apply(i, ProximityBombPlantBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.TICK, (players, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % 5 != 0) {
				return;
			}

			ServerLevel world = game.getWorld();
			List<Plant> removedPlants = new ArrayList<>();

			for (Plant plant : plants) {
				AABB detonateBounds = plant.coverage().asBounds().inflate(this.radius);
				List<Mob> entities = world.getEntitiesOfClass(Mob.class, detonateBounds, BbMobEntity.PREDICATE);

				if (!entities.isEmpty()) {
					removedPlants.add(plant);

					explode(world, plant.coverage());
				}
			}

			for (Plant plant : removedPlants) {
				game.invoker(BbEvents.BREAK_PLANT).breakPlant(players.iterator().next(), plot, plant);
			}
		});
	}

	// Kaboom!
	private static void explode(ServerLevel world, PlantCoverage coverage) {
		for (BlockPos pos : coverage) {
			world.removeBlock(pos, true);

			double x = pos.getX() + 0.5;
			double y = pos.getY() + 0.5;
			double z = pos.getZ() + 0.5;

			Explosion explosion = new FilteredExplosion(world, null, null, null, x, y, z, 2.0f, false, Explosion.BlockInteraction.BREAK, e -> e instanceof ServerPlayer);
			explosion.explode();
			explosion.finalizeExplosion(false);

			for (ServerPlayer player : world.players()) {
				if (player.distanceToSqr(x, y, z) < 4096.0) {
					player.connection.send(new ClientboundExplodePacket(x, y, z, 2.0f, explosion.getToBlow(), explosion.getHitPlayers().get(player)));
				}
			}
		}
	}
}
