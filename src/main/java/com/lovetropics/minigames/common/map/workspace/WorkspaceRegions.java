package com.lovetropics.minigames.common.map.workspace;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.network.LTNetwork;
import com.lovetropics.minigames.common.network.map.AddWorkspaceRegionMessage;
import com.lovetropics.minigames.common.network.map.UpdateWorkspaceRegionMessage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public final class WorkspaceRegions implements Iterable<WorkspaceRegions.Entry> {
	private final DimensionType dimension;
	private final Int2ObjectMap<Entry> entries = new Int2ObjectOpenHashMap<>();
	private int nextId;

	public WorkspaceRegions(DimensionType dimension) {
		this.dimension = dimension;
	}

	private int nextId() {
		return nextId++;
	}

	private <T> void sendMessage(T message) {
		LTNetwork.CHANNEL.send(PacketDistributor.DIMENSION.with(() -> dimension), message);
	}

	public void add(String key, MapRegion region) {
		add(nextId(), key, region);
	}

	public void add(int id, String key, MapRegion region) {
		add(new Entry(id, key, region));
	}

	void add(Entry entry) {
		entries.put(entry.id, entry);
		sendMessage(new AddWorkspaceRegionMessage(entry.id, entry.key, entry.region));
	}

	public void set(int id, @Nullable MapRegion region) {
		if (region != null) {
			WorkspaceRegions.Entry entry = entries.get(id);
			if (entry != null) {
				entry.region = region;
				sendMessage(new UpdateWorkspaceRegionMessage(id, region));
			}
		} else {
			entries.remove(id);
			sendMessage(new UpdateWorkspaceRegionMessage(id, null));
		}
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}

	public CompoundNBT write(CompoundNBT root) {
		Multimap<String, Entry> byKey = groupedByKey();

		for (String key : byKey.keySet()) {
			Collection<Entry> entries = byKey.get(key);

			ListNBT regionsList = new ListNBT();
			for (Entry entry : entries) {
				regionsList.add(entry.region.write(new CompoundNBT()));
			}

			root.put(key, regionsList);
		}

		return root;
	}

	public void read(CompoundNBT root) {
		this.entries.clear();

		for (String key : root.keySet()) {
			ListNBT regionsList = root.getList(key, Constants.NBT.TAG_COMPOUND);
			for (int i = 0; i < regionsList.size(); i++) {
				MapRegion region = MapRegion.read(regionsList.getCompound(i));
				this.add(key, region);
			}
		}
	}

	public void write(PacketBuffer buffer) {
		Multimap<String, Entry> byKey = groupedByKey();
		Set<String> keys = byKey.keySet();

		buffer.writeVarInt(keys.size());

		for (String key : keys) {
			buffer.writeString(key, 64);

			Collection<Entry> entries = byKey.get(key);
			buffer.writeVarInt(entries.size());

			for (Entry entry : entries) {
				buffer.writeVarInt(entry.id);
				entry.region.write(buffer);
			}
		}
	}

	private Multimap<String, Entry> groupedByKey() {
		Multimap<String, Entry> map = HashMultimap.create();
		for (Entry entry : entries.values()) {
			map.put(entry.key, entry);
		}

		return map;
	}

	@Override
	public Iterator<Entry> iterator() {
		return entries.values().iterator();
	}

	public static class Entry {
		public final int id;
		public final String key;
		public MapRegion region;

		Entry(int id, String key, MapRegion region) {
			this.id = id;
			this.key = key;
			this.region = region;
		}
	}
}
