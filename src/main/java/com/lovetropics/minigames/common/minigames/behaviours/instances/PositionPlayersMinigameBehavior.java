package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class PositionPlayersMinigameBehavior implements IMinigameBehavior {
	private final BlockPos[] participantSpawns;
	private final BlockPos[] spectatorSpawns;

	private int participantSpawnIndex;
	private int spectatorSpawnIndex;

	public PositionPlayersMinigameBehavior(final BlockPos[] startPositions, BlockPos[] spectatorSpawns) {
		this.participantSpawns = startPositions;
		this.spectatorSpawns = spectatorSpawns;
	}

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			if (participantSpawns.length != minigame.getDefinition().getMaximumParticipantCount()) {
				throw new IllegalStateException("The participant positions length doesn't match the" +
						"maximum participant count defined by the following minigame definition! " + minigame.getDefinition().getID());
			}

			BlockPos teleportTo = participantSpawns[participantSpawnIndex++ % participantSpawns.length];
			DimensionUtils.teleportPlayerNoPortal(player, minigame.getDimension(), teleportTo);
		} else {
			BlockPos teleportTo = spectatorSpawns[spectatorSpawnIndex++ % spectatorSpawns.length];
			DimensionUtils.teleportPlayerNoPortal(player, minigame.getDimension(), teleportTo);
		}
	}
}
