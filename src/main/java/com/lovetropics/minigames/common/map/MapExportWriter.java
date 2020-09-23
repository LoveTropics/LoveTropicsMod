package com.lovetropics.minigames.common.map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ResourceLocation;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public final class MapExportWriter implements Closeable {
	private final FileSystem fs;

	private MapExportWriter(FileSystem fs) {
		this.fs = fs;
	}

	public static MapExportWriter open(Path path) throws IOException {
		Files.deleteIfExists(path);

		Map<String, String> env = new HashMap<>();
		env.put("create", "true");

		try {
			URI uri = new URI("jar:file", path.toUri().getPath(), null);
			return new MapExportWriter(FileSystems.newFileSystem(uri, env, null));
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	public static Path pathFor(ResourceLocation id) {
		return Paths.get("export", id.getNamespace(), "maps", id.getPath() + ".zip");
	}

	public void writeMetadata(MapMetadata metadata) throws IOException {
		CompoundNBT nbt = metadata.write(new CompoundNBT());

		Path path = fs.getPath("metadata.nbt");
		try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
			CompressedStreamTools.write(nbt, output);
		}
	}

	public void writeWorldData(Path sourceRoot) throws IOException {
		Path sourceRegionRoot = sourceRoot.resolve("region");
		Path targetWorldRoot = fs.getPath("world");

		Files.walkFileTree(sourceRegionRoot, new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path source, BasicFileAttributes attrs) throws IOException {
				Path target = targetWorldRoot.resolve(sourceRegionRoot.relativize(source).toString());
				Files.createDirectories(target.getParent());
				Files.copy(source, target);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@Override
	public void close() throws IOException {
		this.fs.close();
	}
}
