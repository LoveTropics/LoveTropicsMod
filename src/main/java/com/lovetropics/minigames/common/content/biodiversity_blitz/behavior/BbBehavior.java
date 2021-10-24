package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.FriendlyExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

// TODO: needs to be split up & data-driven more!
public final class BbBehavior implements IGameBehavior {
	public static final Codec<BbBehavior> CODEC = Codec.unit(BbBehavior::new);

	private static final Object2FloatMap<Difficulty> DEATH_DECREASE = new Object2FloatOpenHashMap<>();

	static {
		DEATH_DECREASE.put(Difficulty.EASY, 0.9f);
		DEATH_DECREASE.put(Difficulty.NORMAL, 0.8F);
		DEATH_DECREASE.put(Difficulty.HARD, 0.5F);
	}

	private IGamePhase game;
	private PlotsState plots;

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(GamePlayerEvents.ADD, player -> setupPlayerAsRole(player, null));
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> setupPlayerAsRole(player, role));
		events.listen(BbEvents.ASSIGN_PLOT, this::onAssignPlot);
		events.listen(GamePhaseEvents.TICK, () -> tick(game));
		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
		events.listen(GameWorldEvents.EXPLOSION_DETONATE, this::onExplosion);
		// Don't grow any trees- we handle that ourselves
		events.listen(GameWorldEvents.SAPLING_GROW, (w, p) -> ActionResultType.FAIL);
		events.listen(GamePlayerEvents.ATTACK, this::onAttack);
		// No mob drops
		events.listen(GameLivingEntityEvents.MOB_DROP, (e, d, r) -> ActionResultType.FAIL);
		events.listen(GameLivingEntityEvents.FARMLAND_TRAMPLE, this::onFarmlandTrample);

		events.listen(GamePlayerEvents.PLACE_BLOCK, this::onPlaceBlock);
		events.listen(GamePlayerEvents.BREAK_BLOCK, (player, pos, state, hand) -> ActionResultType.FAIL);
	}

	private void setupPlayerAsRole(ServerPlayerEntity player, @Nullable PlayerRole role) {
		if (role == PlayerRole.SPECTATOR) {
			this.spawnSpectator(player);
		}
	}

	private void spawnSpectator(ServerPlayerEntity player) {
		Plot plot = plots.getRandomPlot(player.getRNG());
		if (plot != null) {
			teleportToRegion(player, plot.plantBounds, plot.forward);
		}

		player.setGameType(GameType.SPECTATOR);
	}

	private void onAssignPlot(ServerPlayerEntity player, Plot plot) {
		teleportToRegion(player, plot.spawn, plot.spawnForward);
	}

	private void onExplosion(Explosion explosion, List<BlockPos> affectedBlocks, List<Entity> affectedEntities) {
		// Remove players from friendly explosions
		if (explosion instanceof FriendlyExplosion) {
			affectedEntities.removeIf(e -> e instanceof ServerPlayerEntity);
		}

		affectedEntities.removeIf(e -> e instanceof VillagerEntity);

		// Blocks should not explode
		affectedBlocks.clear();
	}

	private ActionResultType onAttack(ServerPlayerEntity player, Entity target) {
		// disable pvp and pvv (player vs villager)
		if (target instanceof VillagerEntity || target instanceof PlayerEntity) {
			return ActionResultType.FAIL;
		}

		return ActionResultType.PASS;
	}

	private ActionResultType onPlaceBlock(ServerPlayerEntity player, BlockPos pos, BlockState placed, BlockState placedOn) {
		Plot plot = plots.getPlotFor(player);
		if (plot != null && plot.bounds.contains(pos)) {
			return this.onPlaceBlockInOwnPlot(player, pos, placed, plot);
		} else {
			this.sendActionRejection(player, BiodiversityBlitzTexts.notYourPlot());
			return ActionResultType.FAIL;
		}
	}

	private ActionResultType onPlaceBlockInOwnPlot(ServerPlayerEntity player, BlockPos pos, BlockState placed, Plot plot) {
		if (placed.matchesBlock(Blocks.FARMLAND)) {
			player.world.setBlockState(pos, Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, 7));
			return ActionResultType.PASS;
		}

		if (plot.plantBounds.contains(pos)) {
			this.sendActionRejection(player, BiodiversityBlitzTexts.canOnlyPlacePlants());
		}

		return ActionResultType.FAIL;
	}

	private void sendActionRejection(ServerPlayerEntity player, IFormattableTextComponent message) {
		player.sendStatusMessage(message.mergeStyle(TextFormatting.RED), true);
		player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
	}

	private ActionResultType onFarmlandTrample(Entity entity, BlockPos pos, BlockState state) {
		if (entity instanceof ServerPlayerEntity) {
			Plot plot = plots.getPlotFor(entity);
			if (plot != null && !plot.bounds.contains(pos)) {
				return ActionResultType.FAIL;
			}
		}

		return ActionResultType.PASS;
	}

	private ActionResultType onPlayerDeath(ServerPlayerEntity player, DamageSource damageSource) {
		Plot plot = plots.getPlotFor(player);
		if (plot == null) {
			return ActionResultType.PASS;
		}

		teleportToRegion(player, plot.spawn, plot.spawnForward);
		player.setHealth(20.0F);

		// Resets all currency from the player's inventory and adds a new stack with 80% of the amount.
		// A better way of just removing 20% of the existing stacks could be done but this was chosen for the time being to save time
		int totalCount = player.inventory.func_234564_a_(stack -> stack.getItem() == Items.SUNFLOWER, -1, player.container.func_234641_j_());

		Difficulty difficulty = game.getWorld().getDifficulty();
		int targetCount = (int) (totalCount * DEATH_DECREASE.getFloat(difficulty));

		// First insert all the full stacks
		int stacks = targetCount / 64;
		for (int i = 0; i < stacks; i++) {
			player.addItemStackToInventory(new ItemStack(Items.SUNFLOWER, 64));
			// Reduce the target by 64 as we just inserted a full stack
			targetCount -= 64;
		}

		// Add the remaining items
		player.addItemStackToInventory(new ItemStack(Items.SUNFLOWER, targetCount));
		player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.18F, 0.1F);
		player.sendStatusMessage(BiodiversityBlitzTexts.deathDecrease(totalCount - targetCount).mergeStyle(TextFormatting.RED), false);

		return ActionResultType.FAIL;
	}

	// TODO: staggered tick per player because this much logic in a single tick is not ideal
	private void tick(IGamePhase game) {
		ServerWorld world = game.getWorld();
		long ticks = game.ticks();

		for (ServerPlayerEntity player : game.getParticipants()) {
			Plot plot = plots.getPlotFor(player);
			if (plot != null) {
				game.invoker(BbEvents.TICK_PLOT).onTickPlot(player, plot);
			}
		}

		// Drop currency every 30 seconds
		if (ticks % 600 == 0) {
			for (ServerPlayerEntity player : game.getParticipants()) {
				Plot plot = plots.getPlotFor(player);
				if (plot == null) continue;

				double value = this.computeCurrency(world, plot);
				this.givePlayerCurrency(world, player, value);
			}
		}
	}

	private double computeCurrency(ServerWorld world, Plot plot) {
		double value = 2;

		// TODO: calculate crops, plants, and trees separately
		Set<Block> uniqueBlocks = new ReferenceOpenHashSet<>();

		// TODO: reference plants instead of blocks!
		for (BlockPos pos : plot.plantBounds) {
			BlockState state = world.getBlockState(pos);
			uniqueBlocks.add(state.getBlock());

			if (state.getBlock() instanceof CropsBlock) {
				value += 0.05;
			} else if (state.getBlock() == Blocks.GRASS) {
				value += 0.025;
			} else if (state.getBlock() == Blocks.WITHER_ROSE) {
				value += 0.075;
			} else if (state.getBlock() == Blocks.BIRCH_LOG || state.getBlock() == Blocks.OAK_LOG) {
				value += 0.85;
			}

			// ...
		}

		// TODO: temp math equation
		value += (uniqueBlocks.size() / 4.0) * value;

		return value;
	}

	private void givePlayerCurrency(ServerWorld world, ServerPlayerEntity player, double value) {
		int count = MathHelper.floor(value);
		if (world.rand.nextDouble() < value - count) {
			count++;
		}

		player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, 0.24F, 1.0F);
		player.sendStatusMessage(BiodiversityBlitzTexts.currencyAddition(count), true);
		player.addItemStackToInventory(new ItemStack(Items.SUNFLOWER, count));
	}

	private void teleportToRegion(ServerPlayerEntity player, BlockBox region, Direction direction) {
		BlockPos pos = region.sample(player.getRNG());

		player.rotationYaw = direction.getHorizontalAngle();
		DimensionUtils.teleportPlayerNoPortal(player, game.getDimension(), pos);
	}
}
