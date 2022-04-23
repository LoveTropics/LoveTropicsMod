package com.lovetropics.minigames.client.particle_line;

import net.minecraft.client.Minecraft;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public final class ParticleLineSpawner {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public static void spawnParticleLine(IParticleData particle, Vector3d from, Vector3d to, float spacing) {
		Vector3d delta = to.subtract(from);
		float length = (float) delta.length();

		Vector3d direction = delta.scale(1.0 / length);

		int count = MathHelper.ceil(length / spacing) + 1;

		for (int i = 0; i < count; i++) {
			float progress = (float) i / count;
			float project = progress * length;
			Vector3d point = from.add(direction.scale(project));

			CLIENT.particleEngine.createParticle(particle, point.x, point.y, point.z, 0.0, 0.0, 0.0);
		}
	}
}
