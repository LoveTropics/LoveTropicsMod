package com.lovetropics.minigames.common.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ParticleArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import static net.minecraft.command.Commands.argument;
import static net.minecraft.command.Commands.literal;

public final class ParticleLineCommand {
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("drawline").requires(source -> source.hasPermissionLevel(2))
				.then(argument("particle", ParticleArgument.particle())
				.then(argument("from", Vec3Argument.vec3())
				.then(argument("to", Vec3Argument.vec3())
				.then(argument("spacing", FloatArgumentType.floatArg(0.01F))
				.executes(ParticleLineCommand::spawnLine)
			))))
		);
		// @formatter:on
	}

	private static int spawnLine(CommandContext<CommandSource> context) throws CommandSyntaxException {
		CommandSource source = context.getSource();
		ServerWorld world = source.getWorld();
		ServerPlayerEntity player = source.asPlayer();

		IParticleData particle = ParticleArgument.getParticle(context, "particle");
		Vector3d from = Vec3Argument.getVec3(context, "from");
		Vector3d to = Vec3Argument.getVec3(context, "to");
		float spacing = FloatArgumentType.getFloat(context, "spacing");

		Vector3d delta = to.subtract(from);
		float length = (float) delta.length();

		Vector3d direction = delta.scale(1.0 / length);

		int count = MathHelper.ceil(length / spacing) + 1;

		for (int i = 0; i < count; i++) {
			float progress = (float) i / count;
			float project = progress * length;
			Vector3d point = from.add(direction.scale(project));

			world.spawnParticle(player, particle, true, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
		}

		return Command.SINGLE_SUCCESS;
	}
}
