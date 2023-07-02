package com.lovetropics.minigames.client.particle_line;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public record DrawParticleLineMessage(ParticleOptions particle, Vec3 from, Vec3 to, float spacing) {
	public void encode(FriendlyByteBuf buffer) {
		buffer.writeRegistryIdUnsafe(ForgeRegistries.PARTICLE_TYPES, particle.getType());
		particle.writeToNetwork(buffer);

		buffer.writeDouble(from.x);
		buffer.writeDouble(from.y);
		buffer.writeDouble(from.z);
		buffer.writeDouble(to.x);
		buffer.writeDouble(to.y);
		buffer.writeDouble(to.z);
		buffer.writeFloat(spacing);
	}

	public static DrawParticleLineMessage decode(FriendlyByteBuf buffer) {
		ParticleType<?> particleType = buffer.readRegistryIdUnsafe(ForgeRegistries.PARTICLE_TYPES);
		ParticleOptions particle = readParticle(buffer, particleType);

		Vec3 from = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		Vec3 to = new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		float spacing = buffer.readFloat();

		return new DrawParticleLineMessage(particle, from, to, spacing);
	}

	private static <T extends ParticleOptions> T readParticle(FriendlyByteBuf buffer, ParticleType<T> particleType) {
		return particleType.getDeserializer().fromNetwork(particleType, buffer);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ParticleLineSpawner.spawnParticleLine(particle, from, to, spacing);
	}
}
