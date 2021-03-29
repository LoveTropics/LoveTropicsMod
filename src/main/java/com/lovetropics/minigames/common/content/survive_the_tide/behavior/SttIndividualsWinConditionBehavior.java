package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.util.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.Iterator;

public class SttIndividualsWinConditionBehavior extends SttWinConditionBehavior {
	public static final Codec<SttIndividualsWinConditionBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.optionalFieldOf("game_finish_tick_delay", 0L).forGetter(c -> c.gameFinishTickDelay),
				MoreCodecs.long2Object(TemplatedText.CODEC).fieldOf("scheduled_game_finish_messages").forGetter(c -> c.scheduledGameFinishMessages),
				Codec.STRING.fieldOf("down_to_two_translation_key").forGetter(c -> c.downToTwoTranslationKey),
				Codec.BOOL.optionalFieldOf("spawn_lightning_bolts_on_finish", false).forGetter(c -> c.spawnLightningBoltsOnFinish),
				Codec.INT.optionalFieldOf("lightning_bolt_spawn_tick_rate", 60).forGetter(c -> c.lightningBoltSpawnTickRate)
		).apply(instance, SttIndividualsWinConditionBehavior::new);
	});

	private final String downToTwoTranslationKey;

	public SttIndividualsWinConditionBehavior(final long gameFinishTickDelay, final Long2ObjectMap<TemplatedText> scheduledGameFinishMessages, final String downToTwoTranslationKey, final boolean spawnLightningBoltsOnFinish, final int lightningBoltSpawnTickRate) {
		super(gameFinishTickDelay, scheduledGameFinishMessages, spawnLightningBoltsOnFinish, lightningBoltSpawnTickRate);
		this.downToTwoTranslationKey = downToTwoTranslationKey;
	}

	@Override
	public void onPlayerDeath(final IGameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		PlayerSet participants = minigame.getParticipants();

		if (participants.size() == 2) {
			Iterator<ServerPlayerEntity> it = participants.iterator();

			ServerPlayerEntity p1 = it.next();
			ServerPlayerEntity p2 = it.next();

			if (p1 != null && p2 != null) {
				ITextComponent p1text = p1.getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA);
				ITextComponent p2text = p2.getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA);

				minigame.getPlayers().sendMessage(new TranslationTextComponent(downToTwoTranslationKey, p1text, p2text).mergeStyle(TextFormatting.GOLD));
			}
		}

		if (participants.size() == 1) {
			ServerPlayerEntity winningPlayer = participants.iterator().next();
			this.triggerWin(winningPlayer.getDisplayName().deepCopy());

			minigame.getStatistics().getGlobal().set(StatisticKey.WINNING_PLAYER, PlayerKey.from(winningPlayer));
		}
	}
}
