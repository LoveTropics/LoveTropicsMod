package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.MoreCodecs;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.TemplatedText;
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

public class DonationPackageData {
	public static final MapCodec<DonationPackageData> CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("package_type").forGetter(c -> c.packageType),
				MoreCodecs.nullableFieldOf(TemplatedText.CODEC, "message_for_player").forGetter(c -> c.messageForPlayer),
				DonationPackageBehavior.PlayerSelect.CODEC.optionalFieldOf("player_select", DonationPackageBehavior.PlayerSelect.RANDOM).forGetter(c -> c.playerSelect),
				SoundEvent.CODEC.optionalFieldOf("sound_on_receive", SoundEvents.ITEM_TOTEM_USE).forGetter(c -> c.soundOnReceive)
		).apply(instance, DonationPackageData::new);
	});

	protected final String packageType;
	protected final TemplatedText messageForPlayer;
	protected final DonationPackageBehavior.PlayerSelect playerSelect;
	protected final SoundEvent soundOnReceive;

	public DonationPackageData(final String packageType, final TemplatedText messageForPlayer, final DonationPackageBehavior.PlayerSelect playerSelect, final SoundEvent soundOnReceive) {
		this.packageType = packageType;
		this.messageForPlayer = messageForPlayer;
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

	public void onReceive(final IMinigameInstance instance, @Nullable final ServerPlayerEntity player, @Nullable final String sendingPlayer) {
		if (messageForPlayer != null) {
			instance.getPlayers().forEach(p -> {
				if (player != null) {
					p.sendStatusMessage(messageForPlayer.apply(player.getDisplayName().deepCopy().mergeStyle(TextFormatting.BOLD, TextFormatting.GREEN)), false);
				} else {
					p.sendStatusMessage(messageForPlayer.apply(""), false);
				}
			});

			if (sendingPlayer != null) {
				final ITextComponent sentByPlayerMessage = new StringTextComponent("Package sent by ").mergeStyle(TextFormatting.GOLD)
						.appendSibling(new StringTextComponent(sendingPlayer).mergeStyle(TextFormatting.GREEN, TextFormatting.BOLD));
				instance.getPlayers().sendMessage(sentByPlayerMessage);
			}

			if (soundOnReceive != null) {
				instance.getPlayers().forEach(p -> p.connection.sendPacket(new SPlaySoundEffectPacket(soundOnReceive, SoundCategory.MASTER, p.getPosX(), p.getPosY(), p.getPosZ(), 0.2f, 1f)));
			}
		}
	}
}
