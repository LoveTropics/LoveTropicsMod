package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommand;
import com.lovetropics.minigames.common.core.game.state.team.TeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO: use items in player inventory for team selector instead of chat message
public final class SetupTeamsBehavior implements IGameBehavior {
	private static final UnboundedMapCodec<String, List<UUID>> TEAM_ASSIGN = Codec.unboundedMap(Codec.STRING, MoreCodecs.UUID_STRING.listOf());
	private static final Codec<Object2IntMap<String>> TEAM_TO_SIZE = Codec.unboundedMap(Codec.STRING, Codec.INT)
			.xmap(Object2IntOpenHashMap::new, HashMap::new);

	public static final Codec<SetupTeamsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				TeamKey.CODEC.listOf().fieldOf("teams").forGetter(c -> c.teams),
				TEAM_ASSIGN.fieldOf("assign").orElseGet(Object2ObjectOpenHashMap::new).forGetter(c -> c.assignedTeams),
				TEAM_TO_SIZE.fieldOf("max_sizes").orElseGet(Object2IntOpenHashMap::new).forGetter(c -> c.maxTeamSizes)
		).apply(instance, SetupTeamsBehavior::new);
	});

	private final List<TeamKey> teams;
	private final Map<String, List<UUID>> assignedTeams;
	private final Object2IntMap<String> maxTeamSizes;

	private TeamState teamState;

	public SetupTeamsBehavior(List<TeamKey> teams, Map<String, List<UUID>> assignedTeams, Object2IntMap<String> maxTeamSizes) {
		this.teams = teams;
		this.assignedTeams = assignedTeams;
		this.maxTeamSizes = maxTeamSizes;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		teamState = state.register(TeamState.KEY, new TeamState(this.teams));

		TeamState.Allocations allocations = teamState.getAllocations();

		for (TeamKey team : teams) {
			if (!assignedTeams.containsKey(team.key)) {
				allocations.addPollingTeam(team);
			}
		}

		for (Object2IntMap.Entry<String> entry : maxTeamSizes.object2IntEntrySet()) {
			TeamKey team = getTeamOrThrow(entry.getKey());
			allocations.setMaxTeamSize(team, entry.getIntValue());
		}

		for (Map.Entry<String, List<UUID>> entry : assignedTeams.entrySet()) {
			TeamKey team = getTeamOrThrow(entry.getKey());
			for (UUID player : entry.getValue()) {
				allocations.setPlayerAssignment(player, team);
			}
		}
	}

	private TeamKey getTeamOrThrow(String name) {
		TeamKey team = teamState.getTeamByKey(name);
		if (team == null) {
			throw new GameException(new StringTextComponent("Invalid team '" + name + "' specified for assignment"));
		}
		return team;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.registerJoinCommands(game);

		events.listen(GamePlayerEvents.ADD, player -> this.onPlayerWaiting(game, player));
	}

	private void registerJoinCommands(IGamePhase game) {
		for (TeamKey team : teamState.getAllocations().getPollingTeams()) {
			game.getControlCommands().add("join_team_" + team.key, ControlCommand.forEveryone(source -> {
				ServerPlayerEntity player = source.asPlayer();
				if (game.getAllPlayers().contains(player)) {
					onRequestJoinTeam(player, team);
				} else {
					player.sendStatusMessage(new StringTextComponent("You have not yet joined this game!").mergeStyle(TextFormatting.RED), false);
				}
			}));
		}
	}

	private void onPlayerWaiting(IGamePhase game, ServerPlayerEntity player) {
		PlayerRole registeredRole = game.getLobby().getPlayers().getRegisteredRoleFor(player);
		if (registeredRole != PlayerRole.SPECTATOR && teamState.getAllocations().getPollingTeams().size() > 1) {
			Scheduler.nextTick().run(server -> sendTeamSelectionTo(player));
		}
	}

	private void onRequestJoinTeam(ServerPlayerEntity player, TeamKey team) {
		teamState.getAllocations().setPlayerPreference(player.getUniqueID(), team);

		player.sendStatusMessage(
				new StringTextComponent("You have requested to join ").mergeStyle(TextFormatting.GRAY)
						.appendSibling(new StringTextComponent(team.name).mergeStyle(team.text, TextFormatting.BOLD)),
				false
		);
	}

	private void sendTeamSelectionTo(ServerPlayerEntity player) {
		player.sendStatusMessage(new StringTextComponent("This is a team-based game!").mergeStyle(TextFormatting.GOLD, TextFormatting.BOLD), false);
		player.sendStatusMessage(new StringTextComponent("You can select a team preference by clicking the links below:").mergeStyle(TextFormatting.GRAY), false);

		for (TeamKey team : teamState.getAllocations().getPollingTeams()) {
			Style linkStyle = Style.EMPTY
					.setFormatting(team.text)
					.setUnderlined(true)
					.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/game join_team_" + team.key))
					.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Join " + team.name)));

			player.sendStatusMessage(
					new StringTextComponent(" - ").mergeStyle(TextFormatting.GRAY)
							.appendSibling(new StringTextComponent("Join " + team.name).setStyle(linkStyle)),
					false
			);
		}
	}
}
