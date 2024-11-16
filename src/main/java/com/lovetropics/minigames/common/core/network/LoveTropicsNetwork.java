package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.lobby.manage.ClientManageLobbyMessage;
import com.lovetropics.minigames.client.lobby.manage.ServerManageLobbyMessage;
import com.lovetropics.minigames.client.lobby.select_role.SelectRoleMessage;
import com.lovetropics.minigames.client.lobby.select_role.SelectRolePromptMessage;
import com.lovetropics.minigames.client.lobby.state.message.JoinedLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LeftLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyPlayersMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyUpdateMessage;
import com.lovetropics.minigames.client.particle_line.DrawParticleLineMessage;
import com.lovetropics.minigames.client.toast.ShowNotificationToastMessage;
import com.lovetropics.minigames.common.core.network.trivia.RequestTriviaStateUpdateMessage;
import com.lovetropics.minigames.common.core.network.trivia.SelectTriviaAnswerMessage;
import com.lovetropics.minigames.common.core.network.trivia.ShowTriviaMessage;
import com.lovetropics.minigames.common.core.network.trivia.TriviaAnswerResponseMessage;
import com.lovetropics.minigames.common.core.network.workspace.AddWorkspaceRegionMessage;
import com.lovetropics.minigames.common.core.network.workspace.SetWorkspaceMessage;
import com.lovetropics.minigames.common.core.network.workspace.UpdateWorkspaceRegionMessage;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = LoveTropics.ID, bus = EventBusSubscriber.Bus.MOD)
public final class LoveTropicsNetwork {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(LoveTropics.getCompatVersion());
        registrar.playToClient(SetWorkspaceMessage.TYPE, SetWorkspaceMessage.STREAM_CODEC, SetWorkspaceMessage::handle);
        registrar.playToClient(AddWorkspaceRegionMessage.TYPE, AddWorkspaceRegionMessage.STREAM_CODEC, AddWorkspaceRegionMessage::handle);
        registrar.playBidirectional(UpdateWorkspaceRegionMessage.TYPE, UpdateWorkspaceRegionMessage.STREAM_CODEC, UpdateWorkspaceRegionMessage::handle);

        registrar.playToServer(SpectatePlayerAndTeleportMessage.TYPE, SpectatePlayerAndTeleportMessage.STREAM_CODEC, SpectatePlayerAndTeleportMessage::handle);

        registrar.playToClient(LobbyUpdateMessage.TYPE, LobbyUpdateMessage.STREAM_CODEC, LobbyUpdateMessage::handle);
        registrar.playToClient(JoinedLobbyMessage.TYPE, JoinedLobbyMessage.STREAM_CODEC, JoinedLobbyMessage::handle);
        registrar.playToClient(LeftLobbyMessage.TYPE, LeftLobbyMessage.STREAM_CODEC, LeftLobbyMessage::handle);
        registrar.playToClient(LobbyPlayersMessage.TYPE, LobbyPlayersMessage.STREAM_CODEC, LobbyPlayersMessage::handle);

        registrar.playToClient(PlayerDisguiseMessage.TYPE, PlayerDisguiseMessage.STREAM_CODEC, PlayerDisguiseMessage::handle);
        registrar.playToClient(ShowNotificationToastMessage.TYPE, ShowNotificationToastMessage.STREAM_CODEC, ShowNotificationToastMessage::handle);

        registrar.playToClient(ClientManageLobbyMessage.TYPE, ClientManageLobbyMessage.STREAM_CODEC, ClientManageLobbyMessage::handle);
        registrar.playToServer(ServerManageLobbyMessage.TYPE, ServerManageLobbyMessage.STREAM_CODEC, ServerManageLobbyMessage::handle);

        registrar.playToClient(SetGameClientStateMessage.TYPE, SetGameClientStateMessage.STREAM_CODEC, SetGameClientStateMessage::handle);

        registrar.playToClient(SelectRolePromptMessage.TYPE, SelectRolePromptMessage.STREAM_CODEC, SelectRolePromptMessage::handle);
        registrar.playToServer(SelectRoleMessage.TYPE, SelectRoleMessage.STREAM_CODEC, SelectRoleMessage::handle);

        registrar.playToClient(DrawParticleLineMessage.TYPE, DrawParticleLineMessage.STREAM_CODEC, DrawParticleLineMessage::handle);

        registrar.playToClient(SpectatorPlayerActivityMessage.TYPE, SpectatorPlayerActivityMessage.STREAM_CODEC, SpectatorPlayerActivityMessage::handle);

        registrar.playToClient(RiseTideMessage.TYPE, RiseTideMessage.STREAM_CODEC, RiseTideMessage::handle);

        registrar.playToClient(ShowTriviaMessage.TYPE, ShowTriviaMessage.STREAM_CODEC, ShowTriviaMessage::handle);
        registrar.playToServer(SelectTriviaAnswerMessage.TYPE, SelectTriviaAnswerMessage.STREAM_CODEC, SelectTriviaAnswerMessage::handle);
        registrar.playToServer(RequestTriviaStateUpdateMessage.TYPE, RequestTriviaStateUpdateMessage.STREAM_CODEC, RequestTriviaStateUpdateMessage::handle);
        registrar.playToClient(TriviaAnswerResponseMessage.TYPE, TriviaAnswerResponseMessage.STREAM_CODEC, TriviaAnswerResponseMessage::handle);

        registrar.playToClient(SetForcedPoseMessage.TYPE, SetForcedPoseMessage.STREAM_CODEC, SetForcedPoseMessage::handle);
    }
}
