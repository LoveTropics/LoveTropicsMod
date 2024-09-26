package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.GamePhaseType;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorList;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MultiGamePhase extends GamePhase {

    private GamePhase activePhase;
    private List<IGamePhase> subPhases = new ArrayList<>();

    protected MultiGamePhase(GameInstance game, IGamePhaseDefinition definition, GamePhaseType phaseType, GameMap map, BehaviorList behaviors) {
        super(game, definition, phaseType, map, behaviors);
    }

    public void setActivePhase(GamePhase activePhase){
        this.activePhase = activePhase;
    }

    public List<IGamePhase> getSubPhases() {
        return subPhases;
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
            return activePhase.tick();
        }
        return super.tick();
    }
}
