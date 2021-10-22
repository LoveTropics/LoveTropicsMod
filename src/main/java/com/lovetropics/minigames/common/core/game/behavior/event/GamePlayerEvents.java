package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

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

	public static final GameEventType<Tick> TICK = GameEventType.create(Tick.class, listeners -> (player) -> {
		for (Tick listener : listeners) {
			listener.tick(player);
		}
	});

	public static final GameEventType<Damage> DAMAGE = GameEventType.create(Damage.class, listeners -> (player, damageSource, amount) -> {
		for (Damage listener : listeners) {
			ActionResultType result = listener.onDamage(player, damageSource, amount);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<Attack> ATTACK = GameEventType.create(Attack.class, listeners -> (player, target) -> {
		for (Attack listener : listeners) {
			ActionResultType result = listener.onAttack(player, target);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<InteractEntity> INTERACT_ENTITY = GameEventType.create(InteractEntity.class, listeners -> (player, target, hand) -> {
		for (InteractEntity listener : listeners) {
			listener.onInteractEntity(player, target, hand);
		}
	});

	public static final GameEventType<UseItem> USE_ITEM = GameEventType.create(UseItem.class, listeners -> (player, hand) -> {
		for (UseItem listener : listeners) {
			ActionResultType result = listener.onUseItem(player, hand);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<LeftClickBlock> LEFT_CLICK_BLOCK = GameEventType.create(LeftClickBlock.class, listeners -> (player, world, pos) -> {
		for (LeftClickBlock listener : listeners) {
			listener.onLeftClickBlock(player, world, pos);
		}
	});

	public static final GameEventType<BreakBlock> BREAK_BLOCK = GameEventType.create(BreakBlock.class, listeners -> (player, pos, state, hand) -> {
		for (BreakBlock listener : listeners) {
			ActionResultType result = listener.onBreakBlock(player, pos, state, hand);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<PlaceBlock> PLACE_BLOCK = GameEventType.create(PlaceBlock.class, listeners -> (player, pos, placed, placedOn) -> {
		for (PlaceBlock listener : listeners) {
			ActionResultType result = listener.onPlaceBlock(player, pos, placed, placedOn);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<Death> DEATH = GameEventType.create(Death.class, listeners -> (player, damageSource) -> {
		for (Death listener : listeners) {
			ActionResultType result = listener.onDeath(player, damageSource);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
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

	private GamePlayerEvents() {
	}

	public interface Add {
		void onAdd(ServerPlayerEntity player);
	}

	public interface Remove {
		void onRemove(ServerPlayerEntity player);
	}

	public interface SetRole {
		void onSetRole(ServerPlayerEntity player, PlayerRole role, @Nullable PlayerRole lastRole);
	}

	public interface Tick {
		void tick(ServerPlayerEntity player);
	}

	public interface Damage {
		ActionResultType onDamage(ServerPlayerEntity player, DamageSource damageSource, float amount);
	}

	public interface Attack {
		ActionResultType onAttack(ServerPlayerEntity player, Entity target);
	}

	public interface InteractEntity {
		void onInteractEntity(ServerPlayerEntity player, Entity target, Hand hand);
	}

	public interface UseItem {
		ActionResultType onUseItem(ServerPlayerEntity player, Hand hand);
	}

	public interface LeftClickBlock {
		void onLeftClickBlock(ServerPlayerEntity player, ServerWorld world, BlockPos pos);
	}

	public interface BreakBlock {
		ActionResultType onBreakBlock(ServerPlayerEntity player, BlockPos pos, BlockState state, Hand hand);
	}

	public interface PlaceBlock {
		ActionResultType onPlaceBlock(ServerPlayerEntity player, BlockPos pos, BlockState placed, BlockState placedOn);
	}

	public interface Death {
		ActionResultType onDeath(ServerPlayerEntity player, DamageSource damageSource);
	}

	public interface Respawn {
		void onRespawn(ServerPlayerEntity player);
	}

	public interface AllocateRoles {
		void onAllocateRoles(TeamAllocator<PlayerRole, ServerPlayerEntity> allocator);
	}
}
