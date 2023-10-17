package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.google.common.collect.Multimap;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.tutorial.TutorialState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientMobSpawnState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.explosion.FilteredExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.explosion.PlantAffectingExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.util.BbUtils;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticsMap;
import com.lovetropics.minigames.common.core.game.util.GameSidebar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// TODO: needs to be split up & data-driven more!

public final class BbBehavior implements IGameBehavior {
	public static final MapCodec<BbBehavior> CODEC = MapCodec.unit(BbBehavior::new);

	private IGamePhase game;
	private PlotsState plots;
	private TutorialState tutorial;
	private GameSidebar sidebar;
	private GlobalGameWidgets widgets;

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);
		this.tutorial = game.getState().getOrThrow(TutorialState.KEY);
		this.widgets = GlobalGameWidgets.registerTo(game, events);

		events.listen(GamePlayerEvents.ADD, player -> setupPlayerAsRole(player, null));
		events.listen(GamePlayerEvents.SPAWN, this::setupPlayerAsRole);
		events.listen(BbEvents.ASSIGN_PLOT, this::onAssignPlot);
		events.listen(GamePhaseEvents.TICK, () -> tick(game));
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
		events.listen(GameWorldEvents.EXPLOSION_DETONATE, this::onExplosion);
		// Don't grow any trees- we handle that ourselves
		events.listen(GameWorldEvents.SAPLING_GROW, (w, p) -> InteractionResult.FAIL);
		events.listen(GamePlayerEvents.ATTACK, this::onAttack);
		// No mob drops
		events.listen(GameLivingEntityEvents.MOB_DROP, (e, d, r) -> InteractionResult.FAIL);
		events.listen(GameLivingEntityEvents.FARMLAND_TRAMPLE, this::onTrampleFarmland);
		events.listen(GameEntityEvents.MOUNTED, (mounting, beingMounted) -> {
			if (mounting instanceof ServerPlayer) {
				return InteractionResult.PASS;
			} else {
				return InteractionResult.FAIL;
			}
		});
		events.listen(GamePlayerEvents.DAMAGE, (player, damageSource, amount) -> {
			Plot plot = this.plots.getPlotFor(player);
			if (plot == null) {
				return InteractionResult.PASS;
			}

			if (!plot.walls.getBounds().contains(player.position())) {
				return InteractionResult.FAIL;
			}

			// Don't damage players from sweet berry bushes or wither roses
			// TODO: reduce slowdown from bush
			if (damageSource.is(DamageTypes.SWEET_BERRY_BUSH) || damageSource.is(DamageTypes.WITHER)) {
				return InteractionResult.FAIL;
			}

			return InteractionResult.PASS;
		});

		events.listen(GamePlayerEvents.PLACE_BLOCK, this::onPlaceBlock);
		events.listen(GamePlayerEvents.BREAK_BLOCK, (player, pos, state, hand) -> InteractionResult.FAIL);

		events.listen(GamePlayerEvents.USE_BLOCK, this::onUseBlock);

		events.listen(GamePhaseEvents.START, () -> {
			Component sidebarTitle = Component.literal("Biodiversity Blitz")
					.withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD);

			sidebar = widgets.openSidebar(sidebarTitle);
			sidebar.set(collectScoreboard(game));
		});

		// TODO: this applies for every player in the game!
		events.listen(BbEvents.CURRENCY_ACCUMULATE, (player, value, lastValue) -> {
			// Change scoreboard only when player's currency changes
			sidebar.set(collectScoreboard(game));
		});

		events.listen(BbEvents.CURRENCY_INCREMENT_CHANGED, (player, value, lastValue) -> {
			// Change scoreboard only when player's currency changes
			sidebar.set(collectScoreboard(game));
		});
	}

	private Component[] collectScoreboard(IGamePhase game) {
		// FIXME: does not work with teams!
		List<Component> sidebar = new ArrayList<>(10);
		sidebar.add(Component.literal("Player ").withStyle(ChatFormatting.AQUA).append(Component.literal("points (+ per drop)").withStyle(ChatFormatting.GOLD)));
		sidebar.add(CommonComponents.EMPTY);

		List<ServerPlayer> list = new ArrayList<>();
		game.getParticipants().forEach(list::add);

		list.sort(Comparator.comparingInt(c -> this.game.getStatistics().forPlayer((ServerPlayer) c).getOr(StatisticKey.POINTS, 0)).reversed());

		int added = 0;
		for (ServerPlayer player : list) {
			// Limit leaderboard to 8 players- otherwise it gets too chaotic
			if (added >= 8) {
				sidebar.add(Component.literal("... and more!").withStyle(ChatFormatting.AQUA));
				break;
			}

			added++;
			int points = 0;
			int increment = 0;
			// Get points for player
			Plot plot = plots.getPlotFor(player);
			StatisticsMap stats = this.game.getStatistics().forPlayer(player);
			if (plot != null) {
				points = stats.getOr(StatisticKey.POINTS, 0);
				increment = plot.nextCurrencyIncrement;
			}

			sidebar.add(Component.literal(player.getGameProfile().getName() + ": ").withStyle(ChatFormatting.AQUA)
					.append(Component.literal(points + " (+ " + increment + ")").withStyle(ChatFormatting.GOLD)));
		}

		return sidebar.toArray(new Component[0]);
	}

	private InteractionResult onUseBlock(ServerPlayer player, ServerLevel world, BlockPos blockPos, InteractionHand hand, BlockHitResult blockRayTraceResult) {
		if (!tutorial.isTutorialFinished()) {
			return InteractionResult.FAIL;
		}

		Plot plot = this.plots.getPlotFor(player);
		BlockPos pos = blockRayTraceResult.getBlockPos();

		if (plot != null && plot.bounds.contains(pos)) {
			return this.onUseBlockInPlot(player, world, blockPos, hand, plot, pos);
		} else {
			return InteractionResult.FAIL;
		}
	}

	private InteractionResult onUseBlockInPlot(ServerPlayer player, ServerLevel world, BlockPos blockPos, InteractionHand hand, Plot plot, BlockPos pos) {
		BlockState state = world.getBlockState(pos);

		// Check if farmland is being used and the user has a hoe TODO: can we make it not hardcoded?
		if (state.getBlock() == Blocks.FARMLAND && player.getItemInHand(hand).getItem() instanceof HoeItem) {
			// If there is no plant above we can change to grass safely
			if (!plot.plants.hasPlantAt(pos.above())) {
				world.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
				world.playSound(null, blockPos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
				player.swing(hand);
				player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 3);
				return InteractionResult.SUCCESS;
			}
		}

		return InteractionResult.PASS;
	}

	private InteractionResult onTrampleFarmland(Entity entity, BlockPos pos, BlockState state) {
		if (!tutorial.isTutorialFinished()) {
			return InteractionResult.FAIL;
		}

		Plot plot = this.plots.getPlotFor(entity);
		if (plot != null && plot.bounds.contains(pos)) {
			if (!plot.plants.hasPlantAt(pos.above())) {
				return InteractionResult.PASS;
			}
		}

		return InteractionResult.FAIL;
	}

	private void setupPlayerAsRole(ServerPlayer player, @Nullable PlayerRole role) {
		if (role == PlayerRole.SPECTATOR) {
			this.spawnSpectator(player);
		}
	}

	private void spawnSpectator(ServerPlayer player) {
		Plot plot = plots.getRandomPlot(player.getRandom());
		if (plot != null) {
			teleportToRegion(player, plot.plantBounds, plot.forward);
		}

		player.setGameMode(GameType.SPECTATOR);
	}

	private void onAssignPlot(ServerPlayer player, Plot plot) {
		GameClientState.sendToPlayer(new ClientMobSpawnState(plot.mobSpawn), player);
		teleportToRegion(player, plot.spawn, plot.spawnForward);
	}

	private void onExplosion(Explosion explosion, List<BlockPos> affectedBlocks, List<Entity> affectedEntities) {
		// Remove from filtered explosions
		if (explosion instanceof FilteredExplosion) {
			affectedEntities.removeIf(((FilteredExplosion)explosion).remove);
		}

		if (explosion instanceof PlantAffectingExplosion) {
			((PlantAffectingExplosion)explosion).affectPlants(affectedBlocks);
		}

		// Blocks should not explode
		affectedBlocks.clear();
	}

	private InteractionResult onAttack(ServerPlayer player, Entity target) {
		if (!tutorial.isTutorialFinished()) {
			return InteractionResult.FAIL;
		}

		if (BbMobEntity.matches(target)) {
			Plot plot = plots.getPlotAt(target.blockPosition());
			if (plot != null && plot.walls.containsEntity(player)) {
				return InteractionResult.PASS;
			}
		}
		return InteractionResult.FAIL;
	}

	private InteractionResult onPlaceBlock(ServerPlayer player, BlockPos pos, BlockState placed, BlockState placedOn) {
		if (!tutorial.isTutorialFinished()) {
			return InteractionResult.FAIL;
		}

		Plot plot = plots.getPlotFor(player);
		if (plot != null && plot.bounds.contains(pos)) {
			// Don't let players place plants inside mob spawns
			if (plot.mobSpawn.contains(pos)) {
				this.sendActionRejection(player, BiodiversityBlitzTexts.plantCannotFit());
				return InteractionResult.FAIL;
			}

			return this.onPlaceBlockInOwnPlot(player, pos, placed, plot);
		} else {
			this.sendActionRejection(player, BiodiversityBlitzTexts.notYourPlot());
			return InteractionResult.FAIL;
		}
	}

	private InteractionResult onPlaceBlockInOwnPlot(ServerPlayer player, BlockPos pos, BlockState placed, Plot plot) {
		if (placed.is(Blocks.FARMLAND)) {
			player.level().setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7));
			return InteractionResult.PASS;
		}

		if (plot.plantBounds.contains(pos)) {
			this.sendActionRejection(player, BiodiversityBlitzTexts.canOnlyPlacePlants());
		}

		return InteractionResult.FAIL;
	}

	private void sendActionRejection(ServerPlayer player, MutableComponent message) {
		player.displayClientMessage(message.withStyle(ChatFormatting.RED), true);
		player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,  1.0F, 1.0F);
	}

	private InteractionResult onPlayerDeath(ServerPlayer player, DamageSource damageSource) {
		Plot plot = plots.getPlotFor(player);
		if (plot == null) {
			return InteractionResult.PASS;
		}

		teleportToRegion(player, plot.spawn, plot.spawnForward);
		player.setHealth(20.0F);
		if (player.getFoodData().getFoodLevel() < 10) {
			player.getFoodData().eat(2, 0.8f);
		}

		// We need to encapsulate the currency behavior's death event.
		// Using the standard event system means it'll never be called due to us needing to fail the event, so we duplicate it.
		game.invoker(BbEvents.BB_DEATH).onDeath(player, damageSource);

		player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.18F, 1.0F);
		player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 255, 80));

		player.connection.send(new ClientboundSetTitlesAnimationPacket(40, 20, 0));
        player.connection.send(new ClientboundSetTitleTextPacket(BiodiversityBlitzTexts.deathTitle().withStyle(ChatFormatting.RED)));

		return InteractionResult.FAIL;
	}

	private void tick(IGamePhase game) {
		Multimap<Plot, ServerPlayer> plots = BbUtils.reassociate(this.plots, game.getParticipants());

		for (Plot plot : plots.keySet()) {
			game.invoker(BbEvents.TICK_PLOT).onTickPlot(plots.get(plot), plot);
		}
	}

	private void teleportToRegion(ServerPlayer player, BlockBox region, Direction direction) {
		BlockPos pos = region.sample(player.getRandom());

		player.setYRot(direction.toYRot());
		DimensionUtils.teleportPlayerNoPortal(player, game.getDimension(), pos);
	}
}
