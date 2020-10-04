package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.google.common.collect.Maps;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.VariableTextComponent;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class SurviveTheTideWinConditionBehavior implements IMinigameBehavior
{
	private boolean minigameEnded;
	private long minigameEndedTimer = 0;
	private UUID winningPlayer;
	private ITextComponent winningPlayerName;
	private final long gameFinishTickDelay;
	private final Map<Long, ITextComponent> scheduledGameFinishMessages;
	private final String downToTwoTranslationKey;
	private final boolean spawnLightningBoltsOnFinish;
	private final int lightningBoltSpawnTickRate;

	public SurviveTheTideWinConditionBehavior(final long gameFinishTickDelay, final Map<Long, ITextComponent> scheduledGameFinishMessages,
			final String downToTwoTranslationKey, final boolean spawnLightningBoltsOnFinish, final int lightningBoltSpawnTickRate) {
		this.gameFinishTickDelay = gameFinishTickDelay;
		this.scheduledGameFinishMessages = scheduledGameFinishMessages;
		this.downToTwoTranslationKey = downToTwoTranslationKey;
		this.spawnLightningBoltsOnFinish = spawnLightningBoltsOnFinish;
		this.lightningBoltSpawnTickRate = lightningBoltSpawnTickRate;
	}

	public static <T> SurviveTheTideWinConditionBehavior parse(Dynamic<T> root) {
		final long gameFinishTickDelay = root.get("game_finish_tick_delay").asLong(0);
		final Map<Long, ITextComponent> scheduledShutdownMessages = root.get("scheduled_game_finish_messages").asMap(
				key -> Long.parseLong(key.asString("0")),
				Util::getText
		);
		final String downToTwoTranslationKey = root.get("down_to_two_translation_key").asString("");
		final boolean spawnLightningBoltsOnFinish = root.get("spawn_lightning_bolts_on_finish").asBoolean(false);
		final int lightningBoltSpawnTickRate = root.get("lightning_bolt_spawn_tick_rate").asInt(60);

		return new SurviveTheTideWinConditionBehavior(gameFinishTickDelay, scheduledShutdownMessages, downToTwoTranslationKey, spawnLightningBoltsOnFinish, lightningBoltSpawnTickRate);
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		this.checkForGameEndCondition(minigame, world);
	}

	@Override
	public void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player) {
		final MinecraftServer server = player.getServer();

		if (minigame.getParticipants().size() == 2) {
			Iterator<ServerPlayerEntity> it = minigame.getParticipants().iterator();

			ServerPlayerEntity p1 = it.next();
			ServerPlayerEntity p2 = it.next();

			if (p1 != null && p2 != null) {
				ITextComponent p1text = p1.getDisplayName().deepCopy().applyTextStyle(TextFormatting.AQUA);
				ITextComponent p2text = p2.getDisplayName().deepCopy().applyTextStyle(TextFormatting.AQUA);

				minigame.getPlayers().sendMessage(new TranslationTextComponent(downToTwoTranslationKey, p1text, p2text).applyTextStyle(TextFormatting.GOLD));
			}
		}

		if (minigame.getParticipants().size() == 1) {
			this.minigameEnded = true;

			this.winningPlayer = minigame.getParticipants().iterator().next().getUniqueID();
			this.winningPlayerName = server.getPlayerList().getPlayerByUUID(this.winningPlayer).getDisplayName().deepCopy();
		}
	}

	@Override
	public void onFinish(final IMinigameInstance minigame) {
		this.minigameEnded = false;
		this.minigameEndedTimer = 0;
		this.winningPlayer = null;
	}

	private void checkForGameEndCondition(final IMinigameInstance minigame, final World world) {
		if (this.minigameEnded) {
			if (spawnLightningBoltsOnFinish) {
				spawnLightningBoltsEverywhere(world);
			}

			sendGameFinishMessages(minigame);

			if (this.minigameEndedTimer >= gameFinishTickDelay) {
				MinigameManager.getInstance().finish();
			}

			this.minigameEndedTimer++;
		}
	}

	private void spawnLightningBoltsEverywhere(final World world) {
		if (this.minigameEndedTimer % lightningBoltSpawnTickRate == 0) {
			ServerPlayerEntity winning = world.getServer().getPlayerList().getPlayerByUUID(this.winningPlayer);

			if (winning != null) {
				int xOffset = (7 + world.rand.nextInt(5)) * (world.rand.nextBoolean() ? 1 : -1);
				int zOffset =  (7 + world.rand.nextInt(5)) * (world.rand.nextBoolean() ? 1 : -1);

				int posX = MathHelper.floor(winning.getPosX()) + xOffset;
				int posZ = MathHelper.floor(winning.getPosZ()) + zOffset;

				int posY = world.getHeight(Heightmap.Type.MOTION_BLOCKING, posX, posZ);

				((ServerWorld)world).addLightningBolt(new LightningBoltEntity(world, posX, posY, posZ, true));
			}
		}
	}

	private void sendGameFinishMessages(final IMinigameInstance minigame) {
		if (scheduledGameFinishMessages.containsKey(minigameEndedTimer)) {
			final ITextComponent message = scheduledGameFinishMessages.get(minigameEndedTimer);
			final Map<String, String> variables = Maps.newHashMap();

			variables.put("#winning_player", winningPlayerName.getFormattedText());
			minigame.getPlayers().sendMessage(new VariableTextComponent(message, variables));
		}
	}
}
