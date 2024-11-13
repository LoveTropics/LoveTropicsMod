package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.GamePhaseType;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.lovetropics.minigames.common.core.game.PlayerIsolation;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorList;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.config.GameConfig;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MultiGamePhase extends GamePhase {

    private static final Logger LOGGER = LogManager.getLogger(MultiGamePhase.class);

    @Nullable
    private GamePhase activePhase = null;
    private final List<GameConfig> subPhaseGames = new ArrayList<>();

    protected MultiGamePhase(GameInstance game, IGameDefinition gameDefinition, IGamePhaseDefinition definition, GamePhaseType phaseType, GameMap map, BehaviorList behaviors) {
        super(game, gameDefinition, definition, phaseType, map, behaviors);
    }

    public void setActivePhase(GamePhase activePhase, final boolean saveInventory) {
        this.activePhase = activePhase;
        MultiGameManager.INSTANCE.addGamePhaseToDimension(activePhase.dimension(), activePhase);
        activePhase.start(saveInventory);
    }

    public void returnHere(){
        List<ServerPlayer> shuffledPlayers = Lists.newArrayList(allPlayers());
        Collections.shuffle(shuffledPlayers);

        for (ServerPlayer player : shuffledPlayers) {
            returnPlayerToParentPhase(player, getRoleFor(player));
        }
    }

    private void returnPlayerToParentPhase(ServerPlayer player, @Nullable PlayerRole role) {
        // [Cojo] Added this event just in case we want to know when a player returns from a microgame, can remove if there's no usecase for it
        invoker(GamePlayerEvents.RETURN).onReturn(player.getUUID(), role);

        ServerPlayer newPlayer = PlayerIsolation.INSTANCE.reloadPlayerFromMemory(game, player);

        invoker(GamePlayerEvents.ADD).onAdd(newPlayer);
        invoker(GamePlayerEvents.SET_ROLE).onSetRole(newPlayer, role, null);

        addedPlayers.add(player.getUUID());
    }

    @Override
    public ResourceKey<Level> dimension() {
        if(activePhase != null){
            return activePhase.dimension();
        }
        return super.dimension();
    }

    @Override
    public <T> T invoker(GameEventType<T> type) {
        // Figure out what our sub-game phase is active and do that instead
        if(activePhase != null){
            return activePhase.invoker(type);
        }
        return events.invoker(type);
    }

    @Override
    public GameInstance game(){
        return game;
    }

    @Nullable
    @Override
    GameStopReason tick() {
        // Also tick our current sub-game phase
        if(activePhase != null){
            if(activePhase.tick() != null){
                MultiGameManager.INSTANCE.removeGamePhaseFromDimension(activePhase.dimension(), activePhase);
                activePhase.destroy();
                activePhase = null;
                startNextQueuedMicrogame(false).whenComplete((newGame, throwable) -> {
                    if(!newGame){
                        returnHere();
                    }
                    if(throwable != null){
                        LOGGER.info("Failed to start next queued micro-game {}", throwable.getMessage());
                    }
                });
            }
        } else {
            return super.tick();
        }
        return null;
    }

    @Override
    public GameResult<Unit> requestStop(GameStopReason reason) {
        if(activePhase != null){
            if(reason.isFinished()){
                return activePhase.requestStop(GameStopReason.canceled());
            }
            return activePhase.requestStop(reason);
        }
        return super.requestStop(reason);
    }

    @Override
    public MapRegions mapRegions() {
        if(activePhase != null){
            return activePhase.mapRegions();
        }
        return super.mapRegions();
    }


    @Override
    public ServerLevel level() {
        if(activePhase != null){
            return activePhase.level();
        }
        return super.level();
    }

    @Override
    public GameStateMap state() {
        if(activePhase != null){
            return activePhase.state();
        }
        return super.state();
    }

    @Override
    void destroy() {
        if(activePhase != null){
            activePhase.destroy();
            activePhase = null;
            return;
        }
        super.destroy();
    }

    public void clearQueuedGames() {
        subPhaseGames.clear();
    }

    public void queueGames(List<GameConfig> games) {
        subPhaseGames.addAll(games);
    }

    public CompletableFuture<Boolean> startNextQueuedMicrogame(final boolean saveInventory) {
        // No queued games left
        if (subPhaseGames.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }
        final GameConfig nextGame = subPhaseGames.removeFirst();
        return GamePhase.create(game(), nextGame, nextGame.getPlayingPhase(), GamePhaseType.PLAYING).thenApply(result -> {
            if (result.isOk()) {
				setActivePhase(result.getOk(), saveInventory);
                invoker(RiverRaceEvents.MICROGAME_STARTED).onMicrogameStarted(this);
                game.allPlayers().sendMessage(Component.literal("Now Playing: ").append(nextGame.name()).withStyle(ChatFormatting.GREEN));
                game.allPlayers().showTitle(Component.empty().append(nextGame.name()).withStyle(ChatFormatting.GREEN),
                        nextGame.subtitle(), 10, 40, 10);
                return true;
            }
            LOGGER.error("Failed to start micro-game {} - {}", nextGame.id().toString(), result.getError().getString());
            return false;
        });
    }
}
