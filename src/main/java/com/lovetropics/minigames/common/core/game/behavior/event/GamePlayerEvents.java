package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.SpawnBuilder;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.UUID;

public final class GamePlayerEvents {
	public static final GameEventType<Add> ADD = GameEventType.create(Add.class, listeners -> (player) -> {
		for (Add listener : listeners) {
			listener.onAdd(player);
		}
	});

	public static final GameEventType<Remove> REMOVE = GameEventType.create(Remove.class, listeners -> (player) -> {
		for (Remove listener : listeners) {
			listener.onRemove(player);
		}
	});

	public static final GameEventType<Add> JOIN = GameEventType.create(Add.class, listeners -> (player) -> {
		for (Add listener : listeners) {
			listener.onAdd(player);
		}
	});

	public static final GameEventType<Remove> LEAVE = GameEventType.create(Remove.class, listeners -> (player) -> {
		for (Remove listener : listeners) {
			listener.onRemove(player);
		}
	});

	public static final GameEventType<SetRole> SET_ROLE = GameEventType.create(SetRole.class, listeners -> (player, role, lastRole) -> {
		for (SetRole listener : listeners) {
			listener.onSetRole(player, role, lastRole);
		}
	});

	public static final GameEventType<Spawn> SPAWN = GameEventType.create(Spawn.class, listeners -> (playerId, spawn, role) -> {
		for (Spawn listener : listeners) {
			listener.onSpawn(playerId, spawn, role);
		}
	});

	public static final GameEventType<Tick> TICK = GameEventType.create(Tick.class, listeners -> (player) -> {
		for (Tick listener : listeners) {
			listener.tick(player);
		}
	});

	public static final GameEventType<Damage> DAMAGE = GameEventType.create(Damage.class, listeners -> (player, damageSource, amount) -> {
		for (Damage listener : listeners) {
			InteractionResult result = listener.onDamage(player, damageSource, amount);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<DamageAmount> DAMAGE_AMOUNT = GameEventType.create(DamageAmount.class, listeners -> (player, damageSource, amount, originalAmount) -> {
		for (DamageAmount listener : listeners) {
			amount = listener.getDamageAmount(player, damageSource, amount, originalAmount);
		}
		return amount;
	});

	public static final GameEventType<Attack> ATTACK = GameEventType.create(Attack.class, listeners -> (player, target) -> {
		for (Attack listener : listeners) {
			InteractionResult result = listener.onAttack(player, target);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<InteractEntity> INTERACT_ENTITY = GameEventType.create(InteractEntity.class, listeners -> (player, target, hand) -> {
		for (InteractEntity listener : listeners) {
			InteractionResult result = listener.onInteractEntity(player, target, hand);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<UseItem> USE_ITEM = GameEventType.create(UseItem.class, listeners -> (player, hand) -> {
		for (UseItem listener : listeners) {
			InteractionResult result = listener.onUseItem(player, hand);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<UseBlock> USE_BLOCK = GameEventType.create(UseBlock.class, listeners -> (player, world, pos, hand, traceResult) -> {
		for (UseBlock listener : listeners) {
			InteractionResult result = listener.onUseBlock(player, world, pos, hand, traceResult);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<LeftClickBlock> LEFT_CLICK_BLOCK = GameEventType.create(LeftClickBlock.class, listeners -> (player, world, pos) -> {
		for (LeftClickBlock listener : listeners) {
			listener.onLeftClickBlock(player, world, pos);
		}
	});

	public static final GameEventType<BreakBlock> BREAK_BLOCK = GameEventType.create(BreakBlock.class, listeners -> (player, pos, state, hand) -> {
		for (BreakBlock listener : listeners) {
			InteractionResult result = listener.onBreakBlock(player, pos, state, hand);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<PlaceBlock> PLACE_BLOCK = GameEventType.create(PlaceBlock.class, listeners -> (player, pos, placed, placedOn) -> {
		for (PlaceBlock listener : listeners) {
			InteractionResult result = listener.onPlaceBlock(player, pos, placed, placedOn);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<ThrowItem> THROW_ITEM = GameEventType.create(ThrowItem.class, listeners -> (player, item) -> {
		for (ThrowItem listener : listeners) {
			InteractionResult result = listener.onThrowItem(player, item);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<PickUpItem> PICK_UP_ITEM = GameEventType.create(PickUpItem.class, listeners -> (player, item) -> {
		for (PickUpItem listener : listeners) {
			InteractionResult result = listener.onPickUpItem(player, item);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<Death> DEATH = GameEventType.create(Death.class, listeners -> (player, damageSource) -> {
		for (Death listener : listeners) {
			InteractionResult result = listener.onDeath(player, damageSource);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<Respawn> RESPAWN = GameEventType.create(Respawn.class, listeners -> player -> {
		for (Respawn listener : listeners) {
			listener.onRespawn(player);
		}
	});

	public static final GameEventType<AllocateRoles> ALLOCATE_ROLES = GameEventType.create(AllocateRoles.class, listeners -> allocator -> {
		for (AllocateRoles listener : listeners) {
			listener.onAllocateRoles(allocator);
		}
	});

	public static final GameEventType<Chat> CHAT = GameEventType.create(Chat.class, listeners -> (player, message) -> {
		for (Chat listener : listeners) {
			if (listener.onChat(player, message)) {
				return true;
			}
		}
		return false;
	});

	private GamePlayerEvents() {
	}

	public interface Add {
		void onAdd(ServerPlayer player);
	}

	public interface Remove {
		void onRemove(ServerPlayer player);
	}

	public interface SetRole {
		void onSetRole(ServerPlayer player, @Nullable PlayerRole role, @Nullable PlayerRole lastRole);
	}

	public interface Spawn {
		void onSpawn(UUID playerId, SpawnBuilder spawn, @Nullable PlayerRole role);
	}

	public interface Tick {
		void tick(ServerPlayer player);
	}

	public interface Damage {
		InteractionResult onDamage(ServerPlayer player, DamageSource damageSource, float amount);
	}

	public interface DamageAmount {
		float getDamageAmount(ServerPlayer player, DamageSource damageSource, float amount, float originalAmount);
	}

	public interface Attack {
		InteractionResult onAttack(ServerPlayer player, Entity target);
	}

	public interface InteractEntity {
		InteractionResult onInteractEntity(ServerPlayer player, Entity target, InteractionHand hand);
	}

	public interface UseItem {
		InteractionResult onUseItem(ServerPlayer player, InteractionHand hand);
	}

	public interface UseBlock {
		InteractionResult onUseBlock(ServerPlayer player, ServerLevel world, BlockPos pos, InteractionHand hand, BlockHitResult traceResult);
	}

	public interface LeftClickBlock {
		void onLeftClickBlock(ServerPlayer player, ServerLevel world, BlockPos pos);
	}

	public interface BreakBlock {
		InteractionResult onBreakBlock(ServerPlayer player, BlockPos pos, BlockState state, InteractionHand hand);
	}

	public interface PlaceBlock {
		InteractionResult onPlaceBlock(ServerPlayer player, BlockPos pos, BlockState placed, BlockState placedOn);
	}

	public interface ThrowItem {
		InteractionResult onThrowItem(ServerPlayer player, ItemEntity item);
	}

	public interface PickUpItem {
		InteractionResult onPickUpItem(ServerPlayer player, ItemEntity item);
	}

	public interface Death {
		InteractionResult onDeath(ServerPlayer player, DamageSource damageSource);
	}

	public interface Respawn {
		void onRespawn(ServerPlayer player);
	}

	public interface AllocateRoles {
		void onAllocateRoles(TeamAllocator<PlayerRole, ServerPlayer> allocator);
	}

	public interface Chat {
		boolean onChat(ServerPlayer player, PlayerChatMessage message);
	}
}
