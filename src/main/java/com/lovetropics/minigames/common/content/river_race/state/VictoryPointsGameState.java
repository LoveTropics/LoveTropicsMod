package com.lovetropics.minigames.common.content.river_race.state;

import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import net.minecraft.server.level.ServerPlayer;

public interface VictoryPointsGameState extends IGameState {
    void addPointsToTeam(final GameTeamKey team, final int points);
    void addPointsToTeam(final ServerPlayer player, final int points);
    int getVictoryPoints(GameTeamKey team);
    int getVictoryPoints(ServerPlayer player);
    void reset();
}
