package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGamePackageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

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
	public void register(IGameInstance registerGame, GameEventListeners events) throws GameException {
		events.listen(GamePackageEvents.RECEIVE_PACKAGE, this::onGamePackageReceived);
		events.listen(GameLifecycleEvents.TICK, this::tick);
	}

	private boolean onGamePackageReceived(final IGameInstance game, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			swapCountdown = 20;
			data.onReceive(game, null, gamePackage.getSendingPlayerName());

			return true;
		}

		return false;
	}

	private void tick(IGameInstance game) {
		if (swapCountdown <= 0) return;

		if (--swapCountdown <= 0) {
			List<ServerPlayerEntity> players = Lists.newArrayList(game.getParticipants());
			Collections.shuffle(players);

			for (int i = 0; i < players.size(); i++) {
				final ServerPlayerEntity player = players.get(i);
				final ServerPlayerEntity nextPlayer = players.get((i + 1) % players.size());
				final Vector3d teleportTo = nextPlayer.getPositionVec();

				player.setPositionAndUpdate(teleportTo.x, teleportTo.y, teleportTo.z);
			}
		}
	}
}
