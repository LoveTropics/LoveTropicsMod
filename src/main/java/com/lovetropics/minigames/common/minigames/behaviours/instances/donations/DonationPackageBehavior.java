package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.game_actions.DonationPackageGameAction;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

// TODO: support combining behaviors for package received.. somehow..?
public abstract class DonationPackageBehavior implements IMinigameBehavior
{
	private static final Logger LOGGER = LogManager.getLogger(DonationPackageBehavior.class);

	public enum PlayerSelect
	{
		SPECIFIC("specific"), RANDOM("random"), ALL("all");

		private final String type;

		PlayerSelect(final String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		public static Optional<PlayerSelect> getFromType(final String type) {
			for (final PlayerSelect select : PlayerSelect.values()) {
				if (select.type.equals(type)) {
					return Optional.of(select);
				}
			}

			return Optional.empty();
		}
	}

	protected final DonationPackageData data;

	public DonationPackageBehavior(final DonationPackageData data) {
		this.data = data;
	}

	@Override
	public boolean onDonationPackageRequested(final IMinigameInstance minigame, final DonationPackageGameAction action) {
		if (action.getPackageType().equals(data.packageType)) {
			switch (data.playerSelect) {
				case SPECIFIC:
					if (action.getReceivingPlayer() == null) {
						LOGGER.warn("Expected donation package to have a receiving player, but did not receive from backend!");
						return false;
					}

					final boolean receiverIsParticipant = minigame.getParticipants().contains(action.getReceivingPlayer());

					if (!receiverIsParticipant) {
						// Player either died, left the server or isn't part of the minigame for some reason.
						return false;
					}

					final ServerPlayerEntity receivingPlayer = minigame.getServer().getPlayerList().getPlayerByUUID(action.getReceivingPlayer());

					if (receivingPlayer == null) {
						// Player not on the server for some reason
						return false;
					}

					receivePackageInternal(action.getSendingPlayerName(), receivingPlayer);
					break;
				case RANDOM:
					final List<ServerPlayerEntity> players = Lists.newArrayList(minigame.getParticipants());
					final ServerPlayerEntity randomPlayer = players.get(minigame.getWorld().getRandom().nextInt(players.size()));

					receivePackageInternal(action.getSendingPlayerName(), randomPlayer);
					break;
				case ALL:
					minigame.getParticipants().stream().forEach(player -> receivePackageInternal(action.getSendingPlayerName(), player));
					break;
			}

			return true;
		}

		return false;
	}

	protected abstract void receivePackage(final String sendingPlayer, final ServerPlayerEntity player);

	protected boolean shouldGiveSenderHead() {
		return true;
	}

	private void receivePackageInternal(final String sendingPlayer, final ServerPlayerEntity player) {
		receivePackage(sendingPlayer, player);

		if (shouldGiveSenderHead()) {
			Util.addItemStackToInventory(player, createHeadForSender(sendingPlayer));
		}

		data.onReceive(player, sendingPlayer);
	}

	protected ItemStack createHeadForSender(String sendingPlayer) {
		final ItemStack senderHead = new ItemStack(Items.PLAYER_HEAD);
		senderHead.getOrCreateTag().putString("SkullOwner", sendingPlayer);
		return senderHead;
	}

}
