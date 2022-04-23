package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.client.toast.NotificationDisplay;
import com.lovetropics.minigames.client.toast.NotificationIcon;
import com.lovetropics.minigames.client.toast.ShowNotificationToastMessage;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.text.*;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class DonationPackageData {
	public static final MapCodec<DonationPackageData> CODEC = RecordCodecBuilder.mapCodec(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("package_type").forGetter(c -> c.packageType),
				Notification.CODEC.optionalFieldOf("notification").forGetter(c -> Optional.ofNullable(c.notification)),
				DonationPackageBehavior.PlayerSelect.CODEC.optionalFieldOf("player_select", DonationPackageBehavior.PlayerSelect.RANDOM).forGetter(c -> c.playerSelect)
		).apply(instance, DonationPackageData::new);
	});

	protected final String packageType;
	protected final Notification notification;
	protected final DonationPackageBehavior.PlayerSelect playerSelect;

	public DonationPackageData(final String packageType, final Optional<Notification> notification, final DonationPackageBehavior.PlayerSelect playerSelect) {
		this.packageType = packageType;
		this.notification = notification.orElse(null);
		this.playerSelect = playerSelect;
	}

	public String getPackageType() {
		return packageType;
	}

	@Nullable
	public Notification getNotification() {
		return notification;
	}

	public DonationPackageBehavior.PlayerSelect getPlayerSelect() {
		return playerSelect;
	}

	public void onReceive(final IGamePhase game, @Nullable final ServerPlayer receiver, @Nullable final String sender) {
		Notification notification = this.notification;
		if (notification == null) return;

		PlayerSet players = game.getAllPlayers();

		Component targetedMessage = notification.createTargetedMessage(receiver, sender);
		Component globalMessage = notification.createGlobalMessage(receiver);

		for (ServerPlayer player : players) {
			boolean targeted = player == receiver || receiver == null;
			NotificationDisplay.Color color = targeted ? NotificationDisplay.Color.LIGHT : NotificationDisplay.Color.DARK;
			Component message = targeted ? targetedMessage : globalMessage;
			long visibleTime = targeted ? 8000 : 6000;

			NotificationDisplay display = new NotificationDisplay(notification.icon, notification.sentiment, color, visibleTime);
			LoveTropicsNetwork.CHANNEL.send(
					PacketDistributor.PLAYER.with(() -> player),
					new ShowNotificationToastMessage(message, display)
			);

			if (targeted) {
				player.connection.send(new ClientboundSoundPacket(notification.sound, SoundSource.MASTER, player.getX(), player.getY(), player.getZ(), 0.2f, 1f));
			}
		}
	}

	public static final class Notification {
		public static final Codec<Notification> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					TemplatedText.CODEC.fieldOf("message").forGetter(c -> c.message),
					NotificationIcon.CODEC.optionalFieldOf("icon", NotificationIcon.item(new ItemStack(Items.GRASS_BLOCK))).forGetter(c -> c.icon),
					NotificationDisplay.Sentiment.CODEC.optionalFieldOf("sentiment", NotificationDisplay.Sentiment.NEUTRAL).forGetter(c -> c.sentiment),
					SoundEvent.CODEC.optionalFieldOf("sound_on_receive", SoundEvents.TOTEM_USE).forGetter(c -> c.sound)
			).apply(instance, Notification::new);
		});

		public final TemplatedText message;
		public final NotificationIcon icon;
		public final NotificationDisplay.Sentiment sentiment;
		public final SoundEvent sound;

		public Notification(TemplatedText message, NotificationIcon icon, NotificationDisplay.Sentiment sentiment, SoundEvent sound) {
			this.message = message;
			this.icon = icon;
			this.sentiment = sentiment;
			this.sound = sound;
		}

		Component createTargetedMessage(@Nullable ServerPlayer receiver, @Nullable String sender) {
			return this.message.apply(this.getSenderName(sender), this.getReceiverName(receiver));
		}

		Component createGlobalMessage(@Nullable ServerPlayer receiver) {
			return new TranslatableComponent("%s received a package!", this.getReceiverName(receiver));
		}

		private MutableComponent getReceiverName(ServerPlayer receiver) {
			return (receiver != null ? receiver.getDisplayName().copy() : new TextComponent("Everyone")).withStyle(ChatFormatting.BLUE);
		}

		private MutableComponent getSenderName(@Nullable String sender) {
			return new TextComponent(sender != null ? sender : "an unknown donor").withStyle(ChatFormatting.BLUE);
		}
	}
}
