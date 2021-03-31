package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Collections;
import java.util.List;

public class SwapPlayersPackageBehavior implements IGameBehavior {
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
	public void register(IGameInstance registerGame, EventRegistrar events) {
		events.listen(GamePackageEvents.APPLY_PACKAGE, (game, player, sendingPlayer) -> swapCountdown = 20);
		events.listen(GameLifecycleEvents.TICK, this::tick);
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
