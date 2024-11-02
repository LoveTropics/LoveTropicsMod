package com.lovetropics.minigames.common.content.survive_the_tide.block;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
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
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

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

	private final Queue<ItemStack> dropQueue = new ArrayDeque<>();

	private int nearbyPlayerCount = -1;

	public LootDispenserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, LootDispenserBlockEntity entity) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		entity.tickDropQueue(level);

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
			if (player.isSpectator()) {
				continue;
			}
			if (!pos.closerToCenterThan(player.position(), loot.playerRange())) {
				continue;
			}
			if (++count >= loot.maxPlayerCount()) {
				break;
			}
		}
		return count;
	}

	private void tickDropQueue(Level level) {
		ItemStack itemToDrop = dropQueue.poll();
		if (itemToDrop != null) {
			Vec3 dispensePos = getDispensePos(getBlockPos(), getBlockState());
			level.addFreshEntity(new ItemEntity(level, dispensePos.x, dispensePos.y, dispensePos.z, itemToDrop));
		}
	}

	private void dispenseDrops(ServerLevel level, BlockPos pos, BlockState state, LootConfig loot, int count) {
		ResourceKey<LootTable> tableId = dropsLeft > 1 ? loot.lootTable() : loot.junkTable().orElse(loot.lootTable());
		LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(tableId);
		LootParams params = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);

		for (int i = 0; i < count; i++) {
			dropQueue.addAll(lootTable.getRandomItems(params));
		}

		ticksToNextDrop = loot.dropInterval.sample(level.random);
		if (--dropsLeft == 0) {
			addFailureEffects(level, getDispensePos(pos, state));
			level.setBlock(pos, state.setValue(LootDispenserBlock.STATE, LootDispenserBlock.State.CLOGGED), Block.UPDATE_ALL);
		}
	}

	private static void addFailureEffects(ServerLevel level, Vec3 dispensePos) {
		for (ServerPlayer player : level.players()) {
			if (player.position().closerThan(dispensePos, 64.0)) {
				player.connection.send(new ClientboundExplodePacket(
						dispensePos.x, dispensePos.y, dispensePos.z,
						1.0f,
						List.of(),
						Vec3.ZERO,
						Explosion.BlockInteraction.KEEP,
						ParticleTypes.EXPLOSION,
						ParticleTypes.EXPLOSION_EMITTER,
						SoundEvents.GENERIC_EXPLODE
				));
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
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		if (loot != null) {
			tag.put(TAG_LOOT, LootConfig.CODEC.encodeStart(NbtOps.INSTANCE, loot).getOrThrow());
		}
		tag.putInt(TAG_DROPS_LEFT, dropsLeft);
		tag.putInt(TAG_TICKS_TO_NEXT_DROP, ticksToNextDrop);
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		if (tag.contains(TAG_LOOT)) {
			loot = LootConfig.CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_LOOT)).resultOrPartial(LOGGER::error).orElse(null);
		} else {
			loot = null;
		}
		dropsLeft = tag.getInt(TAG_DROPS_LEFT);
		ticksToNextDrop = tag.getInt(TAG_TICKS_TO_NEXT_DROP);
	}

	private record LootConfig(ResourceKey<LootTable> lootTable, Optional<ResourceKey<LootTable>> junkTable, IntProvider dropInterval, int playerRange, int maxPlayerCount) {
		public static final Codec<LootConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
				ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("table").forGetter(LootConfig::lootTable),
				ResourceKey.codec(Registries.LOOT_TABLE).optionalFieldOf("junk_table").forGetter(LootConfig::junkTable),
				IntProvider.CODEC.fieldOf("drop_interval").forGetter(LootConfig::dropInterval),
				Codec.INT.fieldOf("player_range").orElse(5).forGetter(LootConfig::playerRange),
				Codec.INT.fieldOf("max_player_count").orElse(1).forGetter(LootConfig::maxPlayerCount)
		).apply(i, LootConfig::new));
	}
}
