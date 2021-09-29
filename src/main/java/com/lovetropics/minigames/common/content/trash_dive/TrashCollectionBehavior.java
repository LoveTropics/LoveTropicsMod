package com.lovetropics.minigames.common.content.trash_dive;

import com.lovetropics.minigames.common.content.block.TrashType;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.*;
import com.lovetropics.minigames.common.core.game.util.GameSidebar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
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
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TrashCollectionBehavior implements IGameBehavior {
	public static final Codec<TrashCollectionBehavior> CODEC = Codec.unit(TrashCollectionBehavior::new);

	private final Set<Block> trashBlocks;

	private GlobalGameWidgets widgets;

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
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		widgets = GlobalGameWidgets.registerTo(game, events);

		events.listen(GamePhaseEvents.START, () -> onStart(game));
		events.listen(GamePhaseEvents.FINISH, () -> triggerGameOver(game));
		events.listen(GamePlayerEvents.ADD, player -> onAddPlayer(game, player));
		events.listen(GamePlayerEvents.LEFT_CLICK_BLOCK, (player, world, pos) -> onPlayerLeftClickBlock(game, player, pos));
		events.listen(GamePlayerEvents.BREAK_BLOCK, this::onPlayerBreakBlock);

		events.listen(GameLogicEvents.GAME_OVER, () -> triggerGameOver(game));
	}

	private void onStart(IGamePhase game) {
		ITextComponent sidebarTitle = new StringTextComponent("Trash Dive")
				.mergeStyle(TextFormatting.BLUE, TextFormatting.BOLD);

		sidebar = widgets.openSidebar(sidebarTitle);
		sidebar.set(renderSidebar(game));

		PlayerSet players = game.getParticipants();
		players.addPotionEffect(new EffectInstance(Effects.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));

		for (ServerPlayerEntity player : players) {
			player.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.TURTLE_HELMET));
		}
	}

	private void onAddPlayer(IGamePhase game, ServerPlayerEntity player) {
		GameStatistics statistics = game.getStatistics();
		statistics.forPlayer(player).set(StatisticKey.TRASH_COLLECTED, 0);
	}

	private void onPlayerLeftClickBlock(IGamePhase game, ServerPlayerEntity player, BlockPos pos) {
		ServerWorld world = game.getWorld();

		BlockState state = world.getBlockState(pos);
		if (!isTrash(state)) {
			return;
		}

		world.removeBlock(pos, false);
		player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

		GameStatistics statistics = game.getStatistics();
		statistics.forPlayer(player)
				.withDefault(StatisticKey.TRASH_COLLECTED, () -> 0)
				.apply(collected -> collected + 1);

		collectedTrash++;

		sidebar.set(renderSidebar(game));
	}

	private ActionResultType onPlayerBreakBlock(ServerPlayerEntity player, BlockPos pos, BlockState state) {
		return isTrash(state) ? ActionResultType.PASS : ActionResultType.FAIL;
	}

	private boolean isTrash(BlockState state) {
		return trashBlocks.contains(state.getBlock());
	}

	private void triggerGameOver(IGamePhase game) {
		if (gameOver) return;

		gameOver = true;

		ITextComponent finishMessage = new StringTextComponent("The game ended! Here are the results for this game:");

		PlayerSet players = game.getAllPlayers();
		players.sendMessage(finishMessage.deepCopy().mergeStyle(TextFormatting.GREEN));

		GameStatistics statistics = game.getStatistics();

		int totalTimeSeconds = (int) (game.ticks() / 20);
		int totalTrashCollected = 0;

		for (PlayerKey player : statistics.getPlayers()) {
			totalTimeSeconds += statistics.forPlayer(player).getOr(StatisticKey.TRASH_COLLECTED, 0);
		}

		StatisticsMap globalStatistics = statistics.global();
		globalStatistics.set(StatisticKey.TOTAL_TIME, totalTimeSeconds);
		globalStatistics.set(StatisticKey.TRASH_COLLECTED, totalTrashCollected);

		PlayerPlacement.Score<Integer> placement = PlayerPlacement.fromMaxScore(game, StatisticKey.TRASH_COLLECTED);
		placement.placeInto(StatisticKey.PLACEMENT);
		placement.sendTo(players, 5);
	}

	private String[] renderSidebar(IGamePhase game) {
		List<String> sidebar = new ArrayList<>(10);
		sidebar.add(TextFormatting.GREEN + "Pick up trash! " + TextFormatting.GRAY + collectedTrash + " collected");

		PlayerPlacement.Score<Integer> placement = PlayerPlacement.fromMaxScore(game, StatisticKey.TRASH_COLLECTED);

		sidebar.add("");
		sidebar.add(TextFormatting.GREEN + "MVPs:");

		placement.addToSidebar(sidebar, 5);

		return sidebar.toArray(new String[0]);
	}
}
