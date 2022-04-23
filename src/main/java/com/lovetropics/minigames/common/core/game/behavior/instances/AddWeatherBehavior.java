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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

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

	private void onTickPlayer(ServerPlayer player) {
		WeatherEventType event = weather.getEventType();
		if (event != null) {
			this.tickPlayerInEvent(player, event);
		}
	}

	private void tickPlayerInEvent(ServerPlayer player, WeatherEventType event) {
		EventEffects config = eventEffects.get(event);
		if (config != null) {
			this.tickEventEffects(player, config);
		}
	}

	private void tickEventEffects(ServerPlayer player, EventEffects config) {
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
				player.addEffect(new MobEffectInstance(potion.effect));
			}
		}
	}

	private static boolean isPlayerSheltered(ServerPlayer player) {
		int x = Mth.floor(player.getX());
		int y = Mth.floor(player.getY() + player.getEyeHeight());
		int z = Mth.floor(player.getZ());
		return player.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) > y;
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

		public boolean tryUseRepellent(ServerPlayer player) {
			return repellent != null && repellent.tryUse(player);
		}
	}

	public static final class Repellent {
		public static final Codec<Repellent> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(c -> c.item),
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

		public boolean tryUse(ServerPlayer player) {
			ItemStack mainHand = player.getMainHandItem();
			ItemStack offhand = player.getOffhandItem();
			if (mainHand.getItem() == item) {
				if (player.tickCount % damageRate == 0) {
					mainHand.hurtAndBreak(damageAmount, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
				}
				return true;
			} else if (offhand.getItem() == item) {
				if (player.tickCount % damageRate == 0) {
					offhand.hurtAndBreak(damageAmount, player, p -> p.broadcastBreakEvent(InteractionHand.OFF_HAND));
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

		private final MobEffectInstance effect;
		private final int rate;

		public Potion(MobEffectInstance effect, int rate) {
			this.effect = effect;
			this.rate = rate;
		}
	}
}
