package com.lovetropics.minigames.common.content.trash_dive;

import com.lovetropics.minigames.common.content.block.TrashType;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticsMap;
import com.lovetropics.minigames.common.core.game.util.GameSidebar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TrashCollectionBehavior implements IGameBehavior {
	public static final MapCodec<TrashCollectionBehavior> CODEC = MapCodec.unit(TrashCollectionBehavior::new);

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
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (role == PlayerRole.PARTICIPANT) {
				onAddPlayer(game, player);
			}
		});
		events.listen(GamePlayerEvents.LEFT_CLICK_BLOCK, (player, world, pos) -> onPlayerLeftClickBlock(game, player, pos));
		events.listen(GamePlayerEvents.BREAK_BLOCK, this::onPlayerBreakBlock);

		events.listen(GameLogicEvents.GAME_OVER, () -> triggerGameOver(game));
	}

	private void onStart(IGamePhase game) {
		sidebar = widgets.openSidebar(TrashDiveTexts.SIDEBAR_TITLE);
		sidebar.set(renderSidebar(game));

		PlayerSet players = game.getParticipants();
		players.addPotionEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, Integer.MAX_VALUE, 1, false, false));
	}

	private void onAddPlayer(IGamePhase game, ServerPlayer player) {
		GameStatistics statistics = game.getStatistics();
		statistics.forPlayer(player).set(StatisticKey.TRASH_COLLECTED, 0);
	}

	private void onPlayerLeftClickBlock(IGamePhase game, ServerPlayer player, BlockPos pos) {
		ServerLevel world = game.getWorld();

		BlockState state = world.getBlockState(pos);
		if (!isTrash(state)) {
			return;
		}

		world.removeBlock(pos, false);
		player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);

		GameStatistics statistics = game.getStatistics();
		statistics.forPlayer(player)
				.withDefault(StatisticKey.TRASH_COLLECTED, () -> 0)
				.apply(collected -> collected + 1);

		collectedTrash++;

		sidebar.set(renderSidebar(game));
	}

	private InteractionResult onPlayerBreakBlock(ServerPlayer player, BlockPos pos, BlockState state, InteractionHand hand) {
		return isTrash(state) ? InteractionResult.PASS : InteractionResult.FAIL;
	}

	private boolean isTrash(BlockState state) {
		return trashBlocks.contains(state.getBlock());
	}

	private void triggerGameOver(IGamePhase game) {
		if (gameOver) return;

		gameOver = true;

		GameStatistics statistics = game.getStatistics();

		int totalTimeSeconds = (int) (game.ticks() / SharedConstants.TICKS_PER_SECOND);
		int totalTrashCollected = 0;

		for (PlayerKey player : statistics.getPlayers()) {
			totalTimeSeconds += statistics.forPlayer(player).getOr(StatisticKey.TRASH_COLLECTED, 0);
		}

		StatisticsMap globalStatistics = statistics.global();
		globalStatistics.set(StatisticKey.TOTAL_TIME, totalTimeSeconds);
		globalStatistics.set(StatisticKey.TRASH_COLLECTED, totalTrashCollected);
	}

	private Component[] renderSidebar(IGamePhase game) {
		List<Component> sidebar = new ArrayList<>(10);
		sidebar.add(TrashDiveTexts.SIDEBAR_INSTRUCTION);
		sidebar.add(TrashDiveTexts.SIDEBAR_COLLECTED.apply(collectedTrash));

		PlayerPlacement.Score<Integer> placement = PlayerPlacement.fromMaxScore(game, StatisticKey.TRASH_COLLECTED);

		sidebar.add(CommonComponents.EMPTY);
		sidebar.add(TrashDiveTexts.SIDEBAR_TOP_PLAYERS);

		placement.addToSidebar(sidebar, 5);

		return sidebar.toArray(new Component[0]);
	}
}
