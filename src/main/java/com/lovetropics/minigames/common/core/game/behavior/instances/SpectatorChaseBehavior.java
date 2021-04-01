package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
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
	public void register(IGameInstance registerGame, EventRegistrar events) {
		events.listen(GamePlayerEvents.JOIN, this::onPlayerJoin);
		events.listen(GamePlayerEvents.CHANGE_ROLE, (game, player, role, lastRole) -> this.onPlayerJoin(game, player, role));
		events.listen(GamePlayerEvents.LEAVE, this::onPlayerLeave);

		events.listen(GameLifecycleEvents.STOP, this::onFinish);
	}

	private void onPlayerJoin(IGameInstance game, ServerPlayerEntity player, PlayerRole role) {
		List<UUID> participants = collectParticipantIds(game);
		ChaseCameraMessage message = new ChaseCameraMessage(participants);
		if (role == PlayerRole.SPECTATOR) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
		}

		game.getSpectators().sendPacket(LoveTropicsNetwork.CHANNEL, message);
	}

	private void onPlayerLeave(IGameInstance game, ServerPlayerEntity player) {
		List<UUID> participants = collectParticipantIds(game);
		ChaseCameraMessage message = new ChaseCameraMessage(participants);
		game.getSpectators().sendPacket(LoveTropicsNetwork.CHANNEL, message);

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new StopChaseCameraMessage());
	}

	private void onFinish(IGameInstance game) {
		StopChaseCameraMessage message = new StopChaseCameraMessage();
		for (ServerPlayerEntity spectator : game.getSpectators()) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> spectator), message);
		}
	}

	private List<UUID> collectParticipantIds(IGameInstance game) {
		PlayerSet participants = game.getParticipants();
		List<UUID> ids = new ArrayList<>(participants.size());

		for (ServerPlayerEntity participant : participants) {
			ids.add(participant.getUniqueID());
		}

		return ids;
	}
}
