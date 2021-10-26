package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class AssignPlayerRolesBehavior implements IGameBehavior {
	private static final BehaviorConfig<List<UUID>> CFG_FORCED_PARTICIPANTS = BehaviorConfig.fieldOf("forced_participants", MoreCodecs.UUID_STRING.listOf());

	public static final Codec<AssignPlayerRolesBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				CFG_FORCED_PARTICIPANTS.orElse(Collections.emptyList()).forGetter(c -> c.forcedParticipants)
		).apply(instance, AssignPlayerRolesBehavior::new);
	});

	private final List<UUID> forcedParticipants;

	public AssignPlayerRolesBehavior(List<UUID> forcedParticipants) {
		this.forcedParticipants = forcedParticipants;
	}

	@Override
	public ConfigList getConfigurables() {
		return ConfigList.builder()
				.with(CFG_FORCED_PARTICIPANTS, this.forcedParticipants)
				.build();
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		Map<ServerPlayerEntity, PlayerRole> roles = new Reference2ObjectOpenHashMap<>();

		events.listen(GamePhaseEvents.CREATE, () -> {
			this.allocateRoles(game, roles::put);
		});

		events.listen(GamePlayerEvents.ADD, player -> {
			PlayerRole role = roles.remove(player);
			if (role != null) {
				game.setPlayerRole(player, role);
			}
		});
	}

	private void allocateRoles(IGamePhase game, BiConsumer<ServerPlayerEntity, PlayerRole> apply) {
		TeamAllocator<PlayerRole, ServerPlayerEntity> allocator = game.getLobby().getPlayers().createRoleAllocator();
		allocator.setSizeForTeam(PlayerRole.PARTICIPANT, game.getDefinition().getMaximumParticipantCount());

		this.applyForcedParticipants(game, allocator);

		game.invoker(GamePlayerEvents.ALLOCATE_ROLES).onAllocateRoles(allocator);

		allocator.allocate(apply);
	}

	private void applyForcedParticipants(IGamePhase game, TeamAllocator<PlayerRole, ServerPlayerEntity> allocator) {
		for (UUID uuid : this.forcedParticipants) {
			ServerPlayerEntity player = game.getAllPlayers().getPlayerBy(uuid);
			if (player != null) {
				allocator.addPlayer(player, PlayerRole.PARTICIPANT);
			}
		}
	}
}
