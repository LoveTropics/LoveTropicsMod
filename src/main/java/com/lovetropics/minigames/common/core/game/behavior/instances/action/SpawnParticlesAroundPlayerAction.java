package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.phys.AABB;

public record SpawnParticlesAroundPlayerAction(ParticleOptions[] particles, IntProvider count, IntProvider repeats, double radius) implements IGameBehavior {
	public static final Codec<SpawnParticlesAroundPlayerAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.arrayOrUnit(ParticleTypes.CODEC, ParticleOptions[]::new).fieldOf("particles").forGetter(c -> c.particles),
			IntProvider.POSITIVE_CODEC.fieldOf("count").forGetter(c -> c.count),
			IntProvider.POSITIVE_CODEC.optionalFieldOf("repeats", ConstantInt.of(1)).forGetter(c -> c.repeats),
			Codec.DOUBLE.optionalFieldOf("radius", 0.0).forGetter(c -> c.radius)
	).apply(i, SpawnParticlesAroundPlayerAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			RandomSource random = player.level().random;

			int count = this.count.sample(random);
			AABB bounds = player.getBoundingBox().inflate(radius);
			for (int i = 0; i < count; i++) {
				double x = bounds.minX + random.nextDouble() * bounds.getXsize();
				double y = bounds.minY + random.nextDouble() * bounds.getYsize();
				double z = bounds.minZ + random.nextDouble() * bounds.getZsize();

				ParticleOptions particle = particles[random.nextInt(particles.length)];
				int repeats = this.repeats.sample(random);

				player.connection.send(new ClientboundLevelParticlesPacket(particle, false, x, y, z, 0.1f, 0.1f, 0.1f, 0.0f, repeats));
			}

			return true;
		});
	}
}
