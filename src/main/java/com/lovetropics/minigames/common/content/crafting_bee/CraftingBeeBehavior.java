package com.lovetropics.minigames.common.content.crafting_bee;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.entity.FireworkPalette;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
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

        events.listen(GamePlayerEvents.CRAFT_RESULT, this::modifyCraftResult);
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

    private ItemStack modifyCraftResult(ServerPlayer player, ItemStack result, CraftingInput craftingInput, CraftingRecipe recipe) {
        GameTeamKey team = teams.getTeamForPlayer(player);
        if (team == null || teamsWithoutTime.contains(team)) {
            // Don't allow players to use their inventory to craft, either!
            return ItemStack.EMPTY;
        }
        ItemContainerContents usedItems = ItemContainerContents.fromItems(craftingInput.items().stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copyWithCount(1))
                // .peek(s -> s.remove(CraftingBee.CRAFTED_USING)) // TODO - should we keep this?
                .sorted(Comparator.comparing(s -> s.getItemHolder().getRegisteredName()))
                .toList());
        result.set(CraftingBee.CRAFTED_USING, new CraftedUsing(
                result.getCount(),
                usedItems
        ));
        return result;
    }

    private void onCraft(Player player, ItemStack crafted, Container container) {
        var team = teams.getTeamForPlayer(player);
        if (team == null || done) return;

        var teamTasks = tasks.get(team);

        var craftedStackToCompare = crafted.copy();
        craftedStackToCompare.remove(CraftingBee.CRAFTED_USING);
        var task = teamTasks.stream().filter(c -> ItemStack.isSameItemSameComponents(craftedStackToCompare, c.output)).findFirst().orElse(null);

        if (task == null || task.done) return;

        task.done = true;

        sync(team);

        var completed = teamTasks.stream().filter(t -> t.done).count();
        var gameTeam = teams.getTeamByKey(team);
        var teamConfig = gameTeam.config();

        game.allPlayers().sendMessage(CraftingBeeTexts.TEAM_HAS_COMPLETED_RECIPES.apply(teamConfig.styledName(), completed, teamTasks.size()));
        taskBars.get(team).setProgress(completed / (float) teamTasks.size());

        PlayerSet teamPlayers = teams.getPlayersForTeam(team);
        teamPlayers.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0f, 1.0f);
        game.spectators().playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0f, 1.0f);

        PlayerSet otherPlayers = PlayerSet.difference(game.participants(), teamPlayers);
        otherPlayers.playSound(SoundEvents.GHAST_SCREAM, SoundSource.PLAYERS, 0.5f, 0.5f);

        if (completed == teamTasks.size()) {
            triggerGameOver(new GameWinner.Team(gameTeam));
        }
    }

    private void triggerGameOver(GameWinner winner) {
        done = true;
        game.invoker(GameLogicEvents.GAME_OVER).onGameOver(winner);
        game.allPlayers().forEach(ServerPlayer::closeContainer);

        game.scheduler().runAfterSeconds(1.5f, () -> {
            game.allPlayers().playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.5f, 1.0f);
            game.allPlayers().showTitle(MinigameTexts.GAME_OVER, null, 10, 40, 10);

            if (winner instanceof GameWinner.Team(GameTeam team)) {
                for (ServerPlayer winningPlayer : teams.getPlayersForTeam(team.key())) {
                    applyWinningPlayerEffects(winningPlayer, team);
                }
                game.allPlayers().sendMessage(MinigameTexts.TEAM_WON.apply(team.config().styledName()).withStyle(ChatFormatting.GREEN), true);
            } else {
                game.allPlayers().sendMessage(MinigameTexts.NOBODY_WON, true);
            }
        });

        game.scheduler().runAfterSeconds(8, () -> game.requestStop(GameStopReason.finished()));
    }

    private void applyWinningPlayerEffects(ServerPlayer winningPlayer, GameTeam team) {
        game.scheduler().runAfterSeconds(game.random().nextFloat(), () -> {
            BlockPos fireworksPos = BlockPos.containing(winningPlayer.getEyePosition()).above();
            FireworkPalette.forDye(team.config().dye()).spawn(fireworksPos, game.level());
        });
        winningPlayer.setGlowingTag(true);
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

                    PlayerSet playersInTeam = teams.getPlayersForTeam(team);
                    playersInTeam.forEach(ServerPlayer::closeContainer);
                    playersInTeam.playSound(SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.5f, 0.25f);
                    playersInTeam.showTitle(CraftingBeeTexts.TIME_UP, null, 10, SharedConstants.TICKS_PER_SECOND * 2, 10);

                    if (teamsWithoutTime.size() == tasks.asMap().size()) {
                        int mx = tasks.asMap().values().stream().mapToInt(craftingTasks -> (int) craftingTasks.stream().filter(c -> c.done).count())
                                        .max().orElse(0);
                        var withMax = tasks.asMap().entrySet().stream().filter(e -> e.getValue().stream().filter(c -> c.done).count() == mx)
                                        .toList();
                        if (withMax.size() != 1) {
                            triggerGameOver(new GameWinner.Nobody());
                        } else {
                            var gameTeam = teams.getTeamByKey(withMax.getFirst().getKey());
                            triggerGameOver(new GameWinner.Team(gameTeam));
                        }
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
        }
        ItemStack itemInHand = player.getItemInHand(hand);
        if (recyclingRegion.contains(pos) && !itemInHand.isEmpty()) {
            return useRecycler(player, itemInHand);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult useRecycler(ServerPlayer player, ItemStack itemToRecycle) {
        CraftedUsing craftedUsing = itemToRecycle.get(CraftingBee.CRAFTED_USING);
        if (craftedUsing == null) {
            player.sendSystemMessage(CraftingBeeTexts.CANNOT_RECYCLE, true);
            player.playNotifySound(SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.FAIL;
        }
        if (itemToRecycle.getCount() < craftedUsing.count()) {
            player.sendSystemMessage(CraftingBeeTexts.NOT_ENOUGH_TO_RECYCLE.apply(craftedUsing.count(), itemToRecycle.getHoverName()), true);
            player.playNotifySound(SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
            return InteractionResult.FAIL;
        }

        itemToRecycle.shrink(craftedUsing.count());
        craftedUsing.items().stream().forEach(player::addItem);

        applyTimePenalty(player, recyclingPenalty);
        player.playNotifySound(SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 1.0f, 1.0f);

        return InteractionResult.SUCCESS;
    }

    private void applyTimePenalty(ServerPlayer player, int penalty) {
        timerBars.get(teams.getTeamForPlayer(player))
                .state().increaseRemaining(-penalty);

        Component subtitle = CraftingBeeTexts.TIME_PENALTY.apply(Mth.positiveCeilDiv(penalty, SharedConstants.TICKS_PER_SECOND));
        teams.getPlayersOnSameTeam(player).showTitle(null, subtitle, 10, 20, 10);
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
