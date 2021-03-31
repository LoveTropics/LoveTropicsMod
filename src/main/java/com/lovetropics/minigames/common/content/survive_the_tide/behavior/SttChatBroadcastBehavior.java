package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Iterator;

public class SttChatBroadcastBehavior implements IGameBehavior {
	public static final Codec<SttChatBroadcastBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("down_to_two_translation_key").forGetter(c -> c.downToTwoTranslationKey)
		).apply(instance, SttChatBroadcastBehavior::new);
	});

	private final String downToTwoTranslationKey;

	public SttChatBroadcastBehavior(String downToTwoTranslationKey) {
		this.downToTwoTranslationKey = downToTwoTranslationKey;
	}

	@Override
	public void register(IGameInstance registerGame, EventRegistrar events) throws GameException {
		events.listen(GamePlayerEvents.DEATH, (game, player, damageSource) -> {
			PlayerSet participants = game.getParticipants();

			if (participants.size() == 2) {
				Iterator<ServerPlayerEntity> it = participants.iterator();
				ServerPlayerEntity p1 = it.next();
				ServerPlayerEntity p2 = it.next();

				if (p1 != null && p2 != null) {
					ITextComponent p1text = p1.getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA);
					ITextComponent p2text = p2.getDisplayName().deepCopy().mergeStyle(TextFormatting.AQUA);

					game.getAllPlayers().sendMessage(new TranslationTextComponent(downToTwoTranslationKey, p1text, p2text).mergeStyle(TextFormatting.GOLD));
				}
			}

			return ActionResultType.PASS;
		});
	}
}
