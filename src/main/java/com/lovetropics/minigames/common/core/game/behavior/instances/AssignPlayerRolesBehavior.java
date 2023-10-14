package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigType;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public record AssignPlayerRolesBehavior(List<UUID> forcedParticipants) implements IGameBehavior {
	private static final Logger LOGGER = LogManager.getLogger(AssignPlayerRolesBehavior.class);
	private static final ResourceLocation CONFIG_ID = new ResourceLocation("assign_roles");
	private static final BehaviorConfig<List<UUID>> CFG_FORCED_PARTICIPANTS = BehaviorConfig.fieldOf("forced_participants", UUIDUtil.STRING_CODEC.listOf())
			.listTypeHint("", ConfigType.STRING);

	public static final Codec<AssignPlayerRolesBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			CFG_FORCED_PARTICIPANTS.orElse(Collections.emptyList()).forGetter(c -> c.forcedParticipants)
	).apply(i, AssignPlayerRolesBehavior::new));

	@Override
	public ConfigList getConfigurables() {
		return ConfigList.builder(CONFIG_ID)
				.with(CFG_FORCED_PARTICIPANTS, this.forcedParticipants)
				.build();
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		Map<ServerPlayer, PlayerRole> roles = new Reference2ObjectOpenHashMap<>();

		// TODO: somehow if a player is in a lobby and then leaves they can get in such a state as to join late and be joined as a participant when clicking 'play'
		events.listen(GamePhaseEvents.CREATE, () -> {
			this.allocateRoles(game, roles::put);
			LOGGER.info("AFTER ALLOCATION: " + roles);
		});

		events.listen(GamePlayerEvents.ADD, player -> {
			PlayerRole role = roles.remove(player);
			if (role != null) {
				game.setPlayerRole(player, role);
			}
		});

		events.listen(GamePhaseEvents.START, roles::clear);
	}

	private void allocateRoles(IGamePhase game, BiConsumer<ServerPlayer, PlayerRole> apply) {
		LOGGER.info("SELECTED ROLES: " + game.getLobby().getPlayers().getRoleSelections());
		TeamAllocator<PlayerRole, ServerPlayer> allocator = game.getLobby().getPlayers().createRoleAllocator();
		allocator.setSizeForTeam(PlayerRole.PARTICIPANT, game.getDefinition().getMaximumParticipantCount());
		LOGGER.info("TEAM SIZE: " + game.getDefinition().getMaximumParticipantCount());
		this.applyForcedParticipants(game, allocator);

		game.invoker(GamePlayerEvents.ALLOCATE_ROLES).onAllocateRoles(allocator);
		LOGGER.info("SELECTED ROLES: " + game.getLobby().getPlayers().getRoleSelections());

		allocator.allocate(apply);
	}

	private void applyForcedParticipants(IGamePhase game, TeamAllocator<PlayerRole, ServerPlayer> allocator) {
		LOGGER.info("FORCING PARTICIPANTS: " + this.forcedParticipants);
		for (UUID uuid : this.forcedParticipants) {
			ServerPlayer player = game.getAllPlayers().getPlayerBy(uuid);
			if (player != null) {
				allocator.addPlayer(player, PlayerRole.PARTICIPANT);
			}
		}
	}
}
