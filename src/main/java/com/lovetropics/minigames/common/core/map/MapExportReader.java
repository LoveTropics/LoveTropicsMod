package com.lovetropics.minigames.common.core.map;

import net.minecraft.nbt.NbtIo;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class MapExportReader implements Closeable {
	private final ZipInputStream input;

	private MapExportReader(ZipInputStream input) {
		this.input = input;
	}

	public static MapExportReader open(MinecraftServer server, ResourceLocation location) throws IOException {
		ResourceLocation path = new ResourceLocation(location.getNamespace(), "maps/" + location.getPath() + ".zip");
		Resource resource = server.getDataPackRegistries().getResourceManager().getResource(path);
		return MapExportReader.open(resource.getInputStream());
	}

	public static MapExportReader open(InputStream input) {
		return new MapExportReader(new ZipInputStream(input));
	}

	public MapMetadata loadInto(MinecraftServer server, ResourceKey<Level> dimension) throws IOException {
		LevelStorageSource.LevelStorageAccess save = server.storageSource;
		File dimensionDirectory = save.getDimensionPath(dimension);
		return loadInto(dimensionDirectory.toPath());
	}

	public MapMetadata loadInto(Path dimensionRoot) throws IOException {
		if (Files.exists(dimensionRoot)) {
			FileUtils.deleteDirectory(dimensionRoot.toFile());
		}

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
				metadata = MapMetadata.read(NbtIo.read(new DataInputStream(input)));
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
