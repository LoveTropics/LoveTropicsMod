package com.lovetropics.minigames.common.content.turtle_race;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;

public record TurtleRiderBehavior(EntityType<?> entityType, CompoundTag entityTag) implements IGameBehavior {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static final Codec<TurtleRiderBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			ForgeRegistries.ENTITIES.getCodec().fieldOf("entity").forGetter(TurtleRiderBehavior::entityType),
			CompoundTag.CODEC.optionalFieldOf("tag", new CompoundTag()).forGetter(TurtleRiderBehavior::entityTag)
	).apply(i, TurtleRiderBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		Map<UUID, Entity> turtles = new Object2ObjectOpenHashMap<>();

		events.listen(GamePlayerEvents.SPAWN, (player, role) -> {
			Entity entity = spawnTurtle(player);
			if (entity == null) {
				LOGGER.error("Failed to spawn turtle entity of type: {}", entityType);
				return;
			}
			player.startRiding(entity);
			turtles.put(player.getUUID(), entity);
		});

		events.listen(GamePlayerEvents.REMOVE, player -> turtles.remove(player.getUUID()));

		events.listen(GamePlayerEvents.TICK, player -> {
			Entity turtle = turtles.get(player.getUUID());
			if (turtle == null) {
				return;
			}

			if (player.getVehicle() != turtle) {
				fixTurtle(turtles, player, turtle);
			}
		});
	}

	private void fixTurtle(Map<UUID, Entity> turtles, ServerPlayer player, Entity turtle) {
		if (!turtle.isAlive()) {
			turtle = spawnTurtle(player);
			if (turtle == null) {
				turtles.remove(player.getUUID());
				return;
			}
			turtles.put(player.getUUID(), turtle);
		}

		player.startRiding(turtle, true);

		ServerChunkCache chunkSource = player.getLevel().getChunkSource();
		chunkSource.chunkMap.broadcast(turtle, new ClientboundSetPassengersPacket(turtle));
	}

	@Nullable
	private Entity spawnTurtle(ServerPlayer player) {
		CompoundTag tag = entityTag.copy();
		tag.putString("id", entityType.getRegistryName().toString());

		ServerLevel level = player.getLevel();
		Entity entity = EntityType.loadEntityRecursive(tag, level, e -> {
			e.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
			return e;
		});

		if (entity != null) {
			if (entity instanceof Mob mob) {
				mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.COMMAND, null, null);
			}
			return level.tryAddFreshEntityWithPassengers(entity) ? entity : null;
		}

		return null;
	}
}
