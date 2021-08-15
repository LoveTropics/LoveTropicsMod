package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.client.minigame.NotifyDonationPackageMessage;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.potion.Effect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

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

	public void onReceive(final IActiveGame instance, @Nullable final ServerPlayerEntity player, @Nullable final String sendingPlayer) {
		Notification notification = this.notification;
		if (notification == null) return;

		PlayerSet players = instance.getAllPlayers();

		ITextComponent message = notification.createMessage(player, sendingPlayer);
		players.sendPacket(LoveTropicsNetwork.CHANNEL, new NotifyDonationPackageMessage(message, notification.icon));

		players.forEach(p -> p.connection.sendPacket(new SPlaySoundEffectPacket(notification.sound, SoundCategory.MASTER, p.getPosX(), p.getPosY(), p.getPosZ(), 0.2f, 1f)));
	}

	public static final class Notification {
		public static final Codec<Notification> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					TemplatedText.CODEC.fieldOf("message").forGetter(c -> c.message),
					Icon.CODEC.forGetter(c -> c.icon),
					SoundEvent.CODEC.optionalFieldOf("sound_on_receive", SoundEvents.ITEM_TOTEM_USE).forGetter(c -> c.sound)
			).apply(instance, Notification::new);
		});

		public final TemplatedText message;
		public final Icon icon;
		public final SoundEvent sound;

		public Notification(TemplatedText message, Icon icon, SoundEvent sound) {
			this.message = message;
			this.icon = icon;
			this.sound = sound;
		}

		ITextComponent createMessage(@Nullable ServerPlayerEntity receiver, @Nullable String sender) {
			return this.message.apply(
					new StringTextComponent(sender != null ? sender : "an unknown donor").mergeStyle(TextFormatting.GREEN),
					(receiver != null ? receiver.getDisplayName().deepCopy() : new StringTextComponent("Everyone")).mergeStyle(TextFormatting.GREEN)
			);
		}
	}

	public static final class Icon {
		public static final MapCodec<Icon> CODEC = RecordCodecBuilder.mapCodec(instance -> {
			return instance.group(
					MoreCodecs.ITEM_STACK.optionalFieldOf("item_icon").forGetter(c -> Optional.ofNullable(c.item)),
					Registry.EFFECTS.optionalFieldOf("effect_icon").forGetter(c -> Optional.ofNullable(c.effect))
			).apply(instance, Icon::new);
		});

		public final ItemStack item;
		public final Effect effect;

		public Icon(@Nullable ItemStack item, @Nullable Effect effect) {
			this.item = item;
			this.effect = effect;
		}

		public Icon(Optional<ItemStack> item, Optional<Effect> effect) {
			this(item.orElse(null), effect.orElse(null));
		}

		public void encode(PacketBuffer buffer) {
			if (this.item != null) {
				buffer.writeBoolean(true);
				buffer.writeItemStack(this.item);
			} else {
				buffer.writeBoolean(false);
				buffer.writeRegistryIdUnsafe(ForgeRegistries.POTIONS, this.effect);
			}
		}

		public static Icon decode(PacketBuffer buffer) {
			if (buffer.readBoolean()) {
				return new Icon(buffer.readItemStack(), null);
			} else {
				Effect effect = buffer.readRegistryIdUnsafe(ForgeRegistries.POTIONS);
				return new Icon(null, effect);
			}
		}
	}
}
