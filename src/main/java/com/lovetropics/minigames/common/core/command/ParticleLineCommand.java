package com.lovetropics.minigames.common.core.command;

import com.lovetropics.minigames.client.particle_line.DrawParticleLineMessage;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.PacketDistributor;

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
		ServerPlayerEntity player = source.asPlayer();

		IParticleData particle = ParticleArgument.getParticle(context, "particle");
		Vector3d from = Vec3Argument.getVec3(context, "from");
		Vector3d to = Vec3Argument.getVec3(context, "to");
		float spacing = FloatArgumentType.getFloat(context, "spacing");

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DrawParticleLineMessage(particle, from, to, spacing));

		return Command.SINGLE_SUCCESS;
	}
}
