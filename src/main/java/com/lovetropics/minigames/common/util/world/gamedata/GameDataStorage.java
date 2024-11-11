package com.lovetropics.minigames.common.util.world.gamedata;

import com.lovetropics.minigames.LoveTropics;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameDataStorage extends SavedData {
	private static final String ID = LoveTropics.ID + "_gamedata";

	public record NamespacedData(Map<UUID, CompoundTag> playerData) {
		public static final Codec<NamespacedData> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.unboundedMap(UUIDUtil.STRING_CODEC, CompoundTag.CODEC).fieldOf("playerData").forGetter(NamespacedData::playerData)
		).apply(i, NamespacedData::new));

		public NamespacedData(Map<UUID, CompoundTag> playerData) {
			this.playerData = new HashMap<>(playerData);
		}
	}

	public static final Codec<GameDataStorage> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.unboundedMap(ResourceLocation.CODEC, NamespacedData.CODEC).fieldOf("playerData").forGetter(GameDataStorage::getPlayerData)
	).apply(i, GameDataStorage::new));

	protected final Map<ResourceLocation, NamespacedData> playerData;

	public Map<ResourceLocation, NamespacedData> getPlayerData() {
		return playerData;
	}

	public GameDataStorage(Map<ResourceLocation, NamespacedData> playerData) {
		this.playerData = new HashMap<>(playerData);
	}

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
		GameDataStorage.CODEC.encodeStart(ops, this)
				.result().ifPresent(result -> {
					tag.put("namespaces", result);
				});
		return tag;
	}

	public static GameDataStorage get(ServerLevel level) {
		return level.getDataStorage().computeIfAbsent(new Factory<>(
				GameDataStorage::create,
				GameDataStorage::load
		), ID);
	}

	public static GameDataStorage create() {
		return new GameDataStorage(new Object2ObjectOpenHashMap<>());
	}

	public static GameDataStorage load(CompoundTag tag, HolderLookup.Provider registries) {
		RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);
		DataResult<GameDataStorage> namespaces = GameDataStorage.CODEC.parse(ops, tag.get("namespaces"));
		return namespaces.result().orElse(create());
	}

	public CompoundTag get(ResourceLocation storageId, UUID playerId) {
		return getNamespacedData(storageId).playerData().computeIfAbsent(playerId, (k) -> new CompoundTag());
	}

	@NotNull
	private NamespacedData getNamespacedData(ResourceLocation storageId) {
		return playerData.computeIfAbsent(storageId, (k) -> new NamespacedData(new Object2ObjectOpenHashMap<>()));
	}

	public void set(ResourceLocation storageId, UUID playerId, CompoundTag tag) {
		getNamespacedData(storageId).playerData().put(playerId, tag);
		setDirty();
	}
}
