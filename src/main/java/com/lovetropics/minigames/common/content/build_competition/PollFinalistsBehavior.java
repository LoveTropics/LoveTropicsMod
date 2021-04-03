package com.lovetropics.minigames.common.content.build_competition;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.core.game.control.ControlCommand;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class PollFinalistsBehavior implements IGameBehavior {

	public static final Codec<PollFinalistsBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("finalists_tag", "finalist").forGetter(c -> c.finalistsTag),
				Codec.STRING.optionalFieldOf("winner_tag", "winner").forGetter(c -> c.winnerTag),
				Codec.STRING.optionalFieldOf("votes_objective", "votes").forGetter(c -> c.votesObjective),
				Codec.STRING.optionalFieldOf("poll_duration", "5m").forGetter(c -> c.pollDuration)
		).apply(instance, PollFinalistsBehavior::new);
	});

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
	public void register(IActiveGame game, EventRegistrar events) throws GameException {
		game.getControlCommands().add("start_runoff", ControlCommand.forAdmins(source -> {
			try {
				PlayerList players = source.getServer().getPlayerList();
				players.getPlayers().forEach(p -> p.removeTag(winnerTag));
				String[] finalists = players.getPlayers().stream()
						.filter(p -> p.getTags().contains(finalistsTag))
						.map(p -> p.getGameProfile().getName())
						.toArray(String[]::new);
				ObjectArrays.shuffle(finalists, RANDOM);
				game.getTelemetry().createPoll("Choose the best build!", pollDuration, finalists);
			} catch (Exception e) {
				LOGGER.error("Failed to start runoff:", e);
			}
		}));

		events.listen(GamePackageEvents.RECEIVE_POLL_EVENT, this::handlePollEvent);
	}

	private void handlePollEvent(IActiveGame game, JsonObject object, String crud) {
		MinecraftServer server = game.getServer();

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
