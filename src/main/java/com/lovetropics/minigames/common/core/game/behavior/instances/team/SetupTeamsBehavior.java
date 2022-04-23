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
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.UUID;

public final class SetupTeamsBehavior implements IGameBehavior {
	private static final GameTeam DEFAULT_TEAM = new GameTeam(
			new GameTeamKey(""),
			new GameTeamConfig(StringTextComponent.EMPTY, DyeColor.BLACK, TextFormatting.BLACK, ImmutableList.of(UUID.randomUUID()), 1)
	);
	private static final BehaviorConfig<List<GameTeam>> CFG_TEAMS = BehaviorConfig.fieldOf("teams", GameTeam.CODEC.listOf())
			.defaultInstanceHint("", DEFAULT_TEAM, GameTeam.CODEC)
			.listTypeHint("", ConfigType.COMPOSITE)
			.enumHint("[].dye", s -> DyeColor.byName(s, null))
			.enumHint("[].text", TextFormatting::getByName);

	public static final Codec<SetupTeamsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				CFG_TEAMS.forGetter(c -> c.teams)
		).apply(instance, SetupTeamsBehavior::new);
	});

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
	public void registerState(IGamePhase game, GameStateMap state) {
		teamState = state.register(TeamState.KEY, new TeamState(this.teams));
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.ADD, player -> this.onPlayerWaiting(game, player));

		SelectorItems.Handlers<GameTeam> handlers = new SelectorItems.Handlers<GameTeam>() {
			@Override
			public void onPlayerSelected(ServerPlayerEntity player, GameTeam team) {
				onRequestJoinTeam(player, team);
			}

			@Override
			public String getIdFor(GameTeam team) {
				return team.key().id();
			}

			@Override
			public ITextComponent getNameFor(GameTeam team) {
				return new StringTextComponent("Join ").append(team.config().name())
						.withStyle(team.config().formatting());
			}

			@Override
			public IItemProvider getItemFor(GameTeam team) {
				return SheepEntity.ITEM_BY_DYE.getOrDefault(team.config().dye(), Blocks.WHITE_WOOL);
			}
		};

		selectors = new SelectorItems<>(handlers, this.teams.toArray(new GameTeam[0]));
		selectors.applyTo(events);
	}

	private void onPlayerWaiting(IGamePhase game, ServerPlayerEntity player) {
		PlayerRole forcedRole = game.getLobby().getPlayers().getForcedRoleFor(player);
		if (forcedRole != PlayerRole.SPECTATOR && teamState.getPollingTeams().size() > 1) {
			player.displayClientMessage(new StringTextComponent("This is a team-based game!").withStyle(TextFormatting.GOLD, TextFormatting.BOLD), false);
			player.displayClientMessage(new StringTextComponent("You can select a team preference by using the items in your inventory:").withStyle(TextFormatting.GRAY), false);

			Scheduler.nextTick().run(server -> {
				selectors.giveSelectorsTo(player);
			});
		}
	}

	private void onRequestJoinTeam(ServerPlayerEntity player, GameTeam team) {
		teamState.getAllocations().setPlayerPreference(player.getUUID(), team.key());

		ITextComponent teamName = team.config().name().copy().withStyle(team.config().formatting(), TextFormatting.BOLD);
		player.displayClientMessage(
				new StringTextComponent("You have requested to join ").withStyle(TextFormatting.GRAY)
						.append(teamName),
				false
		);
	}
}
