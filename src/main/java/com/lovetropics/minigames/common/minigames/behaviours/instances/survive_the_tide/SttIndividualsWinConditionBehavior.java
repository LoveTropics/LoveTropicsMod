package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Iterator;

public class SttIndividualsWinConditionBehavior extends SttWinConditionBehavior {
	private final String downToTwoTranslationKey;

	public SttIndividualsWinConditionBehavior(final long gameFinishTickDelay, final Long2ObjectMap<ITextComponent> scheduledGameFinishMessages, final String downToTwoTranslationKey, final boolean spawnLightningBoltsOnFinish, final int lightningBoltSpawnTickRate) {
		super(gameFinishTickDelay, scheduledGameFinishMessages, spawnLightningBoltsOnFinish, lightningBoltSpawnTickRate);
		this.downToTwoTranslationKey = downToTwoTranslationKey;
	}

	public static <T> SttIndividualsWinConditionBehavior parse(Dynamic<T> root) {
		final long gameFinishTickDelay = root.get("game_finish_tick_delay").asLong(0);
		final Long2ObjectMap<ITextComponent> scheduledShutdownMessages = new Long2ObjectOpenHashMap<>(root.get("scheduled_game_finish_messages").asMap(
				key -> Long.parseLong(key.asString("0")),
				Util::getText
		));

		final String downToTwoTranslationKey = root.get("down_to_two_translation_key").asString("");
		final boolean spawnLightningBoltsOnFinish = root.get("spawn_lightning_bolts_on_finish").asBoolean(false);
		final int lightningBoltSpawnTickRate = root.get("lightning_bolt_spawn_tick_rate").asInt(60);

		return new SttIndividualsWinConditionBehavior(gameFinishTickDelay, scheduledShutdownMessages, downToTwoTranslationKey, spawnLightningBoltsOnFinish, lightningBoltSpawnTickRate);
	}

	@Override
	public void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		PlayerSet participants = minigame.getParticipants();

		if (participants.size() == 2) {
			Iterator<ServerPlayerEntity> it = participants.iterator();

			ServerPlayerEntity p1 = it.next();
			ServerPlayerEntity p2 = it.next();

			if (p1 != null && p2 != null) {
				ITextComponent p1text = p1.getDisplayName().deepCopy().applyTextStyle(TextFormatting.AQUA);
				ITextComponent p2text = p2.getDisplayName().deepCopy().applyTextStyle(TextFormatting.AQUA);

				minigame.getPlayers().sendMessage(new TranslationTextComponent(downToTwoTranslationKey, p1text, p2text).applyTextStyle(TextFormatting.GOLD));
			}
		}

		if (participants.size() == 1) {
			ServerPlayerEntity winningPlayer = participants.iterator().next();
			this.triggerWin(winningPlayer.getDisplayName().deepCopy());
		}
	}
}
