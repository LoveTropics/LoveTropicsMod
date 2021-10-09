package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Map;

public final class AssignPlayerRolesBehavior implements IGameBehavior {
	public static final Codec<AssignPlayerRolesBehavior> CODEC = Codec.unit(AssignPlayerRolesBehavior::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		Map<ServerPlayerEntity, PlayerRole> roles = new Reference2ObjectOpenHashMap<>();

		events.listen(GamePhaseEvents.CREATE, () -> {
			TeamAllocator<PlayerRole, ServerPlayerEntity> allocator = game.getLobby().getPlayers().createRoleAllocator();
			allocator.setSizeForTeam(PlayerRole.PARTICIPANT, game.getDefinition().getMaximumParticipantCount());

			game.invoker(GamePlayerEvents.ALLOCATE_ROLES).onAllocateRoles(allocator);

			allocator.allocate(roles::put);
		});

		events.listen(GamePlayerEvents.ADD, player -> {
			PlayerRole role = roles.remove(player);
			game.setPlayerRole(player, role);
		});
	}
}
