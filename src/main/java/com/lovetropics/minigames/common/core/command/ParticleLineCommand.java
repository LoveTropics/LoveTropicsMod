package com.lovetropics.minigames.common.core.command;

import com.lovetropics.minigames.client.particle_line.DrawParticleLineMessage;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.network.PacketDistributor;

import static net.minecraft.command.Commands.argument;
import staticnet.minecraft.commands.Commandss.literal;

public final class ParticleLineCommand {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		// @formatter:off
		dispatcher.register(
			literal("drawline").requires(source -> source.hasPermission(2))
				.then(argument("particle", ParticleArgument.particle())
				.then(argument("from", Vec3Argument.vec3())
				.then(argument("to", Vec3Argument.vec3())
				.then(argument("spacing", FloatArgumentType.floatArg(0.01F))
				.executes(ParticleLineCommand::spawnLine)
			))))
		);
		// @formatter:on
	}

	private static int spawnLine(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();

		ParticleOptions particle = ParticleArgument.getParticle(context, "particle");
		Vec3 from = Vec3Argument.getVec3(context, "from");
		Vec3 to = Vec3Argument.getVec3(context, "to");
		float spacing = FloatArgumentType.getFloat(context, "spacing");

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new DrawParticleLineMessage(particle, from, to, spacing));

		return Command.SINGLE_SUCCESS;
	}
}
