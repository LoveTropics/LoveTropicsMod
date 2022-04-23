package com.lovetropics.minigames.common.core.map.workspace;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensions;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class WorkspacePositionTracker {
	private static final String NBT_KEY = Constants.MODID + ":map_workspace";
	private static final String NBT_RETURN_KEY = "return";

	public static void setPositionFor(ServerPlayer player, MapWorkspace workspace, Position position) {
		CompoundTag workspaceTag = getOrCreateWorkspaceTag(player, workspace);
		position.write(workspaceTag);
	}

	@Nullable
	public static Position getPositionFor(ServerPlayer player, MapWorkspace workspace) {
		CompoundTag workspaceTag = getOrCreateWorkspaceTag(player, workspace);
		return Position.read(workspaceTag);
	}

	public static void setReturnPositionFor(ServerPlayer player, Position position) {
		CompoundTag workspaceTag = getOrCreateReturnTag(player);
		position.write(workspaceTag);
	}

	@Nullable
	public static Position getReturnPositionFor(ServerPlayer player) {
		CompoundTag workspaceTag = getOrCreateReturnTag(player);
		return Position.read(workspaceTag);
	}

	private static CompoundTag getOrCreateWorkspaceTag(ServerPlayer player, MapWorkspace workspace) {
		CompoundTag data = getOrCreateTag(player);
		if (!data.contains(workspace.getId(), Tag.TAG_COMPOUND)) {
			data.put(workspace.getId(), new CompoundTag());
		}
		return data.getCompound(workspace.getId());
	}

	private static CompoundTag getOrCreateReturnTag(ServerPlayer player) {
		CompoundTag data = getOrCreateTag(player);
		if (!data.contains(NBT_RETURN_KEY, Tag.TAG_COMPOUND)) {
			data.put(NBT_RETURN_KEY, new CompoundTag());
		}
		return data.getCompound(NBT_RETURN_KEY);
	}

	private static CompoundTag getOrCreateTag(ServerPlayer player) {
		CompoundTag persistentData = player.getPersistentData();
		if (!persistentData.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
			persistentData.put(NBT_KEY, new CompoundTag());
		}
		return persistentData.getCompound(NBT_KEY);
	}

	@SubscribeEvent
	public static void onPlayerChangeDimension(EntityTravelToDimensionEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer)) {
			return;
		}

		ServerPlayer player = (ServerPlayer) event.getEntity();
		MinecraftServer server = player.server;

		ResourceKey<Level> from = player.level.dimension();

		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(server);
		MapWorkspace fromWorkspace = workspaceManager.getWorkspace(from);

		WorkspacePositionTracker.Position position = WorkspacePositionTracker.Position.copyFrom(player);
		if (fromWorkspace != null) {
			WorkspacePositionTracker.setPositionFor(player, fromWorkspace, position);
		} else {
			if (isValidReturnDimension(server, from)) {
				WorkspacePositionTracker.setReturnPositionFor(player, position);
			}
		}
	}

	private static boolean isValidReturnDimension(MinecraftServer server, ResourceKey<Level> dimension) {
		RuntimeDimensions dimensions = RuntimeDimensions.get(server);
		return !dimensions.isTemporaryDimension(dimension);
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		ServerPlayer fromPlayer = (ServerPlayer) event.getOriginal();
		ServerPlayer toPlayer = (ServerPlayer) event.getPlayer();

		CompoundTag fromData = fromPlayer.getPersistentData();
		if (fromData.contains(NBT_KEY, Tag.TAG_COMPOUND)) {
			CompoundTag fromTag = fromData.getCompound(NBT_KEY);
			toPlayer.getPersistentData().put(NBT_KEY, fromTag.copy());
		}
	}

	public record Position(ResourceKey<Level> dimension, Vec3 pos, float yaw, float pitch) {
		public static Position copyFrom(ServerPlayer entity) {
			return new Position(entity.level.dimension(), entity.position(), entity.getYRot(), entity.getXRot());
		}

		public void applyTo(ServerPlayer entity) {
			ServerLevel world = entity.getServer().getLevel(dimension);
			entity.teleportTo(world, pos.x, pos.y, pos.z, yaw, pitch);
		}

		public void write(CompoundTag nbt) {
			nbt.putString("dimension", dimension.location().toString());
			nbt.putDouble("x", pos.x);
			nbt.putDouble("y", pos.y);
			nbt.putDouble("z", pos.z);
			nbt.putFloat("yaw", yaw);
			nbt.putFloat("pitch", pitch);
		}

		@Nullable
		public static Position read(CompoundTag nbt) {
			if (!nbt.contains("dimension")) {
				return null;
			}

			ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("dimension")));
			Vec3 pos = new Vec3(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
			float yaw = nbt.getFloat("yaw");
			float pitch = nbt.getFloat("pitch");

			return new Position(dimension, pos, yaw, pitch);
		}
	}
}
