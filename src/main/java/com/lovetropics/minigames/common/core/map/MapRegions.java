package com.lovetropics.minigames.common.core.map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;

public final class MapRegions {
	public static final Codec<MapRegions> CODEC = MoreCodecs.withNbtCompound(MapRegions::write, MapRegions::read, MapRegions::new);

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

	public CompoundNBT write(CompoundNBT root) {
		for (String key : regions.keySet()) {
			ListNBT regionsList = new ListNBT();
			for (BlockBox region : regions.get(key)) {
				regionsList.add(region.write(new CompoundNBT()));
			}

			root.put(key, regionsList);
		}

		return root;
	}

	public void read(CompoundNBT root) {
		this.regions.clear();

		for (String key : root.keySet()) {
			ListNBT regionsList = root.getList(key, Constants.NBT.TAG_COMPOUND);
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
