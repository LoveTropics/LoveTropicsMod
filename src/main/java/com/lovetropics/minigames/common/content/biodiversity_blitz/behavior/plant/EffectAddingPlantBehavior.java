package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public final class EffectAddingPlantBehavior implements IGameBehavior {
	public static final Codec<EffectAddingPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MoreCodecs.EFFECT_INSTANCE.fieldOf("effect").forGetter(c -> c.effect),
			Codec.DOUBLE.fieldOf("radius").forGetter(c -> c.radius)
	).apply(instance, EffectAddingPlantBehavior::new));

	private final MobEffectInstance effect;
	private final double radius;

	public EffectAddingPlantBehavior(MobEffectInstance effect, double radius) {
		this.effect = effect;
		this.radius = radius;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		int effectDuration = this.effect.getDuration();
		int applyTime = Math.max(effectDuration - 5, 1);

		events.listen(BbPlantEvents.TICK, (player, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % applyTime != 0) {
				return;
			}

			ServerLevel world = game.getWorld();

			for (Plant plant : plants) {
				AABB applyBounds = plant.coverage().asBounds().inflate(this.radius);

				List<Mob> entities = world.getEntitiesOfClass(Mob.class, applyBounds, BbMobEntity.PREDICATE);
				for (Mob entity : entities) {
					entity.addEffect(new MobEffectInstance(this.effect));
				}
			}
		});
	}
}
