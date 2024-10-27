package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.content.river_race.state.RiverRaceState;
import com.lovetropics.minigames.common.core.game.GamePhaseType;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.lovetropics.minigames.common.core.game.PlayerIsolation;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorList;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.config.GameConfig;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.rewards.GameRewardsMap;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MultiGamePhase extends GamePhase {

    @FunctionalInterface
    interface GameStateRegistration<L, M, R> {
        void registerState(L lobby, M phase, R id);
    }

    private enum MultiPhaseGameStates {
        RIVER_RACE((lobby, phase, id) -> {
            IGameState state = lobby.getMultiPhaseDataMap().get(id);
            if (state instanceof RiverRaceState riverRaceState) {
                riverRaceState.reset();
            } else {
                lobby.getMultiPhaseDataMap().put(id, new RiverRaceState(phase));
            }
            phase.phaseState.register(RiverRaceState.KEY, (RiverRaceState) lobby.getMultiPhaseDataMap().get(id));
        });

        final GameStateRegistration<GameLobby, MultiGamePhase, ResourceLocation> registration;

        MultiPhaseGameStates(GameStateRegistration<GameLobby, MultiGamePhase, ResourceLocation> registration) {
            this.registration = registration;
        }
    }
    private ResourceLocation gameId;
    @Nullable
    private GamePhase activePhase = null;
    @Nullable
    private ResourceLocation activePhaseId = null;
    private final List<ResourceLocation> subPhaseGames = Lists.newArrayList();
    private static final Map<ResourceLocation, MultiPhaseGameStates> gameStateMap = Maps.newHashMap();

    protected MultiGamePhase(GameInstance game, IGamePhaseDefinition definition, GamePhaseType phaseType, GameMap map, BehaviorList behaviors, ResourceLocation gameId) {
        super(game, definition, phaseType, map, behaviors);
        this.gameId = gameId;
        gameStateMap.put(gameId, MultiPhaseGameStates.RIVER_RACE);
    }

    protected MultiGamePhase(GameInstance game, IGamePhaseDefinition definition, GamePhaseType phaseType, GameMap map, BehaviorList behaviors) {
        this(game, definition, phaseType, map, behaviors, ResourceLocation.parse("lt:river_race"));
    }

    public void registerState(final GameLobby lobby) {
        final ResourceLocation id = game.definition.getId();
        gameStateMap.get(id).registration.registerState(lobby, this, id);
    }

    public void setActivePhase(GamePhase activePhase, final boolean saveInventory, ResourceLocation activePhaseId) {
        this.activePhase = activePhase;
        this.activePhaseId = activePhaseId;
        MultiGameManager.INSTANCE.addGamePhaseToDimension(activePhase.dimension(), activePhase);
        activePhase.state().register(GameRewardsMap.STATE, ((GameLobby) lobby()).getRewardsMap());
        activePhase.state().register(RiverRaceState.KEY, (RiverRaceState) ((GameLobby) lobby()).createOrGetMultiPhaseState(this));
        activePhase.start(saveInventory);
    }

    public ResourceLocation getActiveGameId(){
        if(activePhaseId != null){
            return activePhaseId;
        }
        return definition().getId();
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

    public List<ResourceLocation> getSubPhaseGames() {
        return subPhaseGames;
    }

    public ResourceLocation getGameId() {
        return gameId;
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
                activePhaseId = null;

                if (!startNextQueuedMicrogame(false)) {
                    returnHere();
                }
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
            activePhaseId = null;
            return;
        }
        super.destroy();
    }

    public void clearQueuedGames() {
        subPhaseGames.clear();
    }

    public void queueGames(List<ResourceLocation> games) {
        subPhaseGames.addAll(games);
    }

    public boolean startNextQueuedMicrogame(final boolean saveInventory) {
        // No queued games left
        if (subPhaseGames.isEmpty()) {
            return false;
        }

        final ResourceLocation gameKey = subPhaseGames.removeFirst();
        GameConfig gameConfig = GameConfigs.REGISTRY.get(gameKey);
        GamePhase.createMultiGame(game(), gameConfig.getPlayingPhase(), GamePhaseType.PLAYING, gameConfig.getId()).thenAccept((result) -> {
            setActivePhase(result.getOk(), saveInventory, gameKey);
            game.allPlayers().showTitle(Component.empty().append(gameConfig.name).withStyle(ChatFormatting.GREEN),
                    gameConfig.subtitle, 10, 40, 10);
        });

        invoker(RiverRaceEvents.MICROGAME_STARTED).onMicrogameStarted(this);

        return true;
    }
}
