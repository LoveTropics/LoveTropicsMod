package com.lovetropics.minigames.common.content.river_race;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RiverRaceState implements IGameState {
	public static final GameStateKey.Defaulted<RiverRaceState> KEY = GameStateKey.create("River Race", RiverRaceState::new);

	private final List<Zone> zones = new ArrayList<>();
	private Direction forwardDirection = Direction.NORTH;

	public void setForwardDirection(Direction forwardDirection) {
		this.forwardDirection = forwardDirection;
	}

	public void addZone(String id, BlockBox box, Component displayName, DyeColor color) {
		zones.add(new Zone(id, box, displayName, color));
	}

	public Zone getZoneById(String id) {
		for (Zone zone : zones) {
			if (zone.id.equals(id)) {
				return zone;
			}
		}
		throw new IllegalArgumentException("No zone with id: '" + id + "'");
	}

	@Nullable
	public Zone getZoneByPos(BlockPos pos) {
		for (Zone zone : zones) {
			if (zone.box.contains(pos)) {
				return zone;
			}
		}
		return null;
	}

	// Mirrored for the opposite team side so that equivalent trivia blocks can reuse the same question
	public ZoneLocalPos getZoneLocalPosKey(Zone zone, BlockPos pos) {
		pos = pos.subtract(zone.box.min());
		BlockPos size = zone.box.size();
		if (forwardDirection.getAxis() == Direction.Axis.X) {
			if (pos.getZ() >= size.getZ() / 2) {
				pos = new BlockPos(pos.getX(), pos.getY(), size.getZ() - 1 - pos.getZ());
			}
		} else {
			if (pos.getX() >= size.getX() / 2) {
				pos = new BlockPos(size.getX() - 1 - pos.getX(), pos.getY(), pos.getZ());
			}
		}
		return new ZoneLocalPos(zone, pos.asLong());
	}

	@Nullable
	public Zone getZoneWithCollectable(ItemStack itemStack) {
		for (Zone zone : zones) {
			ItemStack collectable = zone.collectable;
			if (collectable != null && ItemStack.isSameItemSameComponents(collectable, itemStack)) {
				return zone;
			}
		}
		return null;
	}

	public static final class Zone {
		private final String id;
		private final BlockBox box;
		private final Component displayName;
		private final DyeColor color;

		@Nullable
		private ItemStack collectable;

		public Zone(String id, BlockBox box, Component displayName, DyeColor color) {
			this.id = id;
			this.box = box;
			this.displayName = displayName;
			this.color = color;
		}

		public String id() {
			return id;
		}

		public BlockBox box() {
			return box;
		}

		public Component displayName() {
			return displayName;
		}

		public DyeColor color() {
			return color;
		}

		@Nullable
		public ItemStack collectable() {
			return collectable;
		}

		public void setCollectable(ItemStack collectable) {
			this.collectable = collectable;
		}
	}

	public record ZoneLocalPos(
			Zone zone,
			long pos
	) {
	}
}
