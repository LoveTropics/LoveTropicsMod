package com.lovetropics.minigames.client.map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.map.workspace.ClientWorkspaceRegions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class MapWorkspaceTracer {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final double TRACE_RANGE = 64.0;

	private static RegionEditOperator edit;

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		ClientPlayerEntity player = CLIENT.player;
		if (event.phase == TickEvent.Phase.START || player == null) {
			return;
		}

		if (edit != null) {
			edit.update(player);
		}
	}

	@Nullable
	public static RegionTraceTarget trace(PlayerEntity player) {
		ClientWorkspaceRegions regions = ClientMapWorkspace.INSTANCE.getRegions();
		if (regions.isEmpty()) {
			return null;
		}

		Vec3d origin = player.getEyePosition(1.0F);
		Vec3d target = origin.add(player.getLookVec().scale(TRACE_RANGE));

		AxisAlignedBB traceScope = new AxisAlignedBB(origin, target).grow(1.0);

		ClientWorkspaceRegions.Entry closestEntry = null;
		double closestDistance = Double.POSITIVE_INFINITY;
		Vec3d closestPoint = null;
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
					closestPoint = intersectPoint;
					closestDistance = distance;
					closestSide = traceResult.getFace();
				}
			}
		}

		if (closestEntry != null) {
			return new RegionTraceTarget(closestEntry, closestSide, closestPoint, closestDistance);
		} else {
			return null;
		}
	}

	public static boolean select(PlayerEntity player, @Nullable RegionTraceTarget target, Function<RegionTraceTarget, RegionEditOperator> operatorFactory) {
		if (edit != null) {
			if (edit.select(player, target)) {
				edit = null;
			}
			return true;
		} else if (target != null) {
			edit = operatorFactory.apply(target);
			return true;
		}
		return false;
	}

	public static void stopEditing() {
		edit = null;
	}

	public static Set<ClientWorkspaceRegions.Entry> getSelectedRegions() {
		if (edit != null) {
			return edit.getSelectedRegions();
		} else {
			return ImmutableSet.of();
		}
	}
}
