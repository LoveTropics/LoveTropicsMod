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
import net.minecraft.util.registry.Registry;

public class SpawnEntityAtPlayerPackageBehavior implements IGameBehavior {
	public static final Codec<SpawnEntityAtPlayerPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ENTITY_TYPE.fieldOf("entity_id").forGetter(c -> c.entityId),
				Codec.INT.optionalFieldOf("damage_player_amount", 0).forGetter(c -> c.damagePlayerAmount)
		).apply(instance, SpawnEntityAtPlayerPackageBehavior::new);
	});

	private final EntityType<?> entityId;
	private final int damagePlayerAmount;

	public SpawnEntityAtPlayerPackageBehavior(final EntityType<?> entityId, final int damagePlayerAmount) {
		this.entityId = entityId;
		this.damagePlayerAmount = damagePlayerAmount;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GamePackageEvents.APPLY_PACKAGE, (player, sendingPlayer) -> {
			Util.spawnEntity(entityId, player.getServerWorld(), player.getPosX(), player.getPosY(), player.getPosZ());
			if (damagePlayerAmount > 0) {
				player.attackEntityFrom(DamageSource.GENERIC, damagePlayerAmount);
			}
		});
	}
}
