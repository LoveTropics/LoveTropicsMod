package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigType;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamConfig;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.SelectorItems;
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.List;
import java.util.UUID;

public final class SetupTeamsBehavior implements IGameBehavior {
	private static final GameTeam DEFAULT_TEAM = new GameTeam(
			new GameTeamKey(""),
			new GameTeamConfig(TextComponent.EMPTY, DyeColor.BLACK, ChatFormatting.BLACK, ImmutableList.of(UUID.randomUUID()), 1)
	);
	private static final BehaviorConfig<List<GameTeam>> CFG_TEAMS = BehaviorConfig.fieldOf("teams", GameTeam.CODEC.listOf())
			.defaultInstanceHint("", DEFAULT_TEAM, GameTeam.CODEC)
			.listTypeHint("", ConfigType.COMPOSITE)
			.enumHint("[].dye", s -> DyeColor.byName(s, null))
			.enumHint("[].text", ChatFormatting::getByName);

	public static final Codec<SetupTeamsBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			CFG_TEAMS.forGetter(c -> c.teams)
	).apply(i, SetupTeamsBehavior::new));

	private final List<GameTeam> teams;

	private TeamState teamState;
	private SelectorItems<GameTeam> selectors;

	public SetupTeamsBehavior(List<GameTeam> teams) {
		this.teams = teams;
	}

	@Override
	public ConfigList getConfigurables() {
		return ConfigList.builder()
				.with(CFG_TEAMS, this.teams)
				.build();
	}

	@Override
	public IGameBehavior configure(ConfigList configs) {
		return new SetupTeamsBehavior(CFG_TEAMS.getValue(configs));
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap phaseState, GameStateMap instanceState) {
		teamState = instanceState.register(TeamState.KEY, new TeamState(this.teams));
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.ADD, player -> this.onPlayerWaiting(game, player));

		SelectorItems.Handlers<GameTeam> handlers = new SelectorItems.Handlers<GameTeam>() {
			@Override
			public void onPlayerSelected(ServerPlayer player, GameTeam team) {
				onRequestJoinTeam(player, team);
			}

			@Override
			public String getIdFor(GameTeam team) {
				return team.key().id();
			}

			@Override
			public Component getNameFor(GameTeam team) {
				return Component.literal("Join ").append(team.config().name())
						.withStyle(team.config().formatting());
			}

			@Override
			public ItemLike getItemFor(GameTeam team) {
				return Sheep.ITEM_BY_DYE.getOrDefault(team.config().dye(), Blocks.WHITE_WOOL);
			}
		};

		selectors = new SelectorItems<>(handlers, this.teams.toArray(new GameTeam[0]));
		selectors.applyTo(events);
	}

	private void onPlayerWaiting(IGamePhase game, ServerPlayer player) {
		PlayerRole forcedRole = game.getLobby().getPlayers().getForcedRoleFor(player);
		if (forcedRole != PlayerRole.SPECTATOR && teamState.getPollingTeams().size() > 1) {
			player.displayClientMessage(Component.literal("This is a team-based game!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
			player.displayClientMessage(Component.literal("You can select a team preference by using the items in your inventory:").withStyle(ChatFormatting.GRAY), false);

			Scheduler.nextTick().run(server -> {
				selectors.giveSelectorsTo(player);
			});
		}
	}

	private void onRequestJoinTeam(ServerPlayer player, GameTeam team) {
		teamState.getAllocations().setPlayerPreference(player.getUUID(), team.key());

		Component teamName = team.config().name().copy().withStyle(team.config().formatting(), ChatFormatting.BOLD);
		player.displayClientMessage(
				Component.literal("You have requested to join ").withStyle(ChatFormatting.GRAY)
						.append(teamName),
				false
		);
	}
}
