package com.lovetropics.minigames.common.content.river_race.state;

import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.UUID;

public class RiverRaceState implements VictoryPointsGameState {
    public static final GameStateKey<RiverRaceState> KEY = GameStateKey.create("RiverRace");

    private final IGamePhase game;

    public RiverRaceState(IGamePhase game) {
        this.game = game;
    }

    @Override
    public void addPointsToTeam(final GameTeamKey team, final int points) {
        addToTeam(Trackers.POINTS, team, points);
    }

    public void addCoinsToTeam(final GameTeamKey team, final int coins) {
        addToTeam(Trackers.COINS, team, coins);
    }

    @Override
    public void addPointsToTeam(final UUID playerId, final int points) {
        GameTeamKey team = getTeamForPlayer(playerId);
		if (team != null) {
            addPointsToTeam(team, points);
        }
    }

    @Override
    public int getVictoryPoints(GameTeamKey team) {
        return Trackers.POINTS.getTracker().getOrDefault(team, 0);
    }

    public int getCoins(GameTeamKey team) {
        return Trackers.COINS.getTracker().getOrDefault(team, 0);
    }

    @Override
    public int getVictoryPoints(ServerPlayer player) {
        return getVictoryPoints(getTeamForPlayer(player));
    }

    public int getCoins(ServerPlayer player) {
        return getCoins(getTeamForPlayer(player));
    }

    @Override
    public void reset() {
        for (final Trackers tracker : Trackers.values()) {
            tracker.reset();
        }
    }

    @Nullable
    private GameTeamKey getTeamForPlayer(ServerPlayer player) {
        return getTeamForPlayer(player.getUUID());
    }

    @Nullable
    private GameTeamKey getTeamForPlayer(UUID playerId) {
        TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);
        return teams.getTeamForPlayer(playerId);
    }

    private void addToTeam(final Trackers type, final GameTeamKey team, final int amount) {
        int lastValue = type.getTracker().addTo(team, amount);
        game.invoker(RiverRaceEvents.VICTORY_POINTS_CHANGED).onVictoryPointsChanged(team, lastValue + amount, lastValue);
    }

    public enum Trackers {
        POINTS,
        COINS;

        private final Object2IntOpenHashMap<GameTeamKey> tracker;

        Trackers() {
            tracker = new Object2IntOpenHashMap<>();
            tracker.defaultReturnValue(0);
        }

        private Object2IntOpenHashMap<GameTeamKey> getTracker() {
            return tracker;
        }

        private void reset() {
            tracker.clear();
        }
    }
}
