package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.GameType;

public class SetGameTypesBehavior implements IGameBehavior {
	public static final Codec<SetGameTypesBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.GAME_TYPE.fieldOf("participant").forGetter(c -> c.participantGameType),
				MoreCodecs.GAME_TYPE.fieldOf("spectator").forGetter(c -> c.spectatorGameType)
		).apply(instance, SetGameTypesBehavior::new);
	});

	private final GameType participantGameType;
	private final GameType spectatorGameType;

	public SetGameTypesBehavior(GameType participantGameType, GameType spectatorGameType) {
		this.participantGameType = participantGameType;
		this.spectatorGameType = spectatorGameType;
	}

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) {
		events.listen(GamePlayerEvents.JOIN, (game, player, role) -> applyToPlayer(player, role));
		events.listen(GamePlayerEvents.CHANGE_ROLE, (game, player, role, lastRole) -> applyToPlayer(player, role));
	}

	private void applyToPlayer(ServerPlayerEntity player, PlayerRole role) {
		player.setGameType(role == PlayerRole.PARTICIPANT ? participantGameType : spectatorGameType);
	}
}
