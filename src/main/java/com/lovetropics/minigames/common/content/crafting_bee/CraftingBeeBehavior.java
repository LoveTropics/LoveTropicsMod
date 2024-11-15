package com.lovetropics.minigames.common.content.crafting_bee;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.crafting_bee.ingredient.IngredientDecomposer;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.GameWinner;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.CraftingBeeCraftsClientState;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.TimedGameState;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CraftingBeeBehavior implements IGameBehavior {
    public static final MapCodec<CraftingBeeBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            RecipeSelector.CODEC.listOf().fieldOf("selectors").forGetter(c -> c.selectors),
            IngredientDecomposer.CODEC.codec().listOf().fieldOf("decomposers").forGetter(c -> c.decomposers),
            Codec.INT.optionalFieldOf("hints_per_player", 3).forGetter(c -> c.allowedHints),
            TemplatedText.CODEC.fieldOf("timer_bar").forGetter(c -> c.timerBarText),
            Codec.INT.fieldOf("time_per_team").forGetter(t -> t.timePerTeam),
            Codec.INT.fieldOf("recycling_penalty").forGetter(t -> t.recyclingPenalty),
            Codec.STRING.fieldOf("recycling_region").forGetter(c -> c.recyclingRegionKey)
    ).apply(in, CraftingBeeBehavior::new));

    private final List<RecipeSelector> selectors;
    private final List<IngredientDecomposer> decomposers;
    private final int allowedHints;

    private final TemplatedText timerBarText;
    private final int timePerTeam, recyclingPenalty;
    private final String recyclingRegionKey;

    private TeamState teams;
    private IGamePhase game;
    private BlockBox recyclingRegion;

    private ListMultimap<GameTeamKey, CraftingTask> tasks;
    private volatile boolean done;

    private final Map<GameTeamKey, GameBossBar> taskBars = new HashMap<>();
    private Map<GameTeamKey, BossBar> timerBars;
    private Set<GameTeamKey> teamsWithoutTime;

    public CraftingBeeBehavior(List<RecipeSelector> selectors, List<IngredientDecomposer> decomposers, int allowedHints, TemplatedText timerBarText, int timePerTeam, int recyclingPenalty, String recyclingRegionKey) {
        this.selectors = selectors;
        this.decomposers = decomposers;
        this.allowedHints = allowedHints;

        this.timerBarText = timerBarText;
        this.timePerTeam = timePerTeam;
        this.recyclingPenalty = recyclingPenalty;
        this.recyclingRegionKey = recyclingRegionKey;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        tasks = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

        decomposers.forEach(dec -> dec.prepareCache(game.level()));

        this.game = game;
        teams = game.instanceState().getOrThrow(TeamState.KEY);

        recyclingRegion = game.mapRegions().getOrThrow(recyclingRegionKey);

        timerBars = new HashMap<>();
        teamsWithoutTime = new HashSet<>();

        GlobalGameWidgets widgets = GlobalGameWidgets.registerTo(game, events);
        events.listen(GamePhaseEvents.START, () -> start(widgets));
        events.listen(GamePhaseEvents.TICK, () -> tickRunning(game));
        events.listen(GamePhaseEvents.DESTROY, () -> timerBars.values().forEach(b -> b.bar().close()));
        events.listen(GamePhaseEvents.STOP, reason -> timerBars.values().forEach(b -> b.bar().close()));

        events.listen(GamePlayerEvents.CRAFT, this::onCraft);
        events.listen(GamePlayerEvents.USE_BLOCK, this::useBlock);

        events.listen(GamePhaseEvents.STOP, reason -> GameClientState.removeFromPlayers(GameClientStateTypes.CRAFTING_BEE_CRAFTS.get(), game.allPlayers()));
        events.listen(GamePlayerEvents.REMOVE, player -> GameClientState.removeFromPlayer(GameClientStateTypes.CRAFTING_BEE_CRAFTS.get(), player));
    }

    private void start(GlobalGameWidgets widgets) {
        for (GameTeam team : teams) {
            var recipes = selectors.stream().map(selector -> selector.select(game.level()))
                    .map(recipe -> new CraftingTask(
                            recipe.getResult(game.registryAccess()),
                            recipe
                    ))
                    .toList();
            tasks.putAll(team.key(), recipes);
            sync(team.key());
            distributeIngredients(recipes, teams.getPlayersForTeam(team.key()));

            var bar = timerBars.computeIfAbsent(team.key(), k -> new BossBar(
                    new GameBossBar(CommonComponents.EMPTY, BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.NOTCHED_10),
                    new TimedGameState(timePerTeam, 0)
            ));
            teams.getPlayersForTeam(team.key()).forEach(bar.bar::addPlayer);
        }

        for (GameTeam team : teams) {
            GameBossBar bar = widgets.openBossBar(team.config().styledName(), team.config().dye(), BossEvent.BossBarOverlay.PROGRESS);
            bar.setProgress(0.0f);
            taskBars.put(team.key(), bar);
        }
    }

    private void distributeIngredients(Collection<CraftingTask> tasks, PlayerSet players) {
        // Empty teams have no players to distribute items to
        if (players.isEmpty()) return;

        for (CraftingTask task : tasks) {
            var ingredients = task.recipe.decompose();
            var items = ingredients.stream().flatMap(this::singleDecomposition).collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(items);

            // Evenly distribute the items between the players
            int p = 0;
            var playerList = players.stream().toList();
            for (ItemStack item : items) {
                playerList.get(p++).addItem(item.copy());
                if (p >= playerList.size()) p = 0;
            }
        }
    }

    private Stream<ItemStack> singleDecomposition(Ingredient ingredient) {
        if (ingredient.isEmpty()) return Stream.empty();

        for (IngredientDecomposer decomposer : decomposers) {
            var decomposed = decomposer.decompose(ingredient);
            if (decomposed != null) {
                return decomposed.stream().flatMap(this::singleDecomposition);
            }
        }

        // We have reduced the ingredient to its most basic form, so now we just pick the first item of the ingredient
        for (ItemStack item : ingredient.getItems()) {
            // Prioritize vanilla items
            if (item.getItem().builtInRegistryHolder().key().location().getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                return Stream.of(item);
            }
        }
        return Stream.of(ingredient.getItems()[0]);
    }

    private void onCraft(Player player, ItemStack crafted, Container container) {
        var team = teams.getTeamForPlayer(player);
        if (team == null || done) return;

        var teamTasks = tasks.get(team);
        var task = teamTasks.stream().filter(c -> ItemStack.isSameItemSameComponents(crafted, c.output)).findFirst().orElse(null);

        crafted.set(CraftingBee.CRAFTED_USING, ItemContainerContents.fromItems(IntStream.range(0, container.getContainerSize())
                .mapToObj(container::getItem)
                .filter(Predicate.not(ItemStack::isEmpty))
                .map(s -> s.copyWithCount(1))
                // .peek(s -> s.remove(CraftingBee.CRAFTED_USING)) // TODO - should we keep this?
                .sorted(Comparator.comparing(s -> s.getItemHolder().getRegisteredName()))
                .toList()));

        if (task == null || task.done) return;

        task.done = true;

        sync(team);

        var completed = teamTasks.stream().filter(t -> t.done).count();
        var gameTeam = teams.getTeamByKey(team);
        var teamConfig = gameTeam.config();

        game.allPlayers().sendMessage(CraftingBeeTexts.TEAM_HAS_COMPLETED_RECIPES.apply(teamConfig.styledName(), completed, teamTasks.size()));
        taskBars.get(team).setProgress(completed / (float) teamTasks.size());

        if (completed == teamTasks.size()) {
            game.invoker(GameLogicEvents.GAME_OVER).onGameWonBy(gameTeam);

            done = true;

            game.allPlayers().forEach(ServerPlayer::closeContainer);

            game.scheduler().runAfterSeconds(1.5f, () -> game.allPlayers().sendMessage(MinigameTexts.TEAM_WON.apply(teamConfig.styledName()).withStyle(ChatFormatting.GREEN), true));
            game.scheduler().runAfterSeconds(5, () -> game.requestStop(GameStopReason.finished()));
        }
    }

    private void tickRunning(IGamePhase game) {
        timerBars.forEach((team, bar) -> {
            switch (bar.state.tick(game.ticks())) {
                case RUNNING -> {
                    long ticksRemaining = bar.state.getTicksRemaining();
                    if (ticksRemaining % SharedConstants.TICKS_PER_SECOND == 0) {
                        bar.bar.setTitle(getTimeRemainingText(game, ticksRemaining));
                        bar.bar.setProgress((float) ticksRemaining / timePerTeam);
                    }
                }
                case GAME_OVER -> {
                    bar.bar.setTitle(getTimeRemainingText(game, 0));
                    bar.bar.setProgress(0);

                    teamsWithoutTime.add(team);
                    teams.getPlayersForTeam(team).forEach(ServerPlayer::closeContainer);

                    if (teamsWithoutTime.size() == tasks.asMap().size()) {
                        int mx = tasks.asMap().values().stream().mapToInt(craftingTasks -> (int) craftingTasks.stream().filter(c -> c.done).count())
                                        .max().orElse(0);
                        var withMax = tasks.asMap().entrySet().stream().filter(e -> e.getValue().stream().filter(c -> c.done).count() == mx)
                                        .toList();
                        if (withMax.size() != 1) {
                            game.invoker(GameLogicEvents.GAME_OVER).onGameOver(new GameWinner.Nobody());

                            game.scheduler().runAfterSeconds(1.5f, () -> game.allPlayers().sendMessage(MinigameTexts.NOBODY_WON, true));
                        } else {
                            var gameTeam = teams.getTeamByKey(withMax.getFirst().getKey());
                            var teamConfig = gameTeam.config();
                            game.invoker(GameLogicEvents.GAME_OVER).onGameWonBy(gameTeam);

                            game.scheduler().runAfterSeconds(1.5f, () -> game.allPlayers().sendMessage(MinigameTexts.TEAM_WON.apply(teamConfig.styledName()).withStyle(ChatFormatting.GREEN), true));
                        }

                        done = true;
                        game.scheduler().runAfterSeconds(5, () -> game.requestStop(GameStopReason.finished()));
                    }
                }
            }
        });
    }

    private Component getTimeRemainingText(IGamePhase game, long ticksRemaining) {
        long secondsRemaining = ticksRemaining / (long)game.level().tickRateManager().tickrate();

        Component timeText = Component.literal(Util.formatMinutesSeconds(secondsRemaining)).withStyle(ChatFormatting.AQUA);
        Component gameNameText = game.definition().name().copy().withStyle(ChatFormatting.AQUA);

        return timerBarText.apply(Map.of("time", timeText, "game", gameNameText));
    }

    private InteractionResult useBlock(ServerPlayer player, ServerLevel world, BlockPos pos, InteractionHand hand, BlockHitResult traceResult) {
        // don't allow players to use the crafting table after the game was won
        if (world.getBlockState(pos).is(Blocks.CRAFTING_TABLE) && (done || teamsWithoutTime.contains(teams.getTeamForPlayer(player)))) {
            return InteractionResult.FAIL;
        } else if (recyclingRegion.contains(pos)) {
            var item = player.getItemInHand(hand);
            if (item.isEmpty() || !item.has(CraftingBee.CRAFTED_USING)) return InteractionResult.PASS;

            item.get(CraftingBee.CRAFTED_USING).stream().forEach(s -> player.addItem(s.copyWithCount(1)));
            item.shrink(1);

            timerBars.get(teams.getTeamForPlayer(player))
                    .state().increaseRemaining(-recyclingPenalty);

            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private void sync(GameTeamKey team) {
        teams.getPlayersForTeam(team).forEach(this::sync);
    }

    private void sync(Player player) {
        if (player instanceof ServerPlayer sp) {
            GameClientState.sendToPlayer(new CraftingBeeCraftsClientState(tasks.get(teams.getTeamForPlayer(player)).stream().map(CraftingTask::toCraft).toList(),
                    game.gameUuid(), allowedHints), sp);
        }
    }

    @Override
    public Supplier<? extends GameBehaviorType<?>> behaviorType() {
        return CraftingBee.CRAFTING_BEE;
    }

    private static class CraftingTask {
        private final ItemStack output;
        private final SelectedRecipe recipe;
        private boolean done;

        private CraftingTask(ItemStack output, SelectedRecipe recipe) {
            this.output = output;
            this.recipe = recipe;
        }

        public CraftingBeeCraftsClientState.Craft toCraft() {
            return new CraftingBeeCraftsClientState.Craft(output, recipe.id(), done);
        }
    }

    private record BossBar(GameBossBar bar, TimedGameState state) {}
}
