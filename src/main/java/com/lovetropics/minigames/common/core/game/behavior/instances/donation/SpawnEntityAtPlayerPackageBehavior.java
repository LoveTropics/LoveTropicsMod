package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class SpawnEntityAtPlayerPackageBehavior implements IGameBehavior {
	public static final Codec<SpawnEntityAtPlayerPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				ForgeRegistries.ENTITIES.getCodec().fieldOf("entity_id").forGetter(c -> c.entityId),
				Codec.INT.optionalFieldOf("damage_player_amount", 0).forGetter(c -> c.damagePlayerAmount),
				Codec.DOUBLE.optionalFieldOf("distance", 0.0).forGetter(c -> c.distance)
		).apply(instance, SpawnEntityAtPlayerPackageBehavior::new);
	});

	private final EntityType<?> entityId;
	private final int damagePlayerAmount;
	private final double distance;

	public SpawnEntityAtPlayerPackageBehavior(final EntityType<?> entityId, final int damagePlayerAmount, double distance) {
		this.entityId = entityId;
		this.damagePlayerAmount = damagePlayerAmount;
		this.distance = distance;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GamePackageEvents.APPLY_PACKAGE_TO_PLAYER, (player, sendingPlayer) -> {
			Vec3 spawnPos = findSpawnPos(game, player);
			if (spawnPos == null) {
				spawnPos = player.position();
			}

			Util.spawnEntity(entityId, player.getLevel(), spawnPos.x, spawnPos.y, spawnPos.z);
			if (damagePlayerAmount > 0) {
				player.hurt(DamageSource.GENERIC, damagePlayerAmount);
			}

			return true;
		});
	}

	@Nullable
	private Vec3 findSpawnPos(IGamePhase game, ServerPlayer player) {
		for (int i = 0; i < 10; i++) {
			double angle = player.getRandom().nextDouble() * 2 * Math.PI;
			double x = player.getX() + Math.sin(angle) * distance;
			double z = player.getZ() + Math.cos(angle) * distance;
			int maxDistanceY = Mth.floor(distance);

			BlockPos groundPos = Util.findGround(game.getWorld(), new BlockPos(x, player.getY(), z), maxDistanceY);
			if (groundPos != null) {
				return new Vec3(x, groundPos.getY(), z);
			}
		}

		return null;
	}
}
