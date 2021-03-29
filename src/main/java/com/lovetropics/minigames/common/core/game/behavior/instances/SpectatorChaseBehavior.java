package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.network.ChaseCameraMessage;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.StopChaseCameraMessage;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SpectatorChaseBehavior implements IGameBehavior {
	public static final Codec<SpectatorChaseBehavior> CODEC = Codec.unit(SpectatorChaseBehavior::new);

	@Override
	public void onPlayerJoin(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		List<UUID> participants = collectParticipantIds(minigame);
		ChaseCameraMessage message = new ChaseCameraMessage(participants);
		if (role == PlayerRole.SPECTATOR) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
		}

		minigame.getSpectators().sendPacket(LoveTropicsNetwork.CHANNEL, message);
	}

	@Override
	public void onPlayerChangeRole(IGameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
		onPlayerJoin(minigame, player, role);
	}

	@Override
	public void onPlayerLeave(IGameInstance minigame, ServerPlayerEntity player) {
		List<UUID> participants = collectParticipantIds(minigame);
		ChaseCameraMessage message = new ChaseCameraMessage(participants);
		minigame.getSpectators().sendPacket(LoveTropicsNetwork.CHANNEL, message);

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new StopChaseCameraMessage());
	}

	@Override
	public void onFinish(IGameInstance minigame) {
		StopChaseCameraMessage message = new StopChaseCameraMessage();
		for (ServerPlayerEntity spectator : minigame.getSpectators()) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> spectator), message);
		}
	}

	private List<UUID> collectParticipantIds(IGameInstance minigame) {
		PlayerSet participants = minigame.getParticipants();
		List<UUID> ids = new ArrayList<>(participants.size());

		for (ServerPlayerEntity participant : participants) {
			ids.add(participant.getUniqueID());
		}

		return ids;
	}
}
