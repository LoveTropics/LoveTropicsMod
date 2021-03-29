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
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
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
import static net.minecraft.command.Commands.literal;

public class ScanAreaCommand {

	private static final RegistryObject<Block> PURIFIED_SAND = RegistryObject.of(new ResourceLocation("tropicraft", "purified_sand"), ForgeRegistries.BLOCKS);

	private static final SimpleCommandExceptionType NO_WATER = new SimpleCommandExceptionType(
			new TranslationTextComponent("commands.ltminigames.scan.nowater.fail"));
	private static final SimpleCommandExceptionType TOO_FAR = new SimpleCommandExceptionType(
			new TranslationTextComponent("commands.ltminigames.scan.toofar.fail"));
	private static final DynamicCommandExceptionType WRITE_ERROR = new DynamicCommandExceptionType(ex -> 
			new TranslationTextComponent("commands.ltminigames.scan.write.fail", ex));

	public static void register(final CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(literal("game")
			.then(literal("scan").requires(s -> s.hasPermissionLevel(4))
				.then(argument("name", StringArgumentType.word())
					.executes(ctx -> scanArea(ctx.getSource(), StringArgumentType.getString(ctx, "name"))))
				.executes(ctx -> scanArea(ctx.getSource(), "scan_result"))));
	}

	private static int scanArea(CommandSource source, String fileName) throws CommandSyntaxException {
		BlockPos.Mutable pos = new BlockPos(source.getPos()).toMutable();

		ServerWorld world = source.getWorld();
		while (pos.getY() >= 0 && world.getBlockState(pos).getBlock() != Blocks.WATER) {
			pos.move(Direction.DOWN);
		}
		if (pos.getY() < 0) {
			throw NO_WATER.create();
		}

		LongSet seen = new LongOpenHashSet();
		LongSet found = new LongOpenHashSet();

		Deque<BlockPos> queue = new ArrayDeque<>(100);
		queue.add(pos.toImmutable());
		seen.add(pos.toLong());

		final Set<Block> edges = Sets.newHashSet(Blocks.DIRT, Blocks.GRASS_BLOCK, Blocks.STONE, Blocks.SAND, Blocks.BROWN_STAINED_GLASS);
		PURIFIED_SAND.ifPresent(edges::add);
		final Direction[] dirs = new Direction[] { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

		Map<ChunkPos, Chunk> chunkCache = new HashMap<>();

		while (!queue.isEmpty()) {
			pos.setPos(queue.remove());
			found.add(pos.toLong());
			world.spawnParticle(source.asPlayer(), ParticleTypes.END_ROD, true, pos.getX() + 0.5, source.getPos().getY() - 3, pos.getZ(), 1, 0, 0, 0, 0);
			for (Direction dir : dirs) {
				pos.move(dir);
				if (seen.add(pos.toLong())) {
					if (pos.distanceSq(source.getPos(), true) > 400 * 400) {
						throw TOO_FAR.create();
					}
					Chunk chunk = chunkCache.computeIfAbsent(new ChunkPos(pos), p -> world.getChunk(p.x, p.z));
					if (!edges.contains(chunk.getBlockState(pos).getBlock())) {
						queue.add(pos.toImmutable());
					}
				}
				pos.move(dir.getOpposite());
			}
		}

		source.sendFeedback(new StringTextComponent("Found " + found.size() + " blocks"), true);

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

		source.sendFeedback(new StringTextComponent("Wrote " + buf.capacity() + " bytes to " + output), true);

		return Command.SINGLE_SUCCESS;
	}
}
