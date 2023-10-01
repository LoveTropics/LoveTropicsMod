package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameTeamEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateSender;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.TeamMembersClientState;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.SetGameClientStateMessage;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class SyncTeamsBehavior implements IGameBehavior {
    public static final Codec<SyncTeamsBehavior> CODEC = Codec.unit(SyncTeamsBehavior::new);

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GameTeamEvents.SET_GAME_TEAM, (player, teams, team) -> sendSync(teams, team));
        events.listen(GameTeamEvents.REMOVE_FROM_TEAM, (player, teams, team) -> {
            sendSync(teams, team);
            GameClientStateSender.get().byPlayer(player).enqueueRemove(GameClientStateTypes.TEAM_MEMBERS.get());
        });

        events.listen(GamePhaseEvents.STOP, reason -> LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.with(() -> null), SetGameClientStateMessage.remove(GameClientStateTypes.TEAM_MEMBERS.get())));
    }

    private void sendSync(TeamState teams, GameTeamKey key) {
        final var team = teams.getPlayersForTeam(key);
        team.forEach(player -> GameClientStateSender.get().byPlayer(player).enqueueSet(new TeamMembersClientState(team.stream().filter(p -> p != player)
                .map(ServerPlayer::getUUID).toList())));
    }
}
