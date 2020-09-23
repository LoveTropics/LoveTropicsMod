package com.lovetropics.minigames.client.map;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.map.workspace.ClientWorkspaceRegions;
import com.lovetropics.minigames.common.network.LTNetwork;
import com.lovetropics.minigames.common.network.map.UpdateWorkspaceRegionMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class MapWorkspaceTracer {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final double TRACE_RANGE = 64.0;

	public static RegionTraceTarget traceTarget;
	private static boolean editing;
	private static double editingDistance;

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		ClientPlayerEntity player = CLIENT.player;
		if (event.phase == TickEvent.Phase.START || player == null) {
			return;
		}

		if (editing && traceTarget != null) {
			MapRegion editedRegion = updateEditing(player, traceTarget);
			ClientMapWorkspace.INSTANCE.updateRegion(traceTarget.entry.id, editedRegion);
		} else {
			traceTarget = getTraceTarget(player);
		}
	}

	private static MapRegion updateEditing(ClientPlayerEntity player, RegionTraceTarget editTarget) {
		Vec3d origin = player.getEyePosition(1.0F);

		// TODO: not totally sure how to make this feel natural
		Vec3d grabPoint = origin.add(player.getLookVec().scale(editingDistance));
		BlockPos grabPos = new BlockPos(grabPoint);

		MapRegion region = editTarget.entry.region;

		switch (editTarget.side) {
			case DOWN: return region.withMin(new BlockPos(region.min.getX(), grabPos.getY(), region.min.getZ()));
			case UP: return region.withMax(new BlockPos(region.max.getX(), grabPos.getY(), region.max.getZ()));
			case NORTH: return region.withMin(new BlockPos(region.min.getX(), region.min.getY(), grabPos.getZ()));
			case SOUTH: return region.withMax(new BlockPos(region.max.getX(), region.max.getY(), grabPos.getZ()));
			case WEST: return region.withMin(new BlockPos(grabPos.getX(), region.min.getY(), region.min.getZ()));
			case EAST: return region.withMax(new BlockPos(grabPos.getX(), region.max.getY(), region.max.getZ()));
			default: throw new UnsupportedOperationException();
		}
	}

	private static RegionTraceTarget getTraceTarget(ClientPlayerEntity player) {
		ClientWorkspaceRegions regions = ClientMapWorkspace.INSTANCE.getRegions();
		if (regions.isEmpty()) {
			return null;
		}

		Vec3d origin = player.getEyePosition(1.0F);
		Vec3d target = origin.add(player.getLookVec().scale(TRACE_RANGE));

		AxisAlignedBB traceScope = new AxisAlignedBB(origin, target).grow(1.0);

		ClientWorkspaceRegions.Entry closestEntry = null;
		double closestDistance = Double.POSITIVE_INFINITY;
		Direction closestSide = null;

		for (ClientWorkspaceRegions.Entry entry : regions) {
			if (!entry.region.intersects(traceScope)) {
				continue;
			}

			AxisAlignedBB bounds = entry.region.toAabb();
			BlockRayTraceResult traceResult = AxisAlignedBB.rayTrace(ImmutableList.of(bounds), origin, target, BlockPos.ZERO);
			if (traceResult != null) {
				Vec3d intersectPoint = traceResult.getHitVec();
				double distance = intersectPoint.distanceTo(origin);
				if (distance < closestDistance) {
					closestEntry = entry;
					closestDistance = distance;
					closestSide = traceResult.getFace();
				}
			}
		}

		if (closestEntry != null) {
			return new RegionTraceTarget(closestEntry, closestSide, closestDistance);
		} else {
			return null;
		}
	}

	public static boolean tryStartEditing() {
		if (!editing && traceTarget != null) {
			editing = true;
			editingDistance = traceTarget.distance;
			return true;
		}
		return false;
	}

	public static void stopEditing() {
		if (editing) {
			editing = false;
			if (traceTarget != null) {
				LTNetwork.CHANNEL.sendToServer(new UpdateWorkspaceRegionMessage(traceTarget.entry.id, traceTarget.entry.region));
			}
		}
	}

	public static boolean isEditing() {
		return editing;
	}
}
