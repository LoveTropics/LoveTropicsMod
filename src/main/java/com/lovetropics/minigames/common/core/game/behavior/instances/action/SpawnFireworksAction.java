package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;

public record SpawnFireworksAction(
		boolean useTeamColor
) implements IGameBehavior {
	public static final MapCodec<SpawnFireworksAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.BOOL.optionalFieldOf("use_team_color", true).forGetter(SpawnFireworksAction::useTeamColor)
	).apply(i, SpawnFireworksAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			BlockPos fireworkPos = player.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, player.blockPosition());
			FireworkPalette palette = selectPalette(game, player);
			palette.spawn(fireworkPos, player.level());
			return true;
		});
	}

	private FireworkPalette selectPalette(IGamePhase game, ServerPlayer player) {
		if (useTeamColor) {
			TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
			GameTeamKey team = teams != null ? teams.getTeamForPlayer(player) : null;
			if (team != null) {
				return FireworkPalette.forDye(teams.getTeamOrThrow(team).config().dye());
			}
		}
		return FireworkPalette.DYE_COLORS;
	}
}
