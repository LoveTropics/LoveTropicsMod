package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.FriendlyExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.CurrencyManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.dimension.DimensionUtils;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;

// TODO: needs to be split up & data-driven more!
public final class BbBehavior implements IGameBehavior {
	public static final Codec<BbBehavior> CODEC = RecordCodecBuilder.create(instance -> {
	    return instance.group(
	        Codec.INT.fieldOf("initial_currency").forGetter(c -> c.initialCurrency)
	    ).apply(instance, BbBehavior::new);
	});

	private static final Object2FloatMap<Difficulty> DEATH_DECREASE = new Object2FloatOpenHashMap<>();

	static {
		DEATH_DECREASE.put(Difficulty.EASY, 0.9f);
		DEATH_DECREASE.put(Difficulty.NORMAL, 0.8F);
		DEATH_DECREASE.put(Difficulty.HARD, 0.5F);
	}

	private final int initialCurrency;

	private IGamePhase game;
	private PlotsState plots;

	public BbBehavior(int initialCurrency) {
		this.initialCurrency = initialCurrency;
	}

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
		events.listen(GameLivingEntityEvents.FARMLAND_TRAMPLE, (e, p, s) -> ActionResultType.FAIL);
		events.listen(GameEntityEvents.MOUNTED, (mounting, beingMounted) -> {
			if (mounting instanceof ServerPlayerEntity) {
				return ActionResultType.PASS;
			} else {
				return ActionResultType.FAIL;
			}
		});

		events.listen(GamePlayerEvents.PLACE_BLOCK, this::onPlaceBlock);
		events.listen(GamePlayerEvents.BREAK_BLOCK, (player, pos, state, hand) -> ActionResultType.FAIL);

		events.listen(GamePlayerEvents.THROW_ITEM, (player, item) -> {
			ItemStack stack = item.getItem();
			if (stack.getItem() == BiodiversityBlitz.OSA_POINT.get()) {
				player.inventory.addItemStackToInventory(stack);
				player.sendContainerToPlayer(player.openContainer);
				return ActionResultType.FAIL;
			}
			return ActionResultType.PASS;
		});
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

		CurrencyManager.set(player, initialCurrency);
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
		player.getServerWorld().playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS,  1.0F, 1.0F);
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
		Difficulty difficulty = game.getWorld().getDifficulty();

		int oldCurrency = CurrencyManager.get(player);
		int newCurrency = MathHelper.floor(oldCurrency * DEATH_DECREASE.getFloat(difficulty));

		CurrencyManager.set(player, newCurrency);

		// Add the remaining items
		player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.18F, 1.0F);
		player.sendStatusMessage(BiodiversityBlitzTexts.deathDecrease(oldCurrency - newCurrency).mergeStyle(TextFormatting.RED), true);

		return ActionResultType.FAIL;
	}

	private void tick(IGamePhase game) {
		for (ServerPlayerEntity player : game.getParticipants()) {
			Plot plot = plots.getPlotFor(player);
			if (plot != null) {
				game.invoker(BbEvents.TICK_PLOT).onTickPlot(player, plot);
			}
		}
	}

	private void teleportToRegion(ServerPlayerEntity player, BlockBox region, Direction direction) {
		BlockPos pos = region.sample(player.getRNG());

		player.rotationYaw = direction.getHorizontalAngle();
		DimensionUtils.teleportPlayerNoPortal(player, game.getDimension(), pos);
	}
}
