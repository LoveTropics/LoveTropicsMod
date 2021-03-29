package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGamePackageBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Collections;
import java.util.List;

public class SwapPlayersPackageBehavior implements IGamePackageBehavior {
	public static final Codec<SwapPlayersPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data)
		).apply(instance, SwapPlayersPackageBehavior::new);
	});

	protected final DonationPackageData data;
	private int swapCountdown;

	public SwapPlayersPackageBehavior(final DonationPackageData data) {
		this.data = data;
	}

	@Override
	public String getPackageType() {
		return data.getPackageType();
	}

	@Override
	public void worldUpdate(IGameInstance minigame, ServerWorld world) {
		if (swapCountdown <= 0) return;

		if (--swapCountdown <= 0) {
			List<ServerPlayerEntity> players = Lists.newArrayList(minigame.getParticipants());
			Collections.shuffle(players);

			for (int i = 0; i < players.size(); i++) {
				final ServerPlayerEntity player = players.get(i);
				final ServerPlayerEntity nextPlayer = players.get((i + 1) % players.size());
				final Vector3d teleportTo = nextPlayer.getPositionVec();

				player.setPositionAndUpdate(teleportTo.x, teleportTo.y, teleportTo.z);
			}
		}
	}

	@Override
	public boolean onGamePackageReceived(final IGameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			swapCountdown = 20;
			data.onReceive(minigame, null, gamePackage.getSendingPlayerName());

			return true;
		}

		return false;
	}
}