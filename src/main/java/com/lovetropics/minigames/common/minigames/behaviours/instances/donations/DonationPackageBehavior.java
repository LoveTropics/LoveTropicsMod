package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.game_actions.DonationPackageGameAction;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Optional;

public abstract class DonationPackageBehavior implements IMinigameBehavior
{
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

	protected final String packageType;
	protected final ITextComponent messageForPlayer;
	protected final PlayerSelect playerSelect;

	public DonationPackageBehavior(final String packageType, final ITextComponent messageForPlayer, final PlayerSelect playerSelect) {
		this.packageType = packageType;
		this.messageForPlayer = messageForPlayer;
		this.playerSelect = playerSelect;
	}

	@Override
	public boolean onDonationPackageRequested(final IMinigameInstance minigame, final DonationPackageGameAction action) {
		if (action.getPackageType().equals(packageType)) {
			switch (playerSelect) {
				case SPECIFIC:
					if (action.getReceivingPlayer() == null) {
						throw new RuntimeException("Expected donation package to have a receiving player,"
								+ " but did not receive from backend!");
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

	private void receivePackageInternal(final String sendingPlayer, final ServerPlayerEntity player) {
		receivePackage(sendingPlayer, player);

		final ItemStack senderHead = new ItemStack(Items.PLAYER_HEAD);
		senderHead.getOrCreateTag().putString("SkullOwner", sendingPlayer);

		Util.addItemStackToInventory(player, senderHead);

		final ITextComponent sentByPlayerMessage = new StringTextComponent("Sent by ").applyTextStyle(TextFormatting.GOLD)
				.appendSibling(new StringTextComponent(sendingPlayer).applyTextStyles(TextFormatting.GREEN, TextFormatting.BOLD));

		player.sendMessage(messageForPlayer);
		player.sendMessage(sentByPlayerMessage);
	}
}
