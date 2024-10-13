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
import com.lovetropics.minigames.common.core.game.client_state.instance.CraftingBeeCrafts;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class CraftingBeeBehavior implements IGameBehavior {
    public static final MapCodec<CraftingBeeBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            RecipeSelector.CODEC.codec().listOf().fieldOf("selectors").forGetter(c -> c.selectors),
            IngredientDecomposer.CODEC.codec().listOf().fieldOf("decomposers").forGetter(c -> c.decomposers)
    ).apply(in, CraftingBeeBehavior::new));

    private final List<RecipeSelector> selectors;
    private final List<IngredientDecomposer> decomposers;

    private TeamState teams;
    private IGamePhase game;

    private ListMultimap<GameTeamKey, CraftingTask> tasks;

    public CraftingBeeBehavior(List<RecipeSelector> selectors, List<IngredientDecomposer> decomposers) {
        this.selectors = selectors;
        this.decomposers = decomposers;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        tasks = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

        decomposers.forEach(dec -> dec.prepareCache(game.level()));

        this.game = game;
        teams = game.instanceState().getOrThrow(TeamState.KEY);

        events.listen(GamePhaseEvents.START, this::start);

        events.listen(GamePlayerEvents.CRAFT, this::onCraft);
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
            var items = ingredients.stream().flatMap(this::singleDecomposition).toList();

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
        for (IngredientDecomposer decomposer : decomposers) {
            var decomposed = decomposer.decompose(ingredient);
            if (decomposed != null) {
                return decomposed.stream().flatMap(this::singleDecomposition);
            }
        }
        if (ingredient.getItems().length == 0) return Stream.empty();

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
        if (team == null) return;

        var teamTasks = tasks.get(team);
        var task = teamTasks.stream().filter(c -> ItemStack.isSameItemSameComponents(crafted, c.output)).findFirst().orElse(null);
        if (task == null) return;

        task.done = true;

        sync(team);

        var completed = teamTasks.stream().filter(t -> t.done).count();
        var teamName = teams.getTeamByKey(team).config().styledName();

        game.allPlayers().sendMessage(CraftingBeeTexts.TEAM_HAS_COMPLETED_RECIPES.apply(teamName, completed, teamTasks.size()));

        if (completed == teamTasks.size()) {

            game.statistics().global().set(StatisticKey.WINNING_TEAM, team);
            game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(teamName);

            game.allPlayers().forEach(ServerPlayer::closeContainer);

            game.schedule(1.5f, () -> game.allPlayers().sendMessage(MinigameTexts.TEAM_WON.apply(teamName).withStyle(ChatFormatting.GREEN), true));
            game.schedule(5, () -> game.requestStop(GameStopReason.finished()));
        }
    }

    private void sync(GameTeamKey team) {
        teams.getPlayersForTeam(team).forEach(this::sync);
    }

    private void sync(Player player) {
        if (player instanceof ServerPlayer sp) {
            GameClientState.sendToPlayer(new CraftingBeeCrafts(tasks.get(teams.getTeamForPlayer(player)).stream().map(CraftingTask::toCraft).toList()), sp);
        }
    }

    @Override
    public Supplier<? extends GameBehaviorType<?>> behaviorType() {
        return CraftingBee.CRAFTING_BEE;
    }

    private static class CraftingTask {
        private final ItemStack output;
        private final RecipeSelector.SelectedRecipe recipe;
        private boolean done;

        private CraftingTask(ItemStack output, RecipeSelector.SelectedRecipe recipe) {
            this.output = output;
            this.recipe = recipe;
        }

        public CraftingBeeCrafts.Craft toCraft() {
            return new CraftingBeeCrafts.Craft(output, recipe.id(), done);
        }
    }
}
