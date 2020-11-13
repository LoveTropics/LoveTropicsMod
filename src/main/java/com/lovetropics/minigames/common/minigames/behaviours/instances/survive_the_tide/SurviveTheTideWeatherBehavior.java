package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.item.MinigameItems;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.behaviours.instances.PhasesMinigameBehavior;
import com.lovetropics.minigames.common.minigames.weather.RainType;
import com.lovetropics.minigames.common.minigames.weather.SurviveTheTideWeatherConfig;
import com.lovetropics.minigames.common.minigames.weather.WeatherController;
import com.lovetropics.minigames.common.minigames.weather.WeatherControllerManager;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

import java.util.Random;

public class SurviveTheTideWeatherBehavior implements IMinigameBehavior {
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

	public SurviveTheTideWeatherBehavior(final SurviveTheTideWeatherConfig config) {
		this.config = config;
	}

	public static <T> SurviveTheTideWeatherBehavior parse(Dynamic<T> root) {
		return new SurviveTheTideWeatherBehavior(SurviveTheTideWeatherConfig.parse(root));
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		controller = WeatherControllerManager.forWorld(minigame.getWorld());
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		PhasesMinigameBehavior phases = minigame.getOneBehavior(MinigameBehaviorTypes.PHASES.get()).orElse(null);
		SurviveTheTideRulesetBehavior rules = minigame.getOneBehavior(MinigameBehaviorTypes.SURVIVE_THE_TIDE_RULESET.get()).orElse(null);
		if (phases == null || rules == null) {
			return;
		}

		PhasesMinigameBehavior.MinigamePhase phase = phases.getCurrentPhase();

		if (world.getGameTime() % 20 == 0) {
			if (!specialWeatherActive()) {
				if (random.nextFloat() <= config.getRainHeavyChance(phase.getKey())) {
					heavyRainfallStart(phase);
				} else if (random.nextFloat() <= config.getRainAcidChance(phase.getKey())) {
					acidRainStart(phase);
				} else if (random.nextFloat() <= config.getHeatwaveChance(phase.getKey())) {
					heatwaveStart(phase);
				}
			}

			controller.setWind(config.getWindSpeed(phase.getKey()));
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

		if (specialWeatherActive() && !heatwaveActive()) {
			world.getWorldInfo().setRaining(true);
			world.getWorldInfo().setThundering(true);
		} else {
			world.getWorldInfo().setRaining(false);
			world.getWorldInfo().setThundering(false);
		}
	}

	@Override
	public void onParticipantUpdate(IMinigameInstance minigame, ServerPlayerEntity player) {
		if (acidRainActive() && !isPlayerSheltered(player)) {
			if (player.world.getGameTime() % config.getAcidRainDamageRate() == 0) {
				if (!isPlayerHolding(player, MinigameItems.ACID_REPELLENT_UMBRELLA.get())) {
					player.attackEntityFrom(DamageSource.GENERIC, config.getAcidRainDamage());
				} else {
					damageHeldOrOffhandItem(player, MinigameItems.ACID_REPELLENT_UMBRELLA.get(), (int) (1 * (config.getAcidRainDamageRate() / 20)));
				}
			}
		} else if (heatwaveActive() && !isPlayerSheltered(player)) {
			if (!isPlayerHolding(player, MinigameItems.SUPER_SUNSCREEN.get())) {
				player.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 5, 1, true, false, true));
			} else {
				if (player.world.getWorldInfo().getGameTime() % (20 * 3) == 0) {
					damageHeldOrOffhandItem(player, MinigameItems.SUPER_SUNSCREEN.get(), 3);
				}
			}
		}
	}

	private static boolean isPlayerSheltered(ServerPlayerEntity player) {
		int x = MathHelper.floor(player.getPosX());
		int y = MathHelper.floor(player.getPosY());
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

	@Override
	public void onFinish(final IMinigameInstance minigame) {
		controller.reset();
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
	private void heavyRainfallStart(PhasesMinigameBehavior.MinigamePhase phase) {
		heavyRainfallTime = config.getRainHeavyMinTime() + random.nextInt(config.getRainHeavyExtraRandTime());
		if (phase.is("phase4")) {
			heavyRainfallTime /= 2;
		}
	}

	private void acidRainStart(PhasesMinigameBehavior.MinigamePhase phase) {
		acidRainTime = config.getRainAcidMinTime() + random.nextInt(config.getRainAcidExtraRandTime());
		if (phase.is("phase4")) {
			acidRainTime /= 2;
		}
	}

	private void heatwaveStart(PhasesMinigameBehavior.MinigamePhase phase) {
		heatwaveTime = config.getHeatwaveMinTime() + random.nextInt(config.getHeatwaveExtraRandTime());
		if (phase.is("phase4")) {
			heatwaveTime /= 2;
		}
	}

	@Override
	public boolean onGamePackageReceived(IMinigameInstance minigame, GamePackage gamePackage) {
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
