package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.weather.WeatherEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

import javax.annotation.Nullable;
import java.util.List;

public final class GameWorldEvents {
	public static final GameEventType<ChunkLoad> CHUNK_LOAD = GameEventType.create(ChunkLoad.class, listeners -> (chunk) -> {
		for (ChunkLoad listener : listeners) {
			listener.onChunkLoad(chunk);
		}
	});

	public static final GameEventType<ExplosionSound> EXPLOSION_SOUND = GameEventType.create(ExplosionSound.class, listeners -> (explosion, sound) -> {
		for (ExplosionSound listener : listeners) {
			sound = listener.updateExplosionSound(explosion, sound);
		}
		return sound;
	});

	public static final GameEventType<ExplosionDetonate> EXPLOSION_DETONATE = GameEventType.create(ExplosionDetonate.class, listeners -> (explosion, affectedBlocks, affectedEntities) -> {
		for (ExplosionDetonate listener : listeners) {
			listener.onExplosionDetonate(explosion, affectedBlocks, affectedEntities);
		}
	});

	public static final GameEventType<SaplingGrow> SAPLING_GROW = GameEventType.create(SaplingGrow.class, listeners -> (world, pos) -> {
		for (SaplingGrow listener : listeners) {
			InteractionResult result = listener.onSaplingGrow(world, pos);

			if (result != InteractionResult.PASS) {
				return result;
			}
		}

		return InteractionResult.PASS;
	});

	public static final GameEventType<SetWeather> SET_WEATHER = GameEventType.create(SetWeather.class, listeners -> (lastEvent, event) -> {
		for (SetWeather listener : listeners) {
			listener.onSetWeather(lastEvent, event);
		}
	});

	public static final GameEventType<BlockLanded> BLOCK_LANDED = GameEventType.create(BlockLanded.class, listeners -> (level, pos, state) -> {
		for (var listener : listeners) {
			listener.onBlockLanded(level, pos, state);
		}
	});

	public static final GameEventType<BlockDrops> BLOCK_DROPS = GameEventType.create(BlockDrops.class, listeners -> (player, pos, blockState, blockEntity, tool, drops) -> {
		for (BlockDrops listener : listeners) {
			listener.updateBlockDrops(player, pos, blockState, blockEntity, tool, drops);
		}
	});

	private GameWorldEvents() {
	}

	public interface ChunkLoad {
		void onChunkLoad(ChunkAccess chunk);
	}

	public interface ExplosionSound {
		Holder<SoundEvent> updateExplosionSound(Explosion explosion, Holder<SoundEvent> sound);
	}

	public interface ExplosionDetonate {
		void onExplosionDetonate(Explosion explosion, List<BlockPos> affectedBlocks, List<Entity> affectedEntities);
	}

	public interface SaplingGrow {
		InteractionResult onSaplingGrow(Level world, BlockPos pos);
	}

	public interface SetWeather {
		void onSetWeather(@Nullable WeatherEvent lastEvent, @Nullable WeatherEvent event);
	}

	public interface BlockLanded {
		void onBlockLanded(ServerLevel level, BlockPos pos, BlockState state);
	}

	public interface BlockDrops {
		void updateBlockDrops(ServerPlayer player, BlockPos pos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack tool, List<ItemEntity> drops);
	}
}
