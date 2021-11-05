package com.lovetropics.minigames.client.particle_line;

import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public final class DrawParticleLineMessage {
	private final IParticleData particle;
	private final Vector3d from;
	private final Vector3d to;
	private final float spacing;

	public DrawParticleLineMessage(IParticleData particle, Vector3d from, Vector3d to, float spacing) {
		this.particle = particle;
		this.from = from;
		this.to = to;
		this.spacing = spacing;
	}

	public void encode(PacketBuffer buffer) {
		buffer.writeRegistryIdUnsafe(ForgeRegistries.PARTICLE_TYPES, particle.getType());
		particle.write(buffer);

		buffer.writeDouble(from.x);
		buffer.writeDouble(from.y);
		buffer.writeDouble(from.z);
		buffer.writeDouble(to.x);
		buffer.writeDouble(to.y);
		buffer.writeDouble(to.z);
		buffer.writeFloat(spacing);
	}

	public static DrawParticleLineMessage decode(PacketBuffer buffer) {
		ParticleType<?> particleType = buffer.readRegistryIdUnsafe(ForgeRegistries.PARTICLE_TYPES);
		IParticleData particle = readParticle(buffer, particleType);

		Vector3d from = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		Vector3d to = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
		float spacing = buffer.readFloat();

		return new DrawParticleLineMessage(particle, from, to, spacing);
	}

	private static <T extends IParticleData> T readParticle(PacketBuffer buffer, ParticleType<T> particleType) {
		return particleType.getDeserializer().read(particleType, buffer);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ParticleLineSpawner.spawnParticleLine(particle, from, to, spacing);
		});
		ctx.get().setPacketHandled(true);
	}
}
