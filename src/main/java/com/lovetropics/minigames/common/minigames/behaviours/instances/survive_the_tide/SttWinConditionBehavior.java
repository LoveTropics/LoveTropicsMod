package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.google.common.collect.Maps;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.VariableTextComponent;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.techstack.ParticipantEntry;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.*;

public abstract class SttWinConditionBehavior implements IMinigameBehavior {
	protected boolean minigameEnded;
	private long minigameEndedTimer = 0;
	protected final long gameFinishTickDelay;
	private ITextComponent winner;
	protected final Long2ObjectMap<ITextComponent> scheduledGameFinishMessages;
	protected final boolean spawnLightningBoltsOnFinish;
	protected final int lightningBoltSpawnTickRate;

	protected final Object2IntOpenHashMap<UUID> killsByPlayer = new Object2IntOpenHashMap<>();
	protected final List<UUID> deathOrder = new ArrayList<>();

	public SttWinConditionBehavior(final long gameFinishTickDelay, final Long2ObjectMap<ITextComponent> scheduledGameFinishMessages, final boolean spawnLightningBoltsOnFinish, final int lightningBoltSpawnTickRate) {
		this.gameFinishTickDelay = gameFinishTickDelay;
		this.scheduledGameFinishMessages = scheduledGameFinishMessages;
		this.spawnLightningBoltsOnFinish = spawnLightningBoltsOnFinish;
		this.lightningBoltSpawnTickRate = lightningBoltSpawnTickRate;
	}

	protected final void triggerWin(ITextComponent winner) {
		this.winner = winner;
		this.minigameEnded = true;
		this.minigameEndedTimer = 0;
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		this.checkForGameEndCondition(minigame, world);
	}

	@Override
	public void onFinish(final IMinigameInstance minigame) {
		if (minigameEnded) {
			List<ParticipantEntry> results = buildResults(minigame);
			minigame.sendMinigameResults(results);
		}

		this.minigameEnded = false;
		this.minigameEndedTimer = 0;
	}

	private void checkForGameEndCondition(final IMinigameInstance minigame, final World world) {
		if (this.minigameEnded) {
			if (spawnLightningBoltsOnFinish) {
				spawnLightningBoltsEverywhere(minigame, world);
			}

			sendGameFinishMessages(minigame);

			if (this.minigameEndedTimer >= gameFinishTickDelay) {
				MinigameManager.getInstance().finish();
			}

			this.minigameEndedTimer++;
		}
	}

	private void spawnLightningBoltsEverywhere(IMinigameInstance minigame, final World world) {
		if (this.minigameEndedTimer % lightningBoltSpawnTickRate == 0) {
			PlayerSet participants = minigame.getParticipants();
			if (participants.isEmpty()) {
				return;
			}

			for (ServerPlayerEntity player : participants) {
				int xOffset = (7 + world.rand.nextInt(5)) * (world.rand.nextBoolean() ? 1 : -1);
				int zOffset = (7 + world.rand.nextInt(5)) * (world.rand.nextBoolean() ? 1 : -1);

				int posX = MathHelper.floor(player.getPosX()) + xOffset;
				int posZ = MathHelper.floor(player.getPosZ()) + zOffset;

				int posY = world.getHeight(Heightmap.Type.MOTION_BLOCKING, posX, posZ);

				((ServerWorld) world).addLightningBolt(new LightningBoltEntity(world, posX, posY, posZ, true));
			}
		}
	}

	private void sendGameFinishMessages(final IMinigameInstance minigame) {
		if (scheduledGameFinishMessages.containsKey(minigameEndedTimer)) {
			final ITextComponent message = scheduledGameFinishMessages.get(minigameEndedTimer);

			final Map<String, String> variables = Maps.newHashMap();
			if (winner != null) {
				variables.put("#winner", winner.getFormattedText());
			}

			minigame.getPlayers().sendMessage(new VariableTextComponent(message, variables));
		}
	}

	@Override
	public void onPlayerDeath(IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		Entity source = event.getSource().getTrueSource();
		if (source instanceof ServerPlayerEntity) {
			killsByPlayer.addTo(source.getUniqueID(), 1);
		}

		deathOrder.add(player.getUniqueID());
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		deathOrder.add(player.getUniqueID());
	}

	// TODO: should this only be for individuals?
	private List<ParticipantEntry> buildResults(IMinigameInstance minigame) {
		MinecraftServer server = minigame.getServer();
		PlayerProfileCache profileCache = server.getPlayerProfileCache();

		List<ParticipantEntry> participantResults = new ArrayList<>();

		int place = 0;
		int lastKills = -1;

		PlayerSet alivePlayers = minigame.getParticipants();

		List<UUID> finalists = new ArrayList<>(alivePlayers.size());
		for (ServerPlayerEntity player : alivePlayers) {
			finalists.add(player.getUniqueID());
		}
		finalists.sort(Comparator.comparingInt(killsByPlayer::getInt).reversed());

		for (UUID id : finalists) {
			int kills = killsByPlayer.getInt(id);

			// if this player has the same number of kills as the last finalist, we want to assign them the same place
			if (kills != lastKills) place++;
			lastKills = kills;

			GameProfile profile = profileCache.getProfileByUUID(id);
			participantResults.add(ParticipantEntry.withKills(profile, place, kills));
		}

		// iterate the players who died from most recent to oldest
		for (int i = deathOrder.size() - 1; i >= 0; i--) {
			UUID id = deathOrder.get(i);
			int kills = killsByPlayer.getInt(id);

			GameProfile profile = profileCache.getProfileByUUID(id);
			participantResults.add(ParticipantEntry.withKills(profile, ++place, kills));
		}

		return participantResults;
	}
}
