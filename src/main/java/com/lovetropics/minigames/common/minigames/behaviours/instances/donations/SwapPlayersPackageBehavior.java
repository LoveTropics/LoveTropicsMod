package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.game_actions.DonationPackageGameAction;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SwapPlayersPackageBehavior implements IMinigameBehavior
{
	protected final String packageType;
	protected final ITextComponent messageForPlayer;

	public SwapPlayersPackageBehavior(final String packageType, final ITextComponent messageForPlayer) {
		this.packageType = packageType;
		this.messageForPlayer = messageForPlayer;
	}

	public static <T> SwapPlayersPackageBehavior parse(Dynamic<T> root) {
		final String packageType = root.get("package_type").asString("");
		final ITextComponent messageForPlayer = Util.getText(root, "message_for_player");

		return new SwapPlayersPackageBehavior(packageType, messageForPlayer);
	}

	@Override
	public boolean onDonationPackageRequested(final IMinigameInstance minigame, final DonationPackageGameAction action) {
		if (action.getPackageType().equals(packageType)) {
			List<ServerPlayerEntity> players = Lists.newLinkedList(minigame.getParticipants());
			List<Vec3d> positions = players.stream().map(Entity::getPositionVec).collect(Collectors.toList());

			for (int i = 0; i < players.size(); i++) {
				final ServerPlayerEntity player = players.get(i);
				final Vec3d teleportTo = positions.get((i + 1) % players.size());

				player.setPositionAndUpdate(teleportTo.x, teleportTo.y, teleportTo.z);
			}

			minigame.getParticipants().sendMessage(messageForPlayer);

			return true;
		}

		return false;
	}
}
