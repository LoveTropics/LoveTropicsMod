package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public final class GamePlayerEvents {
	public static final GameEventType<Join> JOIN = GameEventType.create(Join.class, listeners -> (game, player, role) -> {
		for (Join listener : listeners) {
			listener.onJoin(game, player, role);
		}
	});

	public static final GameEventType<Leave> LEAVE = GameEventType.create(Leave.class, listeners -> (game, player) -> {
		for (Leave listener : listeners) {
			listener.onLeave(game, player);
		}
	});

	public static final GameEventType<ChangeRole> CHANGE_ROLE = GameEventType.create(ChangeRole.class, listeners -> (game, player, role, lastRole) -> {
		for (ChangeRole listener : listeners) {
			listener.onChangeRole(game, player, role, lastRole);
		}
	});

	public static final GameEventType<Tick> TICK = GameEventType.create(Tick.class, listeners -> (game, player) -> {
		for (Tick listener : listeners) {
			listener.tick(game, player);
		}
	});

	public static final GameEventType<Damage> DAMAGE = GameEventType.create(Damage.class, listeners -> (game, player, damageSource, amount) -> {
		for (Damage listener : listeners) {
			ActionResultType result = listener.onDamage(game, player, damageSource, amount);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<Attack> ATTACK = GameEventType.create(Attack.class, listeners -> (game, player, target) -> {
		for (Attack listener : listeners) {
			ActionResultType result = listener.onAttack(game, player, target);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<InteractEntity> INTERACT_ENTITY = GameEventType.create(InteractEntity.class, listeners -> (game, player, target, hand) -> {
		for (InteractEntity listener : listeners) {
			listener.onInteract(game, player, target, hand);
		}
	});

	public static final GameEventType<LeftClickBlock> LEFT_CLICK_BLOCK = GameEventType.create(LeftClickBlock.class, listeners -> (game, player, pos) -> {
		for (LeftClickBlock listener : listeners) {
			listener.onLeftClickBlock(game, player, pos);
		}
	});

	public static final GameEventType<BreakBlock> BREAK_BLOCK = GameEventType.create(BreakBlock.class, listeners -> (game, player, pos, state) -> {
		for (BreakBlock listener : listeners) {
			ActionResultType result = listener.onBreakBlock(game, player, pos, state);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<PlaceBlock> PLACE_BLOCK = GameEventType.create(PlaceBlock.class, listeners -> (game, player, pos, placed, placedOn) -> {
		for (PlaceBlock listener : listeners) {
			ActionResultType result = listener.onPlaceBlock(game, player, pos, placed, placedOn);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<Death> DEATH = GameEventType.create(Death.class, listeners -> (game, player, damageSource) -> {
		for (Death listener : listeners) {
			ActionResultType result = listener.onDeath(game, player, damageSource);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	public static final GameEventType<Respawn> RESPAWN = GameEventType.create(Respawn.class, listeners -> (game, player) -> {
		for (Respawn listener : listeners) {
			listener.onRespawn(game, player);
		}
	});

	private GamePlayerEvents() {
	}

	public interface Join {
		void onJoin(IActiveGame game, ServerPlayerEntity player, PlayerRole role);
	}

	public interface Leave {
		void onLeave(IActiveGame game, ServerPlayerEntity player);
	}

	public interface Tick {
		void tick(IActiveGame game, ServerPlayerEntity player);
	}

	public interface ChangeRole {
		void onChangeRole(IActiveGame game, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole);
	}

	public interface Damage {
		ActionResultType onDamage(IActiveGame game, ServerPlayerEntity player, DamageSource damageSource, float amount);
	}

	public interface Attack {
		ActionResultType onAttack(IActiveGame game, ServerPlayerEntity player, Entity target);
	}

	public interface InteractEntity {
		void onInteract(IActiveGame game, ServerPlayerEntity player, Entity target, Hand hand);
	}

	public interface LeftClickBlock {
		void onLeftClickBlock(IActiveGame game, ServerPlayerEntity player, BlockPos pos);
	}

	public interface BreakBlock {
		ActionResultType onBreakBlock(IActiveGame game, ServerPlayerEntity player, BlockPos pos, BlockState state);
	}

	public interface PlaceBlock {
		ActionResultType onPlaceBlock(IActiveGame game, ServerPlayerEntity player, BlockPos pos, BlockState placed, BlockState placedOn);
	}

	public interface Death {
		ActionResultType onDeath(IActiveGame game, ServerPlayerEntity player, DamageSource damageSource);
	}

	public interface Respawn {
		void onRespawn(IActiveGame game, ServerPlayerEntity player);
	}
}
