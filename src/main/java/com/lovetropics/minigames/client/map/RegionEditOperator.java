package com.lovetropics.minigames.client.map;

import com.google.common.collect.ImmutableSet;
import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.map.workspace.ClientWorkspaceRegions;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.workspace.UpdateWorkspaceRegionMessage;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public interface RegionEditOperator {
	void update(Player player);

	boolean select(Player player, @Nullable RegionTraceTarget target);

	default Set<ClientWorkspaceRegions.Entry> getSelectedRegions() {
		return ImmutableSet.of();
	}

	abstract class EditOne implements RegionEditOperator {
		final RegionTraceTarget target;
		final Set<ClientWorkspaceRegions.Entry> selected;

		protected EditOne(RegionTraceTarget target) {
			this.target = target;
			this.selected = Collections.singleton(target.entry);
		}

		@Override
		public void update(Player player) {
			target.entry.region = updateEditing(player, target);
		}

		@Override
		public boolean select(Player player, @Nullable RegionTraceTarget target) {
			LoveTropicsNetwork.CHANNEL.sendToServer(new UpdateWorkspaceRegionMessage(this.target.entry.id, this.target.entry.region));
			return true;
		}

		@Override
		public Set<ClientWorkspaceRegions.Entry> getSelectedRegions() {
			return selected;
		}

		protected abstract BlockBox updateEditing(Player player, RegionTraceTarget editTarget);
	}

	final class Resize extends EditOne {
		public Resize(RegionTraceTarget target) {
			super(target);
		}

		@Override
		protected BlockBox updateEditing(Player player, RegionTraceTarget editTarget) {
			Vec3 origin = player.getEyePosition(1.0F);

			// TODO: not totally sure how to make this feel natural
			Vec3 grabPoint = origin.add(player.getLookAngle().scale(target.distanceToSide));
			BlockPos grabPos = new BlockPos(grabPoint);

			BlockBox region = editTarget.entry.region;

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
	}

	final class Move extends EditOne {
		private final Vec3 offset;

		public Move(RegionTraceTarget target) {
			super(target);
			this.offset = target.intersectPoint.subtract(target.entry.region.getCenter());
		}

		@Override
		protected BlockBox updateEditing(Player player, RegionTraceTarget editTarget) {
			Vec3 origin = player.getEyePosition(1.0F);

			BlockBox region = editTarget.entry.region;

			Vec3 grabPoint = region.getCenter().add(offset);
			Vec3 targetPoint = origin.add(player.getLookAngle().scale(target.distanceToSide));
			Vec3 offset = targetPoint.subtract(grabPoint);

			return region.offset(offset.x, offset.y, offset.z);
		}
	}

	final class Select implements RegionEditOperator {
		private final Set<ClientWorkspaceRegions.Entry> selected = new ObjectOpenHashSet<>();

		public Select(RegionTraceTarget target) {
			selected.add(target.entry);
		}

		@Override
		public void update(Player player) {
		}

		@Override
		public boolean select(Player player, @Nullable RegionTraceTarget target) {
			if (target != null) {
				selected.add(target.entry);
			}
			return false;
		}

		@Override
		public Set<ClientWorkspaceRegions.Entry> getSelectedRegions() {
			return selected;
		}
	}
}
