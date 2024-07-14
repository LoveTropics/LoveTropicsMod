package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.client.toast.NotificationIcon;
import com.lovetropics.minigames.client.toast.NotificationStyle;
import com.lovetropics.minigames.client.toast.ShowNotificationToastMessage;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Map;

public record DonationPackageNotification(
		TemplatedText message,
		NotificationIcon icon,
		NotificationStyle.Sentiment sentiment,
		Holder<SoundEvent> sound
) {
	public static final Codec<DonationPackageNotification> CODEC = RecordCodecBuilder.create(i -> i.group(
			TemplatedText.CODEC.fieldOf("message").forGetter(c -> c.message),
			NotificationIcon.CODEC.optionalFieldOf("icon", NotificationIcon.item(new ItemStack(Items.GRASS_BLOCK))).forGetter(c -> c.icon),
			NotificationStyle.Sentiment.CODEC.optionalFieldOf("sentiment", NotificationStyle.Sentiment.NEUTRAL).forGetter(c -> c.sentiment),
			SoundEvent.CODEC.optionalFieldOf("sound", Holder.direct(SoundEvents.TOTEM_USE)).forGetter(c -> c.sound)
	).apply(i, DonationPackageNotification::new));

	public void onReceive(final IGamePhase game, @Nullable final ServerPlayer receiver, @Nullable final String sender) {
		PlayerSet players = game.getAllPlayers();

		Component targetedMessage = createTargetedMessage(receiver, sender);
		Component globalMessage = createGlobalMessage(receiver);

		for (ServerPlayer player : players) {
			boolean targeted = player == receiver || receiver == null;
			NotificationStyle.Color color = targeted ? NotificationStyle.Color.LIGHT : NotificationStyle.Color.DARK;
			Component message = targeted ? targetedMessage : globalMessage;
			long visibleTime = targeted ? 8000 : 6000;

			NotificationStyle style = createStyle(color, visibleTime);
			PacketDistributor.sendToPlayer(
					player,
					new ShowNotificationToastMessage(message, style)
			);

			if (targeted) {
				long seed = player.getRandom().nextLong();
				player.connection.send(new ClientboundSoundPacket(sound(), SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), 0.2f, 1f, seed));
			}
		}
	}

	public Component createTargetedMessage(@Nullable ServerPlayer receiver, @Nullable String sender) {
		return this.message.apply(Map.of("sender", this.getSenderName(sender), "receiver", this.getReceiverName(receiver)));
	}

	public Component createGlobalMessage(@Nullable ServerPlayer receiver) {
		return MinigameTexts.PACKAGE_RECEIVED.apply(getReceiverName(receiver));
	}

	public NotificationStyle createStyle(final NotificationStyle.Color color, final long visibleTime) {
		return new NotificationStyle(icon, sentiment, color, visibleTime);
	}

	private Component getReceiverName(@Nullable ServerPlayer receiver) {
		if (receiver != null) {
			return receiver.getDisplayName().copy().withStyle(ChatFormatting.BLUE);
		}
		return MinigameTexts.EVERYONE_RECEIVER;
	}

	private Component getSenderName(@Nullable String sender) {
		if (sender != null) {
			return Component.literal(sender).withStyle(ChatFormatting.BLUE);
		}
		return MinigameTexts.UNKNOWN_DONOR;
	}
}
