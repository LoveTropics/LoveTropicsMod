package com.lovetropics.minigames.common.map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.Set;

public final class MapRegionSet {
	private final Multimap<String, MapRegion> regions = HashMultimap.create();

	public void add(String key, MapRegion region) {
		regions.put(key, region);
	}

	public Set<String> keySet() {
		return regions.keySet();
	}

	public Collection<MapRegion> get(String key) {
		return regions.get(key);
	}

	public CompoundNBT write(CompoundNBT root) {
		for (String key : regions.keySet()) {
			ListNBT regionsList = new ListNBT();
			for (MapRegion region : regions.get(key)) {
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
				MapRegion region = MapRegion.read(regionsList.getCompound(i));
				this.regions.put(key, region);
			}
		}
	}

	public void write(PacketBuffer buffer) {
		buffer.writeVarInt(regions.keySet().size());

		for (String key : regions.keySet()) {
			buffer.writeString(key, 64);

			Collection<MapRegion> regions = this.regions.get(key);
			buffer.writeVarInt(regions.size());

			for (MapRegion region : regions) {
				region.write(buffer);
			}
		}
	}

	public void read(PacketBuffer buffer) {
		this.regions.clear();

		int keyCount = buffer.readVarInt();

		for (int i = 0; i < keyCount; i++) {
			String key = buffer.readString(64);

			int regionCount = buffer.readVarInt();
			for (int j = 0; j < regionCount; j++) {
				this.regions.put(key, MapRegion.read(buffer));
			}
		}
	}

	public boolean isEmpty() {
		return regions.isEmpty();
	}
}
