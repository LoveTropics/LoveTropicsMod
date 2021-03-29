package com.lovetropics.minigames.common.content.trash_dive;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.content.block.TrashType;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.util.GameSidebar;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.instances.TimedGameBehavior;
import com.lovetropics.minigames.common.core.game.statistics.*;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TrashCollectionBehavior implements IGameBehavior {
	public static final Codec<TrashCollectionBehavior> CODEC = Codec.unit(TrashCollectionBehavior::new);

	private final Set<Block> trashBlocks;

	private boolean gameOver;
	private GameSidebar sidebar;

	private int collectedTrash;

	public TrashCollectionBehavior() {
		TrashType[] trashTypes = TrashType.values();
		trashBlocks = new ReferenceOpenHashSet<>();

		for (TrashType trash : trashTypes) {
			trashBlocks.add(trash.get());
		}
	}

	@Override
	public ImmutableList<GameBehaviorType<? extends IGameBehavior>> dependencies() {
		return ImmutableList.of(GameBehaviorTypes.PLACE_TRASH.get());
	}

	@Override
	public void onStart(IGameInstance minigame) {
		ITextComponent sidebarTitle = new StringTextComponent("Trash Dive")
				.mergeStyle(TextFormatting.BLUE, TextFormatting.BOLD);

		sidebar = GameSidebar.open(sidebarTitle, minigame.getPlayers());
		sidebar.set(renderSidebar(minigame));

		for (TimedGameBehavior timed : minigame.getBehaviors(GameBehaviorTypes.TIMED.get())) {
			timed.onFinish(this::triggerGameOver);
		}

		PlayerSet players = minigame.getPlayers();
		players.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));

		for (ServerPlayerEntity player : players) {
			player.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.TURTLE_HELMET));
		}
	}

	@Override
	public void onFinish(IGameInstance minigame) {
		triggerGameOver(minigame);

		sidebar.close();
	}

	@Override
	public void onPlayerJoin(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		MinigameStatistics statistics = minigame.getStatistics();
		statistics.forPlayer(player).set(StatisticKey.TRASH_COLLECTED, 0);
	}

	@Override
	public void onPlayerLeftClickBlock(IGameInstance minigame, ServerPlayerEntity player, BlockPos pos, PlayerInteractEvent.LeftClickBlock event) {
		ServerWorld world = minigame.getWorld();

		BlockState state = world.getBlockState(pos);
		if (!isTrash(state)) {
			return;
		}

		world.removeBlock(pos, false);
		player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

		MinigameStatistics statistics = minigame.getStatistics();
		statistics.forPlayer(player)
				.withDefault(StatisticKey.TRASH_COLLECTED, () -> 0)
				.apply(collected -> collected + 1);

		collectedTrash++;

		sidebar.set(renderSidebar(minigame));
	}

	@Override
	public void onPlayerBreakBlock(IGameInstance minigame, ServerPlayerEntity player, BlockPos pos, BlockState state, BlockEvent.BreakEvent event) {
		if (!isTrash(state)) {
			event.setCanceled(true);
		}
	}

	private boolean isTrash(BlockState state) {
		return trashBlocks.contains(state.getBlock());
	}

	private void triggerGameOver(IGameInstance minigame) {
		if (gameOver) return;

		gameOver = true;

		ITextComponent finishMessage = new StringTextComponent("The game ended! Here are the results for this game:");

		PlayerSet players = minigame.getPlayers();
		players.sendMessage(finishMessage.deepCopy().mergeStyle(TextFormatting.GREEN));

		MinigameStatistics statistics = minigame.getStatistics();

		int totalTimeSeconds = (int) (minigame.ticks() / 20);
		int totalTrashCollected = 0;

		for (PlayerKey player : statistics.getPlayers()) {
			totalTimeSeconds += statistics.forPlayer(player).getOr(StatisticKey.TRASH_COLLECTED, 0);
		}

		StatisticsMap globalStatistics = statistics.getGlobal();
		globalStatistics.set(StatisticKey.TOTAL_TIME, totalTimeSeconds);
		globalStatistics.set(StatisticKey.TRASH_COLLECTED, totalTrashCollected);

		PlayerPlacement.Score<Integer> placement = PlayerPlacement.fromMaxScore(minigame, StatisticKey.TRASH_COLLECTED);
		placement.placeInto(StatisticKey.PLACEMENT);
		placement.sendTo(players, 5);
	}

	private String[] renderSidebar(IGameInstance minigame) {
		List<String> sidebar = new ArrayList<>(10);
		sidebar.add(TextFormatting.GREEN + "Pick up trash! " + TextFormatting.GRAY + collectedTrash + " collected");

		PlayerPlacement.Score<Integer> placement = PlayerPlacement.fromMaxScore(minigame, StatisticKey.TRASH_COLLECTED);

		sidebar.add("");
		sidebar.add(TextFormatting.GREEN + "MVPs:");

		placement.addToSidebar(sidebar, 5);

		return sidebar.toArray(new String[0]);
	}
}
