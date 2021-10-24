package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.google.common.base.Strings;
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
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
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

	private static final String JOIN_TEAM_KEY = "join_team";

	private final List<GameTeam> teams;

	private TeamState teamState;

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
	public void registerState(IGamePhase game, GameStateMap state) {
		teamState = state.register(TeamState.KEY, new TeamState(this.teams));
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.ADD, player -> this.onPlayerWaiting(game, player));
		events.listen(GamePlayerEvents.USE_ITEM, this::onPlayerUseItem);
	}

	private void onPlayerWaiting(IGamePhase game, ServerPlayerEntity player) {
		PlayerRole registeredRole = game.getLobby().getPlayers().getRegisteredRoleFor(player);
		if (registeredRole != PlayerRole.SPECTATOR && teamState.getPollingTeams().size() > 1) {
			Scheduler.nextTick().run(server -> addTeamSelectorsTo(player));
		}
	}

	private ActionResultType onPlayerUseItem(ServerPlayerEntity player, Hand hand) {
		ItemStack heldStack = player.getHeldItem(hand);
		if (heldStack.isEmpty()) {
			return ActionResultType.PASS;
		}

		GameTeam team = getJoinTeamFor(heldStack);
		if (team != null) {
			onRequestJoinTeam(player, team);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
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

	private void addTeamSelectorsTo(ServerPlayerEntity player) {
		player.sendStatusMessage(new StringTextComponent("This is a team-based game!").mergeStyle(TextFormatting.GOLD, TextFormatting.BOLD), false);
		player.sendStatusMessage(new StringTextComponent("You can select a team preference by using the items in your inventory:").mergeStyle(TextFormatting.GRAY), false);

		for (GameTeam team : teamState.getPollingTeams()) {
			IItemProvider wool = SheepEntity.WOOL_BY_COLOR.getOrDefault(team.config().dye(), Blocks.WHITE_WOOL);
			player.addItemStackToInventory(this.createTeamSelector(wool, team));
		}
	}

	@Nullable
	private GameTeam getJoinTeamFor(ItemStack stack) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) return null;

		String joinTeam = tag.getString(JOIN_TEAM_KEY);
		if (!Strings.isNullOrEmpty(joinTeam)) {
			return teamState.getTeamByKey(joinTeam);
		} else {
			return null;
		}
	}

	private ItemStack createTeamSelector(IItemProvider item, GameTeam team) {
		ItemStack stack = new ItemStack(item);
		stack.setDisplayName(new StringTextComponent("Join ").appendSibling(team.config().name())
				.mergeStyle(TextFormatting.BOLD, team.config().formatting()));

		CompoundNBT tag = stack.getOrCreateTag();
		tag.putString(JOIN_TEAM_KEY, team.key().id());

		return stack;
	}
}
