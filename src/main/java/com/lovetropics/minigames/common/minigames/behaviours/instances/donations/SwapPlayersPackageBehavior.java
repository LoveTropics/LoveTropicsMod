package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigamePackageBehavior;
import com.mojang.datafixers.Dynamic;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SwapPlayersPackageBehavior implements IMinigamePackageBehavior {
	protected final DonationPackageData data;
	private int swapCountdown;

	public SwapPlayersPackageBehavior(final DonationPackageData data) {
		this.data = data;
	}

	public static <T> SwapPlayersPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);

		return new SwapPlayersPackageBehavior(data);
	}

	@Override
	public String getPackageType() {
		return data.getPackageType();
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		if (swapCountdown <= 0) return;

		if (--swapCountdown <= 0) {
			List<ServerPlayerEntity> players = Lists.newArrayList(minigame.getParticipants());
			Collections.shuffle(players);

			for (int i = 0; i < players.size(); i++) {
				final ServerPlayerEntity player = players.get(i);
				final ServerPlayerEntity nextPlayer = players.get((i + 1) % players.size());
				final Vec3d teleportTo = nextPlayer.getPositionVec();

				player.setPositionAndUpdate(teleportTo.x, teleportTo.y, teleportTo.z);
			}
		}
	}

	@Override
	public boolean onGamePackageReceived(final IMinigameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			swapCountdown = 20;
			data.onReceive(minigame, null, gamePackage.getSendingPlayerName());

			return true;
		}

		return false;
	}
}
