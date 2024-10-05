package com.lovetropics.minigames.common.content.river_race.state;

import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class VictoryPointsState implements IGameState {
    public static final GameStateKey<VictoryPointsState> KEY = GameStateKey.create("VictoryPoints");

    private final IGamePhase game;

    private final Object2IntOpenHashMap<GameTeamKey> pointsTracker = new Object2IntOpenHashMap<>();

    public VictoryPointsState(IGamePhase game) {
        this.game = game;

        pointsTracker.defaultReturnValue(0);
    }

    public void addPointsToTeam(final ServerPlayer player, final int points) {
        addPointsToTeam(getTeamForPlayer(player), points);
    }

    @Nullable
    private GameTeamKey getTeamForPlayer(ServerPlayer player) {
        TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);
        return teams.getTeamForPlayer(player);
    }

    public void addPointsToTeam(final GameTeamKey team, final int points) {
        int lastValue = pointsTracker.addTo(team, points);
        game.invoker(RiverRaceEvents.VICTORY_POINTS_CHANGED).onVictoryPointsChanged(team, lastValue + points, lastValue);
    }

    public int getPoints(GameTeamKey team) {
        return pointsTracker.getOrDefault(team, 0);
    }

    public int getPoints(ServerPlayer player) {
        return getPoints(getTeamForPlayer(player));
    }
}
