package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Optional;

public class DonationPackageData {
	public static final MapCodec<DonationPackageData> CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("package_type").forGetter(c -> c.packageType),
				TemplatedText.CODEC.optionalFieldOf("message_for_player").forGetter(c -> Optional.ofNullable(c.messageForPlayer)),
				DonationPackageBehavior.PlayerSelect.CODEC.optionalFieldOf("player_select", DonationPackageBehavior.PlayerSelect.RANDOM).forGetter(c -> c.playerSelect),
				SoundEvent.CODEC.optionalFieldOf("sound_on_receive", SoundEvents.ITEM_TOTEM_USE).forGetter(c -> c.soundOnReceive)
		).apply(instance, DonationPackageData::new);
	});

	protected final String packageType;
	protected final TemplatedText messageForPlayer;
	protected final DonationPackageBehavior.PlayerSelect playerSelect;
	protected final SoundEvent soundOnReceive;

	public DonationPackageData(final String packageType, final Optional<TemplatedText> messageForPlayer, final DonationPackageBehavior.PlayerSelect playerSelect, final SoundEvent soundOnReceive) {
		this.packageType = packageType;
		this.messageForPlayer = messageForPlayer.orElse(null);
		this.playerSelect = playerSelect;
		this.soundOnReceive = soundOnReceive;
	}

	public String getPackageType()
	{
		return packageType;
	}

	public TemplatedText getMessageForPlayer()
	{
		return messageForPlayer;
	}

	public DonationPackageBehavior.PlayerSelect getPlayerSelect()
	{
		return playerSelect;
	}

	public SoundEvent getSoundOnReceive()
	{
		return soundOnReceive;
	}

	public void onReceive(final IGameInstance instance, @Nullable final ServerPlayerEntity player, @Nullable final String sendingPlayer) {
		PlayerSet players = instance.getAllPlayers();

		if (messageForPlayer != null) {
			if (player != null) {
				players.sendMessage(messageForPlayer.apply(player.getDisplayName().deepCopy().mergeStyle(TextFormatting.BOLD, TextFormatting.GREEN)));
			} else {
				players.sendMessage(messageForPlayer.apply(""));
			}
		}

		if (sendingPlayer != null) {
			final ITextComponent sentByPlayerMessage = new StringTextComponent("Package sent by ").mergeStyle(TextFormatting.GOLD)
					.appendSibling(new StringTextComponent(sendingPlayer).mergeStyle(TextFormatting.GREEN, TextFormatting.BOLD));
			players.sendMessage(sentByPlayerMessage);
		}

		if (soundOnReceive != null) {
			players.forEach(p -> p.connection.sendPacket(new SPlaySoundEffectPacket(soundOnReceive, SoundCategory.MASTER, p.getPosX(), p.getPosY(), p.getPosZ(), 0.2f, 1f)));
		}
	}
}
