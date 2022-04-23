package com.lovetropics.minigames.common.content.trash_dive;

import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import static net.minecraft.command.Commands.argument;
import staticnet.minecraft.commands.Commandss.literal;

public class ScanAreaCommand {

	private static final RegistryObject<Block> PURIFIED_SAND = RegistryObject.of(new ResourceLocation("tropicraft", "purified_sand"), ForgeRegistries.BLOCKS);

	private static final SimpleCommandExceptionType NO_WATER = new SimpleCommandExceptionType(
			new TranslatableComponent("commands.ltminigames.scan.nowater.fail"));
	private static final SimpleCommandExceptionType TOO_FAR = new SimpleCommandExceptionType(
			new TranslatableComponent("commands.ltminigames.scan.toofar.fail"));
	private static final DynamicCommandExceptionType WRITE_ERROR = new DynamicCommandExceptionType(ex -> 
			new TranslatableComponent("commands.ltminigames.scan.write.fail", ex));

	public static void register(final CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(literal("game")
			.then(literal("scan").requires(s -> s.hasPermission(4))
				.then(argument("name", StringArgumentType.word())
					.executes(ctx -> scanArea(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))
				.executes(ctx -> scanArea(ctx.getSource(), "scan_result"))));
	}

	private static int scanArea(CommandSourceStack source, String fileName) throws CommandSyntaxException {
		BlockPos.MutableBlockPos pos = new BlockPos(source.getPosition()).mutable();

		ServerLevel world = source.getLevel();
		while (pos.getY() >= 0 && world.getBlockState(pos).getBlock() != Blocks.WATER) {
			pos.move(Direction.DOWN);
		}
		if (pos.getY() < 0) {
			throw NO_WATER.create();
		}

		LongSet seen = new LongOpenHashSet();
		LongSet found = new LongOpenHashSet();

		Deque<BlockPos> queue = new ArrayDeque<>(100);
		queue.add(pos.immutable());
		seen.add(pos.asLong());

		final Set<Block> edges = Sets.newHashSet(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.SAND, Blocks.BROWN_STAINED_GLASS);
		PURIFIED_SAND.ifPresent(edges::add);
		final Direction[] dirs = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

		Map<ChunkPos, LevelChunk> chunkCache = new HashMap<>();

		while (!queue.isEmpty()) {
			pos.set(queue.remove());
			found.add(pos.asLong());
			world.sendParticles(source.getPlayerOrException(), ParticleTypes.END_ROD, true, pos.getX() + 0.5, source.getPosition().y() - 3, pos.getZ(), 1, 0, 0, 0, 0);
			for (Direction dir : dirs) {
				pos.move(dir);
				if (seen.add(pos.asLong())) {
					if (pos.distSqr(source.getPosition(), true) > 400 * 400) {
						throw TOO_FAR.create();
					}
					LevelChunk chunk = chunkCache.computeIfAbsent(new ChunkPos(pos), p -> world.getChunk(p.x, p.z));
					if (!edges.contains(chunk.getBlockState(pos).getBlock())) {
						queue.add(pos.immutable());
					}
				}
				pos.move(dir.getOpposite());
			}
		}

		source.sendSuccess(new TextComponent("Found " + found.size() + " blocks"), true);

		Path output = Paths.get("export", "scan_results", fileName + ".bin");

		ByteBuffer buf = ByteBuffer.allocate(found.size() * 8);
		LongBuffer longs = buf.asLongBuffer();
		longs.put(found.toLongArray());
		try {
			if (Files.notExists(output.getParent())) {
				Files.createDirectories(output.getParent());
			}
			Files.write(output, buf.array(), StandardOpenOption.CREATE);
		} catch (IOException e) {
			e.printStackTrace();
			throw WRITE_ERROR.create(e);
		}

		source.sendSuccess(new TextComponent("Wrote " + buf.capacity() + " bytes to " + output), true);

		return Command.SINGLE_SUCCESS;
	}
}
