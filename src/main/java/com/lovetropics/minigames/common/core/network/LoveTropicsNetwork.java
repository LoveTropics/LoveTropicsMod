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
				.consumer(SetWorkspaceMessage::handle)
				.add();

		CHANNEL.messageBuilder(AddWorkspaceRegionMessage.class, 1, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(AddWorkspaceRegionMessage::encode).decoder(AddWorkspaceRegionMessage::decode)
				.consumer(AddWorkspaceRegionMessage::handle)
				.add();

		CHANNEL.messageBuilder(UpdateWorkspaceRegionMessage.class, 2)
				.encoder(UpdateWorkspaceRegionMessage::encode).decoder(UpdateWorkspaceRegionMessage::decode)
				.consumer(UpdateWorkspaceRegionMessage::handle)
				.add();

		CHANNEL.messageBuilder(SpectatePlayerAndTeleportMessage.class, 3, NetworkDirection.PLAY_TO_SERVER)
				.encoder(SpectatePlayerAndTeleportMessage::encode).decoder(SpectatePlayerAndTeleportMessage::decode)
				.consumer(SpectatePlayerAndTeleportMessage::handle)
				.add();

		CHANNEL.messageBuilder(LobbyUpdateMessage.class, 4, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(LobbyUpdateMessage::encode).decoder(LobbyUpdateMessage::decode)
				.consumer(LobbyUpdateMessage::handle)
				.add();

		CHANNEL.messageBuilder(JoinedLobbyMessage.class, 5, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(JoinedLobbyMessage::encode).decoder(JoinedLobbyMessage::decode)
				.consumer(JoinedLobbyMessage::handle)
				.add();

		CHANNEL.messageBuilder(LeftLobbyMessage.class, 6, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(LeftLobbyMessage::encode).decoder(LeftLobbyMessage::decode)
				.consumer(LeftLobbyMessage::handle)
				.add();

		CHANNEL.messageBuilder(LobbyPlayersMessage.class, 7, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(LobbyPlayersMessage::encode).decoder(LobbyPlayersMessage::decode)
				.consumer(LobbyPlayersMessage::handle)
				.add();

		CHANNEL.messageBuilder(PlayerDisguiseMessage.class, 8, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(PlayerDisguiseMessage::encode).decoder(PlayerDisguiseMessage::decode)
				.consumer(PlayerDisguiseMessage::handle)
				.add();

		CHANNEL.messageBuilder(ShowNotificationToastMessage.class, 9, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ShowNotificationToastMessage::encode).decoder(ShowNotificationToastMessage::decode)
				.consumer(ShowNotificationToastMessage::handle)
				.add();

		CHANNEL.messageBuilder(ClientManageLobbyMessage.class, 10, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(ClientManageLobbyMessage::encode).decoder(ClientManageLobbyMessage::decode)
				.consumer(ClientManageLobbyMessage::handle)
				.add();

		CHANNEL.messageBuilder(ServerManageLobbyMessage.class, 11, NetworkDirection.PLAY_TO_SERVER)
				.encoder(ServerManageLobbyMessage::encode).decoder(ServerManageLobbyMessage::decode)
				.consumer(ServerManageLobbyMessage::handle)
				.add();

		CHANNEL.messageBuilder(SetGameClientStateMessage.class, 12, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(SetGameClientStateMessage::encode).decoder(SetGameClientStateMessage::decode)
				.consumer(SetGameClientStateMessage::handle)
				.add();

		CHANNEL.messageBuilder(SelectRolePromptMessage.class, 13, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(SelectRolePromptMessage::encode).decoder(SelectRolePromptMessage::decode)
				.consumer(SelectRolePromptMessage::handle)
				.add();

		CHANNEL.messageBuilder(SelectRoleMessage.class, 14, NetworkDirection.PLAY_TO_SERVER)
				.encoder(SelectRoleMessage::encode).decoder(SelectRoleMessage::decode)
				.consumer(SelectRoleMessage::handle)
				.add();

		CHANNEL.messageBuilder(DrawParticleLineMessage.class, 15, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(DrawParticleLineMessage::encode).decoder(DrawParticleLineMessage::decode)
				.consumer(DrawParticleLineMessage::handle)
				.add();

		CHANNEL.messageBuilder(SpectatorPlayerActivityMessage.class, 16, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(SpectatorPlayerActivityMessage::encode).decoder(SpectatorPlayerActivityMessage::decode)
				.consumer(SpectatorPlayerActivityMessage::handle)
				.add();
	}
}
