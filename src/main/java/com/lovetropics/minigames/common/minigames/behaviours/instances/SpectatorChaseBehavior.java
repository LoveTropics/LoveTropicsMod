package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.network.LTNetwork;
import com.lovetropics.minigames.common.network.StartChaseCameraMessage;
import com.lovetropics.minigames.common.network.StopChaseCameraMessage;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SpectatorChaseBehavior implements IMinigameBehavior {
	public static <T> SpectatorChaseBehavior parse(Dynamic<T> root) {
		return new SpectatorChaseBehavior();
	}

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		onPlayerChangeRole(minigame, player, role);
	}

	@Override
	public void onPlayerChangeRole(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.SPECTATOR) {
			List<UUID> participants = collectParticipantIds(minigame);
			LTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new StartChaseCameraMessage(participants));
		}
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		LTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new StopChaseCameraMessage());
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		StopChaseCameraMessage message = new StopChaseCameraMessage();
		for (ServerPlayerEntity spectator : minigame.getSpectators()) {
			LTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> spectator), message);
		}
	}

	private List<UUID> collectParticipantIds(IMinigameInstance minigame) {
		PlayerSet participants = minigame.getParticipants();
		List<UUID> ids = new ArrayList<>(participants.size());

		for (ServerPlayerEntity participant : participants) {
			ids.add(participant.getUniqueID());
		}

		return ids;
	}
}