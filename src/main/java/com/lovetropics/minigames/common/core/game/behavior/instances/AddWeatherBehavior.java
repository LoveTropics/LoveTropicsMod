package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.weather.GameWeatherState;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.Heightmap;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public final class AddWeatherBehavior implements IGameBehavior {
	public static final Codec<AddWeatherBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(WeatherEventType.CODEC, EventEffects.CODEC).fieldOf("event_effects").forGetter(c -> c.eventEffects)
		).apply(instance, AddWeatherBehavior::new);
	});

	private final Map<WeatherEventType, EventEffects> eventEffects;

	private GameWeatherState weather;

	public AddWeatherBehavior(Map<WeatherEventType, EventEffects> eventEffects) {
		this.eventEffects = eventEffects;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		WeatherController controller = WeatherControllerManager.forWorld(game.getWorld());
		weather = state.register(GameWeatherState.KEY, new GameWeatherState(controller));
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		weather = game.getState().getOrThrow(GameWeatherState.KEY);

		events.listen(GamePhaseEvents.TICK, () -> weather.tick());
		events.listen(GamePhaseEvents.STOP, reason -> weather.clear());

		events.listen(GamePlayerEvents.TICK, this::onTickPlayer);
	}

	private void onTickPlayer(ServerPlayerEntity player) {
		WeatherEventType event = weather.getEventType();
		if (event != null) {
			this.tickPlayerInEvent(player, event);
		}
	}

	private void tickPlayerInEvent(ServerPlayerEntity player, WeatherEventType event) {
		EventEffects config = eventEffects.get(event);
		if (config != null) {
			this.tickEventEffects(player, config);
		}
	}

	private void tickEventEffects(ServerPlayerEntity player, EventEffects config) {
		if (isPlayerSheltered(player)) {
			return;
		}

		if (config.tryUseRepellent(player)) {
			return;
		}

		Damage damage = config.damage;
		if (damage != null) {
			if (player.tickCount % damage.rate == 0) {
				// TODO: damage source
				player.hurt(DamageSource.GENERIC, damage.amount);
			}
		}

		Potion potion = config.potion;
		if (potion != null) {
			if (player.tickCount % potion.rate == 0) {
				player.addEffect(new EffectInstance(potion.effect));
			}
		}
	}

	private static boolean isPlayerSheltered(ServerPlayerEntity player) {
		int x = MathHelper.floor(player.getX());
		int y = MathHelper.floor(player.getY() + player.getEyeHeight());
		int z = MathHelper.floor(player.getZ());
		return player.level.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z) > y;
	}

	public static final class EventEffects {
		public static final Codec<EventEffects> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Repellent.CODEC.optionalFieldOf("repellent").forGetter(c -> Optional.ofNullable(c.repellent)),
					Damage.CODEC.optionalFieldOf("damage").forGetter(c -> Optional.ofNullable(c.damage)),
					Potion.CODEC.optionalFieldOf("potion").forGetter(c -> Optional.ofNullable(c.potion))
			).apply(instance, (repellent, damage, potion) -> {
				return new EventEffects(repellent.orElse(null), damage.orElse(null), potion.orElse(null));
			});
		});

		@Nullable
		private final Repellent repellent;
		@Nullable
		private final Damage damage;
		@Nullable
		private final Potion potion;

		public EventEffects(@Nullable Repellent repellent, @Nullable Damage damage, @Nullable Potion potion) {
			this.repellent = repellent;
			this.damage = damage;
			this.potion = potion;
		}

		public boolean tryUseRepellent(ServerPlayerEntity player) {
			return repellent != null && repellent.tryUse(player);
		}
	}

	public static final class Repellent {
		public static final Codec<Repellent> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Registry.ITEM.fieldOf("item").forGetter(c -> c.item),
					Codec.INT.fieldOf("rate").forGetter(c -> c.damageRate),
					Codec.INT.fieldOf("amount").forGetter(c -> c.damageAmount)
			).apply(instance, Repellent::new);
		});

		private final Item item;
		private final int damageRate;
		private final int damageAmount;

		public Repellent(Item item, int damageRate, int damageAmount) {
			this.item = item;
			this.damageRate = damageRate;
			this.damageAmount = damageAmount;
		}

		public boolean tryUse(ServerPlayerEntity player) {
			ItemStack mainHand = player.getMainHandItem();
			ItemStack offhand = player.getOffhandItem();
			if (mainHand.getItem() == item) {
				if (player.tickCount % damageRate == 0) {
					mainHand.hurtAndBreak(damageAmount, player, p -> p.broadcastBreakEvent(Hand.MAIN_HAND));
				}
				return true;
			} else if (offhand.getItem() == item) {
				if (player.tickCount % damageRate == 0) {
					offhand.hurtAndBreak(damageAmount, player, p -> p.broadcastBreakEvent(Hand.OFF_HAND));
				}
				return true;
			}
			return false;
		}
	}

	public static final class Damage {
		public static final Codec<Damage> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.INT.fieldOf("rate").forGetter(c -> c.rate),
					Codec.FLOAT.fieldOf("amount").forGetter(c -> c.amount)
			).apply(instance, Damage::new);
		});

		private final int rate;
		private final float amount;

		public Damage(int rate, float amount) {
			this.rate = rate;
			this.amount = amount;
		}
	}

	public static final class Potion {
		public static final Codec<Potion> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					MoreCodecs.EFFECT_INSTANCE.fieldOf("effect").forGetter(c -> c.effect),
					Codec.INT.fieldOf("rate").forGetter(c -> c.rate)
			).apply(instance, Potion::new);
		});

		private final EffectInstance effect;
		private final int rate;

		public Potion(EffectInstance effect, int rate) {
			this.effect = effect;
			this.rate = rate;
		}
	}
}
