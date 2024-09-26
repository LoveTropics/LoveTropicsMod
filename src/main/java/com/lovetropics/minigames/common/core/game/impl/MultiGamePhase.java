package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorList;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.rewards.GameRewardsMap;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MultiGamePhase extends GamePhase {

    @Nullable
    private GamePhase activePhase = null;
    private final Set<IGamePhase> subPhases = new HashSet<>();

    protected MultiGamePhase(GameInstance game, IGamePhaseDefinition definition, GamePhaseType phaseType, GameMap map, BehaviorList behaviors) {
        super(game, definition, phaseType, map, behaviors);
    }

    public void addAndSetActivePhase(GamePhase activePhase){
        subPhases.add(activePhase);
        setActivePhase(activePhase);
    }

    public void setActivePhase(GamePhase activePhase){
        this.activePhase = activePhase;
        MultiGameManager.INSTANCE.addGamePhaseToDimension(activePhase.dimension(), activePhase);
        activePhase.state().register(GameRewardsMap.STATE, ((GameLobby) activePhase.lobby()).getRewardsMap());
        activePhase.start();
    }

    public void returnHere(){
        List<ServerPlayer> shuffledPlayers = Lists.newArrayList(allPlayers());
        Collections.shuffle(shuffledPlayers);

        for (ServerPlayer player : shuffledPlayers) {
            addAndSpawnPlayer(player, getRoleFor(player));
        }
    }

    public Set<IGamePhase> getSubPhases() {
        return subPhases;
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
                subPhases.remove(activePhase);
                MultiGameManager.INSTANCE.removeGamePhaseFromDimension(activePhase.dimension(), activePhase);
                activePhase.destroy();
                activePhase = null;
                returnHere();
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
}
