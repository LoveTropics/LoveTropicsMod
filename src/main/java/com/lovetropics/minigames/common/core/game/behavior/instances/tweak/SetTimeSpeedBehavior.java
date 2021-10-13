package com.lovetropics.minigames.common.core.game.behavior.instances.tweak;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.TimeInterpolationMessage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

public final class SetTimeSpeedBehavior implements IGameBehavior {
	public static final Codec<SetTimeSpeedBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("factor").forGetter(c -> c.factor)
		).apply(instance, SetTimeSpeedBehavior::new);
	});

	private final int factor;

	public SetTimeSpeedBehavior(int factor) {
		this.factor = factor;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.TICK, () -> {
			ServerWorld world = game.getWorld();
			world.setDayTime(world.getDayTime() + this.factor - 1);
		});

		events.listen(GamePlayerEvents.ADD, player -> {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TimeInterpolationMessage(this.factor));
		});

		events.listen(GamePlayerEvents.REMOVE, player -> {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), TimeInterpolationMessage.reset());
		});
	}
}
