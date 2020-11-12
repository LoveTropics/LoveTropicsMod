package com.lovetropics.minigames.common.minigames.behaviours.instances.build_competition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreCriteria.RenderType;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.StringTextComponent;

public class PollFinalistsBehavior implements IMinigameBehavior {

	public static <T> PollFinalistsBehavior parse(Dynamic<T> root) {
		return new PollFinalistsBehavior(
				root.get("finalists_tag").asString("finalist"),
				root.get("winner_tag").asString("winner"),
				root.get("votes_objective").asString("votes"),
				root.get("poll_duration").asString("5m"));
	}

	private static final Logger LOGGER = LogManager.getLogger();

	private final String finalistsTag;
	private final String winnerTag;
	private final String votesObjective;
	private final String pollDuration;

	public PollFinalistsBehavior(String finalistsTag, String winnerTag, String votesObjective, String pollDuration) {
		this.finalistsTag = finalistsTag;
		this.winnerTag = winnerTag;
		this.votesObjective = votesObjective;
		this.pollDuration = pollDuration;
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		minigame.addControlCommand("start_runoff", source -> {
			try {
				PlayerList players = source.getServer().getPlayerList();
				players.getPlayers().forEach(p -> p.removeTag(winnerTag));
				String[] finalists = players.getPlayers().stream()
						.filter(p -> p.getTags().contains(finalistsTag))
						.map(p -> p.getGameProfile().getName())
						.toArray(String[]::new);
				ObjectArrays.shuffle(finalists, RANDOM);
				minigame.getTelemetry().createPoll("Choose the best build!", pollDuration, finalists);
			} catch (Exception e) {
				LOGGER.error("Failed to start runoff:", e);
			}
		});
	}

	public void handlePollEvent(MinecraftServer server, JsonObject object, String crud) {
		if (crud.equals("create")) {
			LOGGER.info("New poll, resetting objective");
			Scoreboard scoreboard = server.getScoreboard();
			ScoreObjective objective = scoreboard.getObjective(votesObjective);
			if (objective != null) {
				scoreboard.removeObjective(objective);
			}
			scoreboard.addObjective(votesObjective, ScoreCriteria.DUMMY, new StringTextComponent("Votes"), RenderType.INTEGER);
			return;
		}
		PollEvent event = new Gson().fromJson(object, PollEvent.class);
		if (crud.equals("delete")) {
			LOGGER.info("Poll ended, finding winner");
			updateScores(server, event, true);
			Scoreboard scoreboard = server.getScoreboard();
			ScoreObjective objective = scoreboard.getObjective(votesObjective);
			Collection<Score> scores = scoreboard.getSortedScores(objective);
			if (!scores.isEmpty()) {
				String winner = Iterables.getLast(scores).getPlayerName();
				for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
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
		List<ServerPlayerEntity> leaders = new ArrayList<>();
		int leaderVotes = 0;

		for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
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
				ScoreObjective objective = scoreboard.getObjective(votesObjective);
				scoreboard.getOrCreateScore(username, objective).setScorePoints(votes);
			}
		}
		if (leaders.size() > 1) {
			// Shhhhh
			ServerPlayerEntity winner = leaders.get(RANDOM.nextInt(leaders.size()));
			Scoreboard scoreboard = server.getScoreboard();
			ScoreObjective objective = scoreboard.getObjective(votesObjective);
			scoreboard.getOrCreateScore(winner.getGameProfile().getName(), objective).incrementScore();
		}
	}

	public class PollEvent {

		int id;
		List<Option> options;
	}

	public class Option {

	    char key;
	    String title;
	    int results;
	}
}
