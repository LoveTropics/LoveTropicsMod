package com.lovetropics.minigames.common.core.map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.resources.IResource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.storage.SaveFormat;
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
		IResource resource = server.getDataPackRegistries().getResourceManager().getResource(path);
		return MapExportReader.open(resource.getInputStream());
	}

	public static MapExportReader open(InputStream input) {
		return new MapExportReader(new ZipInputStream(input));
	}

	public MapMetadata loadInto(MinecraftServer server, RegistryKey<World> dimension) throws IOException {
		SaveFormat.LevelSave save = server.anvilConverterForAnvilFile;
		File dimensionDirectory = save.getDimensionFolder(dimension);
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
