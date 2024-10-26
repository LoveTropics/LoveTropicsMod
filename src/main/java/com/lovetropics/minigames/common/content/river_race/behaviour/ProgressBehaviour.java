package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.DyeColor;

import java.util.HashMap;
import java.util.Map;

public class ProgressBehaviour implements IGameBehavior {
    public static final MapCodec<ProgressBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.optionalFieldOf("max_points", 1).forGetter(c -> c.maxVictoryPoints)
    ).apply(i, ProgressBehaviour::new));
    private final int maxVictoryPoints;
    private final Map<GameTeamKey, GameBossBar> teamBars = new HashMap<>();

    public ProgressBehaviour(int maxVictoryPoints) {
        this.maxVictoryPoints = maxVictoryPoints;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        GlobalGameWidgets widgets = GlobalGameWidgets.registerTo(game, events);
        TeamState teams = game.instanceState().getOrThrow(TeamState.KEY);
        for (GameTeamKey teamKey : teams.getTeamKeys()) {
            GameTeam teamByKey = teams.getTeamByKey(teamKey);
            if(teamByKey == null){
                continue;
            }
            GameBossBar bossBar = widgets.openBossBar(teamByKey.config().styledName(), getTeamColour(teamByKey.config().dye()), BossEvent.BossBarOverlay.NOTCHED_20);
            bossBar.setProgress(0);
            teamBars.put(teamKey, bossBar);
        }
        events.listen(RiverRaceEvents.VICTORY_POINTS_CHANGED, (team, value, lastValue) -> {
            calculateTeamProgress(team, value);
        });
    }

    public void calculateTeamProgress(GameTeamKey team, int victoryPoints){
        GameBossBar gameBossBar = teamBars.get(team);
        if(gameBossBar != null){
            gameBossBar.setProgress((float) victoryPoints / maxVictoryPoints);
        }
    }

    private BossEvent.BossBarColor getTeamColour(DyeColor dyeColor){
        return switch (dyeColor){
            case BLUE -> BossEvent.BossBarColor.BLUE;
            case RED -> BossEvent.BossBarColor.RED;
            default -> BossEvent.BossBarColor.WHITE;
        };
    }
}