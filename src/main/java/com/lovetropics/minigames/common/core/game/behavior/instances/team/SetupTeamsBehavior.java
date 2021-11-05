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
			.enumHint("[].dye", s -> DyeColor.byTranslationKey(s, null))
			.enumHint("[].text", TextFormatting::getValueByName);

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
				return new StringTextComponent("Join ").appendSibling(team.config().name())
						.mergeStyle(team.config().formatting());
			}

			@Override
			public IItemProvider getItemFor(GameTeam team) {
				return SheepEntity.WOOL_BY_COLOR.getOrDefault(team.config().dye(), Blocks.WHITE_WOOL);
			}
		};

		selectors = new SelectorItems<>(handlers, this.teams.toArray(new GameTeam[0]));
		selectors.applyTo(events);
	}

	private void onPlayerWaiting(IGamePhase game, ServerPlayerEntity player) {
		PlayerRole forcedRole = game.getLobby().getPlayers().getForcedRoleFor(player);
		if (forcedRole != PlayerRole.SPECTATOR && teamState.getPollingTeams().size() > 1) {
			player.sendStatusMessage(new StringTextComponent("This is a team-based game!").mergeStyle(TextFormatting.GOLD, TextFormatting.BOLD), false);
			player.sendStatusMessage(new StringTextComponent("You can select a team preference by using the items in your inventory:").mergeStyle(TextFormatting.GRAY), false);

			Scheduler.nextTick().run(server -> {
				selectors.giveSelectorsTo(player);
			});
		}
	}

	private void onRequestJoinTeam(ServerPlayerEntity player, GameTeam team) {
		teamState.getAllocations().setPlayerPreference(player.getUniqueID(), team.key());

		ITextComponent teamName = team.config().name().deepCopy().mergeStyle(team.config().formatting(), TextFormatting.BOLD);
		player.sendStatusMessage(
				new StringTextComponent("You have requested to join ").mergeStyle(TextFormatting.GRAY)
						.appendSibling(teamName),
				false
		);
	}
}
