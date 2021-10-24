package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

public class SpawnEntityAtPlayerPackageBehavior implements IGameBehavior {
	public static final Codec<SpawnEntityAtPlayerPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ENTITY_TYPE.fieldOf("entity_id").forGetter(c -> c.entityId),
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
		events.listen(GamePackageEvents.APPLY_PACKAGE, (player, sendingPlayer) -> {
			for (int i = 0; i < 10; i++) {
				double angle = player.getRNG().nextDouble() * 2 * Math.PI;
				double x = player.getPosX() + Math.sin(angle) * distance;
				double z = player.getPosZ() + Math.cos(angle) * distance;
				int maxDistanceY = MathHelper.floor(distance);

				BlockPos groundPos = Util.findGround(game.getWorld(), new BlockPos(x, player.getPosY(), z), maxDistanceY);
				if (groundPos == null) continue;

				Util.spawnEntity(entityId, player.getServerWorld(), x, groundPos.getY(), z);
				if (damagePlayerAmount > 0) {
					player.attackEntityFrom(DamageSource.GENERIC, damagePlayerAmount);
				}

				return true;
			}

			return false;
		});
	}
}
