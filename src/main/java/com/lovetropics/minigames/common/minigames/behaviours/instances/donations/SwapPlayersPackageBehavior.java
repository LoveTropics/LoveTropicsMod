package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.stream.Collectors;

public class SwapPlayersPackageBehavior implements IMinigameBehavior
{
	protected final DonationPackageData data;

	public SwapPlayersPackageBehavior(final DonationPackageData data) {
		this.data = data;
	}

	public static <T> SwapPlayersPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);

		return new SwapPlayersPackageBehavior(data);
	}

	@Override
	public boolean onGamePackageReceived(final IMinigameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			List<ServerPlayerEntity> players = Lists.newLinkedList(minigame.getParticipants());
			List<Vec3d> positions = players.stream().map(Entity::getPositionVec).collect(Collectors.toList());

			for (int i = 0; i < players.size(); i++) {
				final ServerPlayerEntity player = players.get(i);
				final Vec3d teleportTo = positions.get((i + 1) % players.size());

				player.setPositionAndUpdate(teleportTo.x, teleportTo.y, teleportTo.z);
			}

			minigame.getParticipants().forEach(player -> data.onReceive(player, gamePackage.getSendingPlayerName()));

			return true;
		}

		return false;
	}
}
