package com.lovetropics.minigames.common.content.survive_the_tide.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class LootDispenserBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final int PLAYER_CHECK_INTERVAL = SharedConstants.TICKS_PER_SECOND;

	private static final double DISPENSE_DISTANCE = 1.0;
	private static final String TAG_LOOT = "loot";
	private static final String TAG_DROPS_LEFT = "drops_left";
	private static final String TAG_TICKS_TO_NEXT_DROP = "ticks_to_next_drop";

	@Nullable
	private LootConfig loot;
	private int dropsLeft;
	private int ticksToNextDrop;

	private int nearbyPlayerCount = -1;

	public LootDispenserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, LootDispenserBlockEntity entity) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		LootConfig loot = entity.loot;
		if (loot == null || entity.dropsLeft == 0 || state.getValue(LootDispenserBlock.STATE) != LootDispenserBlock.State.ACTIVE) {
			return;
		}

		if (entity.nearbyPlayerCount == -1 || serverLevel.getGameTime() % PLAYER_CHECK_INTERVAL == 0) {
			entity.nearbyPlayerCount = getPlayerCountAround(serverLevel, pos, loot);
		}

		if (entity.ticksToNextDrop > 0) {
			entity.ticksToNextDrop--;
			entity.setChanged();
		}

		if (entity.nearbyPlayerCount > 0 && entity.ticksToNextDrop == 0) {
			entity.dispenseDrops(serverLevel, pos, state, loot, entity.nearbyPlayerCount);
			entity.setChanged();
		}
	}

	private static int getPlayerCountAround(ServerLevel level, BlockPos pos, LootConfig loot) {
		int count = 0;
		for (ServerPlayer player : level.players()) {
			if (!pos.closerToCenterThan(player.position(), loot.playerRange())) {
				continue;
			}
			if (++count >= loot.maxPlayerCount()) {
				break;
			}
		}
		return count;
	}

	private void dispenseDrops(ServerLevel level, BlockPos pos, BlockState state, LootConfig loot, int count) {
		ResourceLocation tableId = dropsLeft > 1 ? loot.lootTable() : loot.junkTable().orElse(loot.lootTable());
		LootTable lootTable = level.getServer().getLootData().getLootTable(tableId);
		LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);

		Vec3 dispensePos = getDispensePos(pos, state);
		for (int i = 0; i < count; i++) {
			for (ItemStack item : lootTable.getRandomItems(params)) {
				ItemEntity itemEntity = new ItemEntity(level, dispensePos.x, dispensePos.y, dispensePos.z, item);
				level.addFreshEntity(itemEntity);
			}
		}

		ticksToNextDrop = loot.dropInterval.sample(level.random);
		if (--dropsLeft == 0) {
			addFailureEffects(level, dispensePos);
			level.setBlock(pos, state.setValue(LootDispenserBlock.STATE, LootDispenserBlock.State.CLOGGED), Block.UPDATE_ALL);
		}
	}

	private static void addFailureEffects(ServerLevel level, Vec3 dispensePos) {
		for (ServerPlayer player : level.players()) {
			if (player.position().closerThan(dispensePos, 64.0)) {
				player.connection.send(new ClientboundExplodePacket(dispensePos.x, dispensePos.y, dispensePos.z, 1.0f, List.of(), Vec3.ZERO));
			}
		}
	}

	private static Vec3 getDispensePos(BlockPos pos, BlockState state) {
		Direction facing = state.getValue(LootDispenserBlock.FACING);
		return Vec3.atCenterOf(pos).add(
				facing.getStepX() * DISPENSE_DISTANCE,
				facing.getStepY() * DISPENSE_DISTANCE,
				facing.getStepZ() * DISPENSE_DISTANCE
		);
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		if (loot != null) {
			tag.put(TAG_LOOT, Util.getOrThrow(LootConfig.CODEC.encodeStart(NbtOps.INSTANCE, loot), IllegalStateException::new));
		}
		tag.putInt(TAG_DROPS_LEFT, dropsLeft);
		tag.putInt(TAG_TICKS_TO_NEXT_DROP, ticksToNextDrop);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		if (tag.contains(TAG_LOOT)) {
			loot = LootConfig.CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_LOOT)).resultOrPartial(LOGGER::error).orElse(null);
		} else {
			loot = null;
		}
		dropsLeft = tag.getInt(TAG_DROPS_LEFT);
		ticksToNextDrop = tag.getInt(TAG_TICKS_TO_NEXT_DROP);
	}

	private record LootConfig(ResourceLocation lootTable, Optional<ResourceLocation> junkTable, IntProvider dropInterval, int playerRange, int maxPlayerCount) {
		public static final Codec<LootConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				ResourceLocation.CODEC.fieldOf("table").forGetter(LootConfig::lootTable),
				ResourceLocation.CODEC.optionalFieldOf("junk_table").forGetter(LootConfig::junkTable),
				IntProvider.CODEC.fieldOf("drop_interval").forGetter(LootConfig::dropInterval),
				Codec.INT.fieldOf("player_range").orElse(5).forGetter(LootConfig::playerRange),
				Codec.INT.fieldOf("max_player_count").orElse(1).forGetter(LootConfig::maxPlayerCount)
		).apply(i, LootConfig::new));
	}
}
