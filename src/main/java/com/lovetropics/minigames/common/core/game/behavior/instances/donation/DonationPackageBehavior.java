package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.util.Util;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGamePackageBehavior;
import com.mojang.serialization.Codec;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.IStringSerializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.List;

// TODO: support combining behaviors for package received.. somehow..?
public abstract class DonationPackageBehavior implements IGamePackageBehavior
{
	private static final Logger LOGGER = LogManager.getLogger(DonationPackageBehavior.class);

	public enum PlayerSelect implements IStringSerializable
	{
		SPECIFIC("specific"), RANDOM("random"), ALL("all");

		public static final Codec<PlayerSelect> CODEC = IStringSerializable.createEnumCodec(PlayerSelect::values, PlayerSelect::getFromType);

		private final String type;

		PlayerSelect(final String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		@Override
		public String getString() {
			return type;
		}

		@Nullable
		public static PlayerSelect getFromType(final String type) {
			for (final PlayerSelect select : PlayerSelect.values()) {
				if (select.type.equals(type)) {
					return select;
				}
			}
			return null;
		}
	}

	protected final DonationPackageData data;

	public DonationPackageBehavior(final DonationPackageData data) {
		this.data = data;
	}

	@Override
	public String getPackageType() {
		return data.getPackageType();
	}

	@Override
	public boolean onGamePackageReceived(final IGameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			switch (data.playerSelect) {
				case SPECIFIC:
					if (gamePackage.getReceivingPlayer() == null) {
						LOGGER.warn("Expected donation package to have a receiving player, but did not receive from backend!");
						return false;
					}

					final boolean receiverIsParticipant = minigame.getParticipants().contains(gamePackage.getReceivingPlayer());

					if (!receiverIsParticipant) {
						// Player either died, left the server or isn't part of the minigame for some reason.
						return false;
					}

					final ServerPlayerEntity receivingPlayer = minigame.getServer().getPlayerList().getPlayerByUUID(gamePackage.getReceivingPlayer());

					if (receivingPlayer == null) {
						// Player not on the server for some reason
						return false;
					}

					receivePackageInternal(minigame, gamePackage.getSendingPlayerName(), receivingPlayer);
					data.onReceive(minigame, receivingPlayer, gamePackage.getSendingPlayerName());
					break;
				case RANDOM:
					final List<ServerPlayerEntity> players = Lists.newArrayList(minigame.getParticipants());
					final ServerPlayerEntity randomPlayer = players.get(minigame.getWorld().getRandom().nextInt(players.size()));

					receivePackageInternal(minigame, gamePackage.getSendingPlayerName(), randomPlayer);
					data.onReceive(minigame, randomPlayer, gamePackage.getSendingPlayerName());
					break;
				case ALL:
					minigame.getParticipants().stream().forEach(player -> receivePackageInternal(minigame, gamePackage.getSendingPlayerName(), player));
					data.onReceive(minigame, null, gamePackage.getSendingPlayerName());
					break;
			}

			return true;
		}

		return false;
	}

	protected abstract void receivePackage(@Nullable final String sendingPlayer, final ServerPlayerEntity player);

	protected boolean shouldGiveSenderHead() {
		return true;
	}

	private void receivePackageInternal(final IGameInstance instance, final String sendingPlayer, final ServerPlayerEntity player) {
		receivePackage(sendingPlayer, player);

		if (sendingPlayer != null && shouldGiveSenderHead()) {
			Util.addItemStackToInventory(player, createHeadForSender(sendingPlayer));
		}
	}

	protected ItemStack createHeadForSender(String sendingPlayer) {
		final ItemStack senderHead = new ItemStack(Items.PLAYER_HEAD);
		senderHead.getOrCreateTag().putString("SkullOwner", sendingPlayer);
		return senderHead;
	}

}