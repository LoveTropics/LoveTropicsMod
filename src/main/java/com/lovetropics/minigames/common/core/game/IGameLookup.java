package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface IGameLookup {
	@Nullable
	IGameLobby getLobbyFor(Player player);

	@Nullable
	default IGameLobby getLobbyFor(CommandSourceStack source) {
		Entity entity = source.getEntity();
		if (entity instanceof Player) {
			return getLobbyFor((Player) entity);
		}
		return null;
	}

	@Nullable
	IGamePhase getGamePhaseFor(Player player);

	@Nullable
	IGamePhase getGamePhaseAt(Level world, Vec3 pos);

	@Nullable
	default IGamePhase getGamePhaseFor(CommandSourceStack source) {
		Entity entity = source.getEntity();
		if (entity instanceof Player) {
			return getGamePhaseFor((Player) entity);
		}

		IGamePhase game = getGamePhaseAt(source.getLevel(), source.getPosition());
		if (game != null) {
			return game;
		}

		return null;
	}

	@Nullable
	default IGamePhase getGamePhaseFor(Entity entity) {
		if (entity.level.isClientSide) {
			return null;
		}

		if (entity instanceof Player) {
			return getGamePhaseFor((Player) entity);
		} else {
			return getGamePhaseAt(entity.level, entity.position());
		}
	}

	@Nullable
	default IGamePhase getGamePhaseAt(Level world, BlockPos pos) {
		return getGamePhaseAt(world, Vec3.atCenterOf(pos));
	}
}
