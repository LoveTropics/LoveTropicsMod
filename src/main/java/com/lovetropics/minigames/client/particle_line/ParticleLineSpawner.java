package com.lovetropics.minigames.client.particle_line;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class ParticleLineSpawner {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void spawnParticleLine(ParticleOptions particle, Vec3 from, Vec3 to, float spacing) {
		Vec3 delta = to.subtract(from);
		float length = (float) delta.length();

		Vec3 direction = delta.scale(1.0 / length);

		int count = Mth.ceil(length / spacing) + 1;

		for (int i = 0; i < count; i++) {
			float progress = (float) i / count;
			float project = progress * length;
			Vec3 point = from.add(direction.scale(project));

			CLIENT.particleEngine.createParticle(particle, point.x, point.y, point.z, 0.0, 0.0, 0.0);
		}
	}
}
