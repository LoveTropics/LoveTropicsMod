package com.lovetropics.minigames.common.core.game.behavior.instances.team;

import com.google.common.base.Strings;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.config.BehaviorConfig;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.team.TeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.util.Scheduler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Blocks;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SetupTeamsBehavior implements IGameBehavior {
	private static final UnboundedMapCodec<String, List<UUID>> TEAM_ASSIGN = Codec.unboundedMap(Codec.STRING, MoreCodecs.UUID_STRING.listOf());
	private static final Codec<Object2IntMap<String>> TEAM_TO_SIZE = Codec.unboundedMap(Codec.STRING, Codec.INT)
			.xmap(Object2IntOpenHashMap::new, HashMap::new);

	private static final BehaviorConfig<List<TeamKey>> CFG_TEAMS = new BehaviorConfig<>("teams", TeamKey.CODEC.listOf())
			.enumHint("dye", s -> DyeColor.byTranslationKey(s, null));
	private static final BehaviorConfig<Map<String, List<UUID>>> CFG_ASSIGNED = new BehaviorConfig<>("assign", TEAM_ASSIGN);
	private static final BehaviorConfig<Object2IntMap<String>> CFG_MAX_SIZES = new BehaviorConfig<>("max_sizes", TEAM_TO_SIZE);

	public static final Codec<SetupTeamsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				CFG_TEAMS.forGetter(c -> c.teams),
				CFG_ASSIGNED.orElseGet(Object2ObjectOpenHashMap::new).forGetter(c -> c.assignedTeams),
				CFG_MAX_SIZES.orElseGet(Object2IntOpenHashMap::new).forGetter(c -> c.maxTeamSizes)
		).apply(instance, SetupTeamsBehavior::new);
	});

	private static final String JOIN_TEAM_KEY = "join_team";

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
	public ConfigList getConfigurables() {
		return ConfigList.builder()
				.with(CFG_TEAMS, this.teams)
				.with(CFG_ASSIGNED, this.assignedTeams)
				.with(CFG_MAX_SIZES, this.maxTeamSizes)
				.build();
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

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.ADD, player -> this.onPlayerWaiting(game, player));
		events.listen(GamePlayerEvents.USE_ITEM, this::onPlayerUseItem);
	}

	private void onPlayerWaiting(IGamePhase game, ServerPlayerEntity player) {
		PlayerRole registeredRole = game.getLobby().getPlayers().getRegisteredRoleFor(player);
		if (registeredRole != PlayerRole.SPECTATOR && teamState.getAllocations().getPollingTeams().size() > 1) {
			Scheduler.nextTick().run(server -> addTeamSelectorsTo(player));
		}
	}

	private ActionResultType onPlayerUseItem(ServerPlayerEntity player, Hand hand) {
		ItemStack heldStack = player.getHeldItem(hand);
		if (heldStack.isEmpty()) {
			return ActionResultType.PASS;
		}

		TeamKey team = getJoinTeamFor(heldStack);
		if (team != null) {
			onRequestJoinTeam(player, team);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	private void onRequestJoinTeam(ServerPlayerEntity player, TeamKey team) {
		teamState.getAllocations().setPlayerPreference(player.getUniqueID(), team);

		player.sendStatusMessage(
				new StringTextComponent("You have requested to join ").mergeStyle(TextFormatting.GRAY)
						.appendSibling(new StringTextComponent(team.name).mergeStyle(team.text, TextFormatting.BOLD)),
				false
		);
	}

	private void addTeamSelectorsTo(ServerPlayerEntity player) {
		player.sendStatusMessage(new StringTextComponent("This is a team-based game!").mergeStyle(TextFormatting.GOLD, TextFormatting.BOLD), false);
		player.sendStatusMessage(new StringTextComponent("You can select a team preference by using the items in your inventory:").mergeStyle(TextFormatting.GRAY), false);

		for (TeamKey team : teamState.getAllocations().getPollingTeams()) {
			IItemProvider wool = SheepEntity.WOOL_BY_COLOR.getOrDefault(team.dye, Blocks.WHITE_WOOL);
			player.addItemStackToInventory(this.createTeamSelector(wool, team));
		}
	}

	@Nullable
	private TeamKey getJoinTeamFor(ItemStack stack) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) return null;

		String joinTeam = tag.getString(JOIN_TEAM_KEY);
		if (!Strings.isNullOrEmpty(joinTeam)) {
			return teamState.getTeamByKey(joinTeam);
		} else {
			return null;
		}
	}

	private ItemStack createTeamSelector(IItemProvider item, TeamKey team) {
		ItemStack stack = new ItemStack(item);
		stack.setDisplayName(new StringTextComponent("Join " + team.name).mergeStyle(TextFormatting.BOLD, team.text));

		CompoundNBT tag = stack.getOrCreateTag();
		tag.putString(JOIN_TEAM_KEY, team.key);

		return stack;
	}

	private TeamKey getTeamOrThrow(String name) {
		TeamKey team = teamState.getTeamByKey(name);
		if (team == null) {
			throw new GameException(new StringTextComponent("Invalid team '" + name + "' specified for assignment"));
		}
		return team;
	}
}
