package com.lovetropics.minigames.common.minigames.behaviours.instances.trash_dive;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameSidebar;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.statistics.*;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
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
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public final class TrashCollectionBehavior implements IMinigameBehavior {
	private int totalTrash;
	private final LongSet remainingTrash = new LongOpenHashSet();

	private boolean gameOver;

	private MinigameSidebar sidebar;

	public static <T> TrashCollectionBehavior parse(Dynamic<T> root) {
		return new TrashCollectionBehavior();
	}

	@Override
	public ImmutableList<IMinigameBehaviorType<?>> dependencies() {
		return ImmutableList.of(MinigameBehaviorTypes.PLACE_TRASH.get());
	}

	@Override
	public void onStart(IMinigameInstance minigame) {
		PlaceTrashBehavior trash = minigame.getBehaviorOrThrow(MinigameBehaviorTypes.PLACE_TRASH.get());
		remainingTrash.addAll(trash.getTrashBlocks());
		totalTrash = remainingTrash.size();

		ITextComponent sidebarTitle = new StringTextComponent("Trash Dive")
				.applyTextStyles(TextFormatting.BLUE, TextFormatting.BOLD);

		sidebar = MinigameSidebar.open(sidebarTitle, minigame.getPlayers());
		sidebar.set(renderSidebar(minigame));

		minigame.getBehavior(MinigameBehaviorTypes.TIMED.get()).ifPresent(timed -> {
			timed.onFinish(this::triggerGameOver);
		});

		PlayerSet players = minigame.getPlayers();
		players.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));

		for (ServerPlayerEntity player : players) {
			player.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.TURTLE_HELMET));
		}
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		triggerGameOver(minigame);

		sidebar.close();
	}

	@Override
	public void onPlayerLeftClickBlock(IMinigameInstance minigame, ServerPlayerEntity player, BlockPos pos, PlayerInteractEvent.LeftClickBlock event) {
		if (!remainingTrash.remove(pos.toLong())) {
			event.setCanceled(true);
			return;
		}

		minigame.getWorld().removeBlock(pos, false);
		player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

		MinigameStatistics statistics = minigame.getStatistics();
		statistics.forPlayer(player)
				.withDefault(StatisticKey.TRASH_COLLECTED, () -> 0)
				.apply(collected -> collected + 1);

		sidebar.set(renderSidebar(minigame));

		if (remainingTrash.isEmpty()) {
			triggerGameOver(minigame);
		}
	}

	private void triggerGameOver(IMinigameInstance minigame) {
		if (gameOver) return;

		gameOver = true;

		ITextComponent finishMessage;
		if (remainingTrash.isEmpty()) {
			finishMessage = new StringTextComponent("We collected all the trash! Here are the results for this game:");
		} else {
			finishMessage = new StringTextComponent("We ran out of time! Here are the results for this game:");
		}

		PlayerSet players = minigame.getPlayers();
		players.sendMessage(finishMessage.applyTextStyles(TextFormatting.GREEN));

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

	private String[] renderSidebar(IMinigameInstance minigame) {
		int collectedTrash = totalTrash - remainingTrash.size();

		List<String> sidebar = new ArrayList<>(10);
		sidebar.add(TextFormatting.GREEN + "Pick up trash! " + TextFormatting.GRAY + collectedTrash + "/" + totalTrash);

		PlayerPlacement.Score<Integer> placement = PlayerPlacement.fromMaxScore(minigame, StatisticKey.TRASH_COLLECTED);

		sidebar.add("");
		sidebar.add(TextFormatting.GREEN + "Top players:");

		placement.addToSidebar(sidebar, 5);

		return sidebar.toArray(new String[0]);
	}
}
