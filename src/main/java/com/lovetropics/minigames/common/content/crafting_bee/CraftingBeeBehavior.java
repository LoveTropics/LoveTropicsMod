package com.lovetropics.minigames.common.content.crafting_bee;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.crafting_bee.ingredient.IngredientDecomposer;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
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
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftingBeeBehavior implements IGameBehavior {
    public static final MapCodec<CraftingBeeBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            RecipeSelector.CODEC.listOf().fieldOf("selectors").forGetter(c -> c.selectors),
            IngredientDecomposer.CODEC.codec().listOf().fieldOf("decomposers").forGetter(c -> c.decomposers),
            Codec.INT.optionalFieldOf("hints_per_player", 3).forGetter(c -> c.allowedHints)
    ).apply(in, CraftingBeeBehavior::new));

    private final List<RecipeSelector> selectors;
    private final List<IngredientDecomposer> decomposers;
    private final int allowedHints;

    private TeamState teams;
    private IGamePhase game;

    private ListMultimap<GameTeamKey, CraftingTask> tasks;
    private volatile boolean done;

    public CraftingBeeBehavior(List<RecipeSelector> selectors, List<IngredientDecomposer> decomposers, int allowedHints) {
        this.selectors = selectors;
        this.decomposers = decomposers;
        this.allowedHints = allowedHints;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        tasks = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

        decomposers.forEach(dec -> dec.prepareCache(game.level()));

        this.game = game;
        teams = game.instanceState().getOrThrow(TeamState.KEY);

        events.listen(GamePhaseEvents.START, this::start);
        events.listen(GamePlayerEvents.CRAFT, this::onCraft);
        events.listen(GamePlayerEvents.USE_BLOCK, this::useBlock);

        events.listen(GamePhaseEvents.STOP, reason -> GameClientState.removeFromPlayers(GameClientStateTypes.CRAFTING_BEE_CRAFTS.get(), game.allPlayers()));
        events.listen(GamePlayerEvents.REMOVE, player -> GameClientState.removeFromPlayer(GameClientStateTypes.CRAFTING_BEE_CRAFTS.get(), player));
    }

    private void start() {
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
        if (task == null || task.done) return;

        task.done = true;

        sync(team);

        var completed = teamTasks.stream().filter(t -> t.done).count();
        var teamConfig = teams.getTeamByKey(team).config();

        game.allPlayers().sendMessage(CraftingBeeTexts.TEAM_HAS_COMPLETED_RECIPES.apply(teamConfig.styledName(), completed, teamTasks.size()));

        if (completed == teamTasks.size()) {

            game.statistics().global().set(StatisticKey.WINNING_TEAM, team);
            game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(teamConfig.name());
            game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

            done = true;

            game.allPlayers().forEach(ServerPlayer::closeContainer);

            game.schedule(1.5f, () -> game.allPlayers().sendMessage(MinigameTexts.TEAM_WON.apply(teamConfig.styledName()).withStyle(ChatFormatting.GREEN), true));
            game.schedule(5, () -> game.requestStop(GameStopReason.finished()));
        }
    }

    private InteractionResult useBlock(ServerPlayer player, ServerLevel world, BlockPos pos, InteractionHand hand, BlockHitResult traceResult) {
        // don't allow players to use the crafting table after the game was won
        if (world.getBlockState(pos).is(Blocks.CRAFTING_TABLE) && done) {
            return InteractionResult.FAIL;
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
}
