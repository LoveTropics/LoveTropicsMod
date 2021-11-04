package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface IGameLookup {
	@Nullable
	IGameLobby getLobbyFor(PlayerEntity player);

	@Nullable
	default IGameLobby getLobbyFor(CommandSource source) {
		Entity entity = source.getEntity();
		if (entity instanceof PlayerEntity) {
			return getLobbyFor((PlayerEntity) entity);
		}
		return null;
	}

	@Nullable
	IGamePhase getGamePhaseFor(PlayerEntity player);

	@Nullable
	IGamePhase getGamePhaseAt(World world, Vector3d pos);

	@Nullable
	default IGamePhase getGamePhaseFor(CommandSource source) {
		Entity entity = source.getEntity();
		if (entity instanceof PlayerEntity) {
			return getGamePhaseFor((PlayerEntity) entity);
		}

		IGamePhase game = getGamePhaseAt(source.getWorld(), source.getPos());
		if (game != null) {
			return game;
		}

		return null;
	}

	@Nullable
	default IGamePhase getGamePhaseFor(Entity entity) {
		if (entity.world.isRemote) {
			return null;
		}

		if (entity instanceof PlayerEntity) {
			return getGamePhaseFor((PlayerEntity) entity);
		} else {
			return getGamePhaseAt(entity.world, entity.getPositionVec());
		}
	}

	@Nullable
	default IGamePhase getGamePhaseAt(World world, BlockPos pos) {
		return getGamePhaseAt(world, Vector3d.copyCentered(pos));
	}
}
