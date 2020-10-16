package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.google.common.collect.Maps;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.VariableTextComponent;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;

public abstract class SttWinConditionBehavior implements IMinigameBehavior {
	protected boolean minigameEnded;
	private long minigameEndedTimer = 0;
	protected final long gameFinishTickDelay;
	private ITextComponent winner;
	protected final Long2ObjectMap<ITextComponent> scheduledGameFinishMessages;
	protected final boolean spawnLightningBoltsOnFinish;
	protected final int lightningBoltSpawnTickRate;

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
}
