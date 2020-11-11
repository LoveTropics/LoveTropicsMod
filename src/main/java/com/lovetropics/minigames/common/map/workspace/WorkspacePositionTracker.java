package com.lovetropics.minigames.common.map.workspace;

import com.lovetropics.minigames.Constants;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class WorkspacePositionTracker {
	private static final String NBT_KEY = Constants.MODID + ":map_workspace";
	private static final String NBT_RETURN_KEY = "return";

	public static void setPositionFor(ServerPlayerEntity player, MapWorkspace workspace, Position position) {
		CompoundNBT workspaceTag = getOrCreateWorkspaceTag(player, workspace);
		position.write(workspaceTag);
	}

	@Nullable
	public static Position getPositionFor(ServerPlayerEntity player, MapWorkspace workspace) {
		CompoundNBT workspaceTag = getOrCreateWorkspaceTag(player, workspace);
		return Position.read(workspaceTag);
	}

	public static void setReturnPositionFor(ServerPlayerEntity player, Position position) {
		CompoundNBT workspaceTag = getOrCreateReturnTag(player);
		position.write(workspaceTag);
	}

	@Nullable
	public static Position getReturnPositionFor(ServerPlayerEntity player) {
		CompoundNBT workspaceTag = getOrCreateReturnTag(player);
		return Position.read(workspaceTag);
	}

	private static CompoundNBT getOrCreateWorkspaceTag(ServerPlayerEntity player, MapWorkspace workspace) {
		CompoundNBT data = getOrCreateTag(player);
		if (!data.contains(workspace.getId(), NBT.TAG_COMPOUND)) {
			data.put(workspace.getId(), new CompoundNBT());
		}
		return data.getCompound(workspace.getId());
	}

	private static CompoundNBT getOrCreateReturnTag(ServerPlayerEntity player) {
		CompoundNBT data = getOrCreateTag(player);
		if (!data.contains(NBT_RETURN_KEY, NBT.TAG_COMPOUND)) {
			data.put(NBT_RETURN_KEY, new CompoundNBT());
		}
		return data.getCompound(NBT_RETURN_KEY);
	}

	private static CompoundNBT getOrCreateTag(ServerPlayerEntity player) {
		CompoundNBT persistentData = player.getPersistentData();
		if (!persistentData.contains(NBT_KEY, NBT.TAG_COMPOUND)) {
			persistentData.put(NBT_KEY, new CompoundNBT());
		}
		return persistentData.getCompound(NBT_KEY);
	}

	@SubscribeEvent
	public static void onPlayerChangeDimension(EntityTravelToDimensionEvent event) {
		if (!(event.getEntity() instanceof ServerPlayerEntity)) {
			return;
		}

		ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
		MinecraftServer server = player.server;

		DimensionType from = player.dimension;

		MapWorkspaceManager workspaceManager = MapWorkspaceManager.get(server);
		MapWorkspace fromWorkspace = workspaceManager.getWorkspace(from);

		WorkspacePositionTracker.Position position = WorkspacePositionTracker.Position.copyFrom(player);
		if (fromWorkspace != null) {
			WorkspacePositionTracker.setPositionFor(player, fromWorkspace, position);
		} else {
			WorkspacePositionTracker.setReturnPositionFor(player, position);
		}
	}

	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		ServerPlayerEntity fromPlayer = (ServerPlayerEntity) event.getOriginal();
		ServerPlayerEntity toPlayer = (ServerPlayerEntity) event.getPlayer();

		CompoundNBT fromData = fromPlayer.getPersistentData();
		if (fromData.contains(NBT_KEY, NBT.TAG_COMPOUND)) {
			CompoundNBT fromTag = fromData.getCompound(NBT_KEY);
			toPlayer.getPersistentData().put(NBT_KEY, fromTag.copy());
		}
	}

	public static class Position {
		public final DimensionType dimension;
		public final Vec3d pos;
		public final float yaw;
		public final float pitch;

		Position(DimensionType dimension, Vec3d pos, float yaw, float pitch) {
			this.dimension = dimension;
			this.pos = pos;
			this.yaw = yaw;
			this.pitch = pitch;
		}

		public static Position copyFrom(ServerPlayerEntity entity) {
			return new Position(entity.dimension, entity.getPositionVec(), entity.rotationYaw, entity.rotationPitch);
		}

		public void applyTo(ServerPlayerEntity entity) {
			ServerWorld world = entity.getServer().getWorld(dimension);
			entity.teleport(world, pos.x, pos.y, pos.z, yaw, pitch);
		}

		public void write(CompoundNBT nbt) {
			nbt.putString("dimension", dimension.getRegistryName().toString());
			nbt.putDouble("x", pos.x);
			nbt.putDouble("y", pos.y);
			nbt.putDouble("z", pos.z);
			nbt.putFloat("yaw", yaw);
			nbt.putFloat("pitch", pitch);
		}

		@Nullable
		public static Position read(CompoundNBT nbt) {
			if (!nbt.contains("dimension")) {
				return null;
			}

			DimensionType dimension = DimensionType.byName(new ResourceLocation(nbt.getString("dimension")));
			Vec3d pos = new Vec3d(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
			float yaw = nbt.getFloat("yaw");
			float pitch = nbt.getFloat("pitch");

			return new Position(dimension, pos, yaw, pitch);
		}
	}
}
