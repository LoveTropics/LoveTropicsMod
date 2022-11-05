package com.lovetropics.minigames.common.content.build_competition;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommand;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraft.world.scores.criteria.ObjectiveCriteria.RenderType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.network.chat.TextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public record PollFinalistsBehavior(String finalistsTag, String winnerTag, String votesObjective, String pollDuration) implements IGameBehavior {
	public static final Codec<PollFinalistsBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.optionalFieldOf("finalists_tag", "finalist").forGetter(c -> c.finalistsTag),
			Codec.STRING.optionalFieldOf("winner_tag", "winner").forGetter(c -> c.winnerTag),
			Codec.STRING.optionalFieldOf("votes_objective", "votes").forGetter(c -> c.votesObjective),
			Codec.STRING.optionalFieldOf("poll_duration", "5m").forGetter(c -> c.pollDuration)
	).apply(i, PollFinalistsBehavior::new));

	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		GameInstanceTelemetry telemetry = game.getTelemetryOrThrow();
		game.getControlCommands().add("start_runoff", ControlCommand.forAdmins(source -> {
			try {
				PlayerList players = source.getServer().getPlayerList();
				players.getPlayers().forEach(p -> p.removeTag(winnerTag));
				String[] finalists = players.getPlayers().stream()
						.filter(p -> p.getTags().contains(finalistsTag))
						.map(p -> p.getGameProfile().getName())
						.toArray(String[]::new);
				ObjectArrays.shuffle(finalists, RANDOM);
				telemetry.createPoll("Choose the best build!", pollDuration, finalists);
			} catch (Exception e) {
				LOGGER.error("Failed to start runoff:", e);
			}
		}));

		events.listen(GamePackageEvents.RECEIVE_POLL_EVENT, (object, crud) -> handlePollEvent(game, object, crud));
	}

	private void handlePollEvent(IGamePhase game, JsonObject object, String crud) {
		MinecraftServer server = game.getServer();

		if (crud.equals("create")) {
			LOGGER.info("New poll, resetting objective");
			Scoreboard scoreboard = server.getScoreboard();
			Objective objective = scoreboard.getOrCreateObjective(votesObjective);
			if (objective != null) {
				scoreboard.removeObjective(objective);
			}
			scoreboard.addObjective(votesObjective, ObjectiveCriteria.DUMMY, new TextComponent("Votes"), RenderType.INTEGER);
			return;
		}
		PollEvent event = new Gson().fromJson(object, PollEvent.class);
		if (crud.equals("delete")) {
			LOGGER.info("Poll ended, finding winner");
			updateScores(server, event, true);
			Scoreboard scoreboard = server.getScoreboard();
			Objective objective = scoreboard.getOrCreateObjective(votesObjective);
			Collection<Score> scores = scoreboard.getPlayerScores(objective);
			if (!scores.isEmpty()) {
				String winner = Iterables.getLast(scores).getOwner();
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					player.removeTag(finalistsTag);
					if (player.getGameProfile().getName().equals(winner)) {
						player.addTag(winnerTag);
					}
				}
			}
		} else {
			updateScores(server, event, false);
		}
	}

	private static final Random RANDOM = new Random();

	private void updateScores(MinecraftServer server, PollEvent event, boolean forceWinner) {
		Object2IntMap<String> votesByName = new Object2IntArrayMap<>();
		votesByName.defaultReturnValue(-1);
		for (Option option : event.options) {
			votesByName.put(option.title, option.results);
		}
		List<ServerPlayer> leaders = new ArrayList<>();
		int leaderVotes = 0;

		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			String username = player.getGameProfile().getName();
			int votes = votesByName.getInt(username);
			if (votes >= 0) {
				if (forceWinner) {
					if (votes > leaderVotes) {
						leaders.clear();
						leaderVotes = votes;
						leaders.add(player);
					} else if (votes == leaderVotes) {
						leaders.add(player);
					}
				}
				Scoreboard scoreboard = server.getScoreboard();
				Objective objective = scoreboard.getOrCreateObjective(votesObjective);
				scoreboard.getOrCreatePlayerScore(username, objective).setScore(votes);
			}
		}
		if (leaders.size() > 1) {
			// Shhhhh
			ServerPlayer winner = leaders.get(RANDOM.nextInt(leaders.size()));
			Scoreboard scoreboard = server.getScoreboard();
			Objective objective = scoreboard.getOrCreateObjective(votesObjective);
			scoreboard.getOrCreatePlayerScore(winner.getGameProfile().getName(), objective).increment();
		}
	}

	public static class PollEvent {

		int id;
		List<Option> options;
	}

	public static class Option {

		char key;
		String title;
		int results;
	}
}
