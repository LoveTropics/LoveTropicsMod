package com.lovetropics.minigames.common.core.game.behavior.event;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;

public final class GameLivingEntityEvents {
	public static final GameEventType<Tick> TICK = GameEventType.create(Tick.class, listeners -> entity -> {
		for (Tick listener : listeners) {
			listener.tick(entity);
		}
	});

	public static final GameEventType<Death> DEATH = GameEventType.create(Death.class, listeners -> (entity, damageSource) -> {
		for (Death listener : listeners) {
			ActionResultType result = listener.onDeath(entity, damageSource);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}

		return ActionResultType.PASS;
	});

	public static final GameEventType<MobDrop> MOB_DROP = GameEventType.create(MobDrop.class, listeners -> (entity, damageSource, drops) -> {
		for (MobDrop listener : listeners) {
			ActionResultType result = listener.onMobDrop(entity, damageSource, drops);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}

		return ActionResultType.PASS;
	});

	public static final GameEventType<FarmlandTrample> FARMLAND_TRAMPLE = GameEventType.create(FarmlandTrample.class, listeners -> (entity, pos, state) -> {
		for (FarmlandTrample listener : listeners) {
			ActionResultType result = listener.onFarmlandTrample(entity, pos, state);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}

		return ActionResultType.PASS;
	});

	private GameLivingEntityEvents() {
	}

	public interface Tick {
		void tick(LivingEntity entity);
	}

	public interface Death {
		ActionResultType onDeath(LivingEntity entity, DamageSource damageSource);
	}

	public interface MobDrop {
		ActionResultType onMobDrop(LivingEntity entity, DamageSource damageSource, Collection<ItemEntity> drops);
	}

	public interface FarmlandTrample {
		ActionResultType onFarmlandTrample(Entity entity, BlockPos pos, BlockState state);
	}
}
