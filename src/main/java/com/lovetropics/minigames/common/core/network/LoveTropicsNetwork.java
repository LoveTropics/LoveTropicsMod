package com.lovetropics.minigames.common.core.network;

import com.lovetropics.minigames.Constants;
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
import com.lovetropics.minigames.common.core.network.workspace.AddWorkspaceRegionMessage;
import com.lovetropics.minigames.common.core.network.workspace.SetWorkspaceMessage;
import com.lovetropics.minigames.common.core.network.workspace.UpdateWorkspaceRegionMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class LoveTropicsNetwork {

	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(Constants.MODID, "main"),
			() -> LoveTropics.getCompatVersion(),
			LoveTropics::isCompatibleVersion,
			LoveTropics::isCompatibleVersion
	);

	public static void register() {
		CHANNEL.messageBuilder(SetWorkspaceMessage.class, 0, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(SetWorkspaceMessage::encode).decoder(SetWorkspaceMessage::decode)
				.consumerMainThread(SetWorkspaceMessage::handle)
				.add();

		CHANNEL.messageBuilder(AddWorkspaceRegionMessage.class, 1, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(AddWorkspaceRegionMessage::encode).decoder(AddWorkspaceRegionMessage::decode)
				.consumerMainThread(AddWorkspaceRegionMessage::handle)
				.add();

		CHANNEL.messageBuilder(UpdateWorkspaceRegionMessage.class, 2)
				.encoder(UpdateWorkspaceRegionMessage::encode).decoder(UpdateWorkspaceRegionMessage::decode)
				.consumerMainThread(UpdateWorkspaceRegionMessage::handle)
				.add();

		CHANNEL.messageBuilder(SpectatePlayerAndTeleportMessage.class, 3, NetworkDirection.PLAY_TO_SERVER)
				.encoder(SpectatePlayerAndTeleportMessage::encode).decoder(SpectatePlayerAndTeleportMessage::decode)
				.consumerMainThread(SpectatePlayerAndTeleportMessage::handle)
				.add();

		CHANNEL.messageBuilder(LobbyUpdateMessage.class, 4, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(LobbyUpdateMessage::encode).decoder(LobbyUpdateMessage::decode)
				.consumerMainThread(LobbyUpdateMessage::handle)
				.add();

		CHANNEL.messageBuilder(JoinedLobbyMessage.class, 5, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(JoinedLobbyMessage::encode).decoder(JoinedLobbyMessage::decode)
				.consumerMainThread(JoinedLobbyMessage::handle)
				.add();

		CHANNEL.messageBuilder(LeftLobbyMessage.class, 6, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(LeftLobbyMessage::encode).decoder(LeftLobbyMessage::decode)
				.consumerMainThread(LeftLobbyMessage::handle)
				.add();

		CHANNEL.messageBuilder(LobbyPlayersMessage.class, 7, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(LobbyPlayersMessage::encode).decoder(LobbyPlayersMessage::decode)
				.consumerMainThread(LobbyPlayersMessage::handle)
				.add();

		CHANNEL.messageBuilder(PlayerDisguiseMessage.class, 8, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(PlayerDisguiseMessage::encode).decoder(PlayerDisguiseMessage::decode)
				.consumerMainThread(PlayerDisguiseMessage::handle)
				.add();

		CHANNEL.messageBuilder(ShowNotificationToastMessage.class, 9, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ShowNotificationToastMessage::encode).decoder(ShowNotificationToastMessage::decode)
				.consumerMainThread(ShowNotificationToastMessage::handle)
				.add();

		CHANNEL.messageBuilder(ClientManageLobbyMessage.class, 10, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientManageLobbyMessage::encode).decoder(ClientManageLobbyMessage::decode)
				.consumerMainThread(ClientManageLobbyMessage::handle)
				.add();

		CHANNEL.messageBuilder(ServerManageLobbyMessage.class, 11, NetworkDirection.PLAY_TO_SERVER)
				.encoder(ServerManageLobbyMessage::encode).decoder(ServerManageLobbyMessage::decode)
				.consumerMainThread(ServerManageLobbyMessage::handle)
				.add();

		CHANNEL.messageBuilder(SetGameClientStateMessage.class, 12, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(SetGameClientStateMessage::encode).decoder(SetGameClientStateMessage::decode)
				.consumerMainThread(SetGameClientStateMessage::handle)
				.add();

		CHANNEL.messageBuilder(SelectRolePromptMessage.class, 13, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(SelectRolePromptMessage::encode).decoder(SelectRolePromptMessage::decode)
				.consumerMainThread(SelectRolePromptMessage::handle)
				.add();

		CHANNEL.messageBuilder(SelectRoleMessage.class, 14, NetworkDirection.PLAY_TO_SERVER)
				.encoder(SelectRoleMessage::encode).decoder(SelectRoleMessage::decode)
				.consumerMainThread(SelectRoleMessage::handle)
				.add();

		CHANNEL.messageBuilder(DrawParticleLineMessage.class, 15, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(DrawParticleLineMessage::encode).decoder(DrawParticleLineMessage::decode)
				.consumerMainThread(DrawParticleLineMessage::handle)
				.add();

		CHANNEL.messageBuilder(SpectatorPlayerActivityMessage.class, 16, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(SpectatorPlayerActivityMessage::encode).decoder(SpectatorPlayerActivityMessage::decode)
				.consumerMainThread(SpectatorPlayerActivityMessage::handle)
				.add();

		CHANNEL.messageBuilder(RiseTideMessage.class, 17, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(RiseTideMessage::encode).decoder(RiseTideMessage::new)
				.consumerMainThread(RiseTideMessage::handle)
				.add();
	}
}
