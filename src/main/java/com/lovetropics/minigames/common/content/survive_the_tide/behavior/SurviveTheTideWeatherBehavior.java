package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTide;
import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTideWeatherConfig;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.state.instances.GamePhase;
import com.lovetropics.minigames.common.core.game.state.instances.GamePhaseState;
import com.lovetropics.minigames.common.core.game.weather.RainType;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;

import java.util.Random;

public class SurviveTheTideWeatherBehavior implements IGameBehavior {
	public static final Codec<SurviveTheTideWeatherBehavior> CODEC = SurviveTheTideWeatherConfig.CODEC.xmap(SurviveTheTideWeatherBehavior::new, b -> b.config);

	private final SurviveTheTideWeatherConfig config;
	private WeatherController controller;

	private final Random random = new Random();

	/**
	 * instantiate in IslandRoyaleMinigameDefinition
	 * - packet sync what is needed
	 * - setup instanced overrides on client
	 * <p>
	 * <p>
	 * phases:
	 * - 1: semi peacefull, maybe light rain/wind
	 * - 2: heavy wind, acid rain
	 * - 3: see doc, "an extreme storm encroaches the map slowly towards the centre"
	 * --- assuming can also do same things phase 2 does?
	 * <p>
	 * phases should be in IslandRoyaleMinigameDefinition for use in other places, and this class listens to them
	 * <p>
	 * rng that can happen:
	 * - wind, can operate independently of other rng events
	 * <p>
	 * rng that only allows 1 of them at a time:
	 * - extreme rain
	 * - acid rain
	 * - heat wave
	 * <p>
	 * heat wave:
	 * - player movement reduced if player pos can see sky
	 * <p>
	 * rain:
	 * - the usual
	 * <p>
	 * acid rain:
	 * - player damage over time
	 * - degrade items and armor over time
	 * - use normal rain visual too, color changed
	 * <p>
	 * extreme rain:
	 * - fog closes in
	 * - pump up weather2 effects
	 * - splashing noise while walking
	 * - use normal rain visual too
	 * <p>
	 * - consider design to factor in worn items to negate player effects
	 */

	//only one of these can be active at a time
	protected long heavyRainfallTime = 0;
	protected long acidRainTime = 0;
	protected long heatwaveTime = 0;

	protected GamePhaseState phases;

	public SurviveTheTideWeatherBehavior(final SurviveTheTideWeatherConfig config) {
		this.config = config;
	}

	@Override
	public void register(IGameInstance game, EventRegistrar events) {
		controller = WeatherControllerManager.forWorld(game.getWorld());

		events.listen(GameLifecycleEvents.TICK, this::tick);
		events.listen(GameLifecycleEvents.STOP, g -> controller.reset());

		events.listen(GamePlayerEvents.TICK, this::onParticipantUpdate);

		events.listen(GamePackageEvents.RECEIVE_PACKAGE, this::onPackageReceive);

		phases = game.getState().getOrNull(GamePhaseState.TYPE);
	}

	private void tick(final IGameInstance game) {
		if (phases == null) {
			return;
		}

		GamePhase phase = phases.get();

		ServerWorld world = game.getWorld();
		if (world.getGameTime() % 20 == 0) {
			if (!specialWeatherActive()) {
				if (random.nextFloat() <= config.getRainHeavyChance(phase.key)) {
					heavyRainfallStart(phase);
				} else if (random.nextFloat() <= config.getRainAcidChance(phase.key)) {
					acidRainStart(phase);
				} else if (random.nextFloat() <= config.getHeatwaveChance(phase.key)) {
					heatwaveStart(phase);
				}
			}

			controller.setWind(config.getWindSpeed(phase.key));
		}

		if (heavyRainfallTime > 0) {
			heavyRainfallTime--;
		}

		if (acidRainTime > 0) {
			acidRainTime--;
		}

		if (heatwaveTime > 0) {
			heatwaveTime--;
		}

		if (heavyRainfallTime > 0) {
			controller.setRain(1.0F, RainType.NORMAL);
		} else if (acidRainTime > 0) {
			controller.setRain(1.0F, RainType.ACID);
		} else {
			controller.setRain(0.0F, controller.getRainType());
		}

		controller.setHeatwave(heatwaveTime > 0);

		IServerWorldInfo worldInfo = (IServerWorldInfo) world.getWorldInfo();
		if (specialWeatherActive() && !heatwaveActive()) {
			worldInfo.setRaining(true);
			worldInfo.setThundering(true);
		} else {
			worldInfo.setRaining(false);
			worldInfo.setThundering(false);
		}
	}

	private void onParticipantUpdate(IGameInstance game, ServerPlayerEntity player) {
		if (acidRainActive() && !isPlayerSheltered(player)) {
			if (player.world.getGameTime() % config.getAcidRainDamageRate() == 0) {
				if (!isPlayerHolding(player, SurviveTheTide.ACID_REPELLENT_UMBRELLA.get())) {
					player.attackEntityFrom(DamageSource.GENERIC, config.getAcidRainDamage());
				} else {
					damageHeldOrOffhandItem(player, SurviveTheTide.ACID_REPELLENT_UMBRELLA.get(), (int) (1 * (config.getAcidRainDamageRate() / 20)));
				}
			}
		} else if (heatwaveActive() && !isPlayerSheltered(player)) {
			if (!isPlayerHolding(player, SurviveTheTide.SUPER_SUNSCREEN.get())) {
				player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 5, 1, true, false, true));
			} else {
				if (player.world.getWorldInfo().getGameTime() % (20 * 3) == 0) {
					damageHeldOrOffhandItem(player, SurviveTheTide.SUPER_SUNSCREEN.get(), 3);
				}
			}
		}
	}

	private static boolean isPlayerSheltered(ServerPlayerEntity player) {
		int x = MathHelper.floor(player.getPosX());
		int y = MathHelper.floor(player.getPosY() + player.getEyeHeight());
		int z = MathHelper.floor(player.getPosZ());
		return player.world.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z) > y;
	}

	private static boolean isPlayerHolding(ServerPlayerEntity player, Item item) {
		return player.getHeldItemMainhand().getItem() == item || player.getHeldItemOffhand().getItem() == item;
	}

	private static void damageHeldOrOffhandItem(ServerPlayerEntity player, Item item, int amount) {
		if (player.getHeldItemMainhand().getItem() == item) {
			player.getHeldItemMainhand().damageItem(amount, player, (p_226874_1_) -> {
				p_226874_1_.sendBreakAnimation(Hand.MAIN_HAND);
			});
		} else if (player.getHeldItemOffhand().getItem() == item) {
			player.getHeldItemOffhand().damageItem(amount, player, (p_226874_1_) -> {
				p_226874_1_.sendBreakAnimation(Hand.OFF_HAND);
			});
		}
	}

	private boolean heavyRainfallActive() {
		return heavyRainfallTime > 0;
	}

	private boolean acidRainActive() {
		return acidRainTime > 0;
	}

	private boolean heatwaveActive() {
		return heatwaveTime > 0;
	}

	private boolean specialWeatherActive() {
		return heavyRainfallActive() || acidRainActive() || heatwaveActive();
	}

	// TODO phase names
	private void heavyRainfallStart(GamePhase phase) {
		heavyRainfallTime = config.getRainHeavyMinTime() + random.nextInt(config.getRainHeavyExtraRandTime());
		if (phase.is("phase4")) {
			heavyRainfallTime /= 2;
		}
	}

	private void acidRainStart(GamePhase phase) {
		acidRainTime = config.getRainAcidMinTime() + random.nextInt(config.getRainAcidExtraRandTime());
		if (phase.is("phase4")) {
			acidRainTime /= 2;
		}
	}

	private void heatwaveStart(GamePhase phase) {
		heatwaveTime = config.getHeatwaveMinTime() + random.nextInt(config.getHeatwaveExtraRandTime());
		if (phase.is("phase4")) {
			heatwaveTime /= 2;
		}
	}

	private boolean onPackageReceive(IGameInstance game, GamePackage gamePackage) {
		// TODO: hardcoded
		String packageType = gamePackage.getPackageType();
		if (packageType.equals("acid_rain_event")) {
			heatwaveTime = 0;
			heavyRainfallTime = 0;
			acidRainTime = config.getRainAcidMinTime() + config.getRainAcidExtraRandTime() / 2;
			return true;
		} else if (packageType.equals("heatwave_event")) {
			acidRainTime = 0;
			heavyRainfallTime = 0;
			heatwaveTime = config.getHeatwaveMinTime() + config.getHeatwaveExtraRandTime() / 2;
			return true;
		}

		return false;
	}
}
