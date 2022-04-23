package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

import javax.annotation.Nullable;

public class SetGameTypesBehavior implements IGameBehavior {
	public static final Codec<SetGameTypesBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.GAME_TYPE.optionalFieldOf("participant", GameType.SURVIVAL).forGetter(c -> c.participantGameType),
				MoreCodecs.GAME_TYPE.optionalFieldOf("spectator", GameType.SPECTATOR).forGetter(c -> c.spectatorGameType),
				MoreCodecs.GAME_TYPE.optionalFieldOf("all", GameType.ADVENTURE).forGetter(c -> c.allGameType)
		).apply(instance, SetGameTypesBehavior::new);
	});

	private final GameType participantGameType;
	private final GameType spectatorGameType;
	private final GameType allGameType;

	public SetGameTypesBehavior(GameType participantGameType, GameType spectatorGameType, GameType allGameType) {
		this.participantGameType = participantGameType;
		this.spectatorGameType = spectatorGameType;
		this.allGameType = allGameType;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.ADD, player -> applyRoleTo(player, null));
		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> applyRoleTo(player, role));
	}

	private void applyRoleTo(ServerPlayer player, @Nullable PlayerRole role) {
		GameType gameType = allGameType;
		if (role == PlayerRole.PARTICIPANT) {
			gameType = participantGameType;
		} else if (role == PlayerRole.SPECTATOR) {
			gameType = spectatorGameType;
		}
		player.setGameMode(gameType);
	}
}
