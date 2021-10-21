package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public final class EffectAddingPlantBehavior implements IGameBehavior {
	public static final Codec<EffectAddingPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MoreCodecs.EFFECT_INSTANCE.fieldOf("effect").forGetter(c -> c.effect),
			Codec.DOUBLE.fieldOf("radius").forGetter(c -> c.radius)
	).apply(instance, EffectAddingPlantBehavior::new));

	private final EffectInstance effect;
	private final double radius;

	public EffectAddingPlantBehavior(EffectInstance effect, double radius) {
		this.effect = effect;
		this.radius = radius;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		int effectDuration = this.effect.getDuration();
		int applyTime = Math.max(effectDuration - 5, 0);

		events.listen(MpEvents.TICK_PLANTS, (player, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % effectDuration != applyTime) {
				return;
			}

			ServerWorld world = game.getWorld();

			for (Plant plant : plants) {
				AxisAlignedBB applyBounds = plant.coverage().asBounds().grow(this.radius);

				List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, applyBounds, entity -> !(entity instanceof VillagerEntity));
				for (MobEntity entity : entities) {
					entity.addPotionEffect(new EffectInstance(this.effect));
				}
			}
		});
	}
}