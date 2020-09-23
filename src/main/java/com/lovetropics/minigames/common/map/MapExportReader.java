package com.lovetropics.minigames.common.map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class MapExportReader implements Closeable {
	private final ZipInputStream input;

	private MapExportReader(ZipInputStream input) {
		this.input = input;
	}

	public static MapExportReader open(InputStream input) {
		return new MapExportReader(new ZipInputStream(input));
	}

	public MapMetadata loadInto(MinecraftServer server, DimensionType dimension) throws IOException {
		ServerWorld overworld = server.getWorld(DimensionType.OVERWORLD);
		File worldDirectory = overworld.getSaveHandler().getWorldDirectory();
		File dimensionDirectory = dimension.getDirectory(worldDirectory);

		return loadInto(dimensionDirectory.toPath());
	}

	public MapMetadata loadInto(Path dimensionRoot) throws IOException {
		FileUtils.deleteDirectory(dimensionRoot.toFile());

		Path regionRoot = dimensionRoot.resolve("region");
		Files.createDirectories(regionRoot);

		MapMetadata metadata = null;

		ZipEntry entry;
		while ((entry = input.getNextEntry()) != null) {
			if (entry.isDirectory()) {
				continue;
			}

			String name = entry.getName();
			if (name.equals("metadata.nbt")) {
				metadata = MapMetadata.read(CompressedStreamTools.read(new DataInputStream(input)));
			} else if (name.startsWith("world/")) {
				Path targetPath = regionRoot.resolve(name.substring("world/".length()));
				Files.copy(input, targetPath);
			}
		}

		if (metadata == null) {
			throw new IOException("missing metadata");
		}

		return metadata;
	}

	@Override
	public void close() throws IOException {
		this.input.close();
	}
}
