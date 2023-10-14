package com.lovetropics.minigames.common.core.map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.GameException;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class MapRegions {
	public static final Codec<MapRegions> CODEC = CompoundTag.CODEC.xmap(
			tag -> {
				MapRegions regions = new MapRegions();
				regions.read(tag);
				return regions;
			},
			regions -> regions.write(new CompoundTag())
	);

	private final Multimap<String, BlockBox> regions = HashMultimap.create();

	public void add(String key, BlockBox region) {
		regions.put(key, region);
	}

	public void addAll(MapRegions regions) {
		this.regions.putAll(regions.regions);
	}

	public Set<String> keySet() {
		return regions.keySet();
	}

	public Collection<BlockBox> get(String key) {
		return regions.get(key);
	}

	@Nullable
	public BlockBox getAny(String key) {
		Collection<BlockBox> regions = this.regions.get(key);
		if (!regions.isEmpty()) {
			return regions.iterator().next();
		} else {
			return null;
		}
	}

	public List<BlockBox> getAll(String... keys) {
		return Arrays.stream(keys).flatMap(key -> get(key).stream()).toList();
	}

	@Nonnull
	public BlockBox getOrThrow(String key) {
		BlockBox box = this.getAny(key);
		if (box == null) {
			throw new GameException(Component.literal("Missing expected region with key '" + key + "'"));
		}
		return box;
	}

	public CompoundTag write(CompoundTag root) {
		for (String key : regions.keySet()) {
			ListTag regionsList = new ListTag();
			for (BlockBox region : regions.get(key)) {
				regionsList.add(region.write(new CompoundTag()));
			}

			root.put(key, regionsList);
		}

		return root;
	}

	public void read(CompoundTag root) {
		this.regions.clear();

		for (String key : root.getAllKeys()) {
			ListTag regionsList = root.getList(key, Tag.TAG_COMPOUND);
			for (int i = 0; i < regionsList.size(); i++) {
				BlockBox region = BlockBox.read(regionsList.getCompound(i));
				this.regions.put(key, region);
			}
		}
	}

	public boolean isEmpty() {
		return regions.isEmpty();
	}
}
