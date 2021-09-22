package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.IGameLookup;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.lovetropics.minigames.common.util.Scheduler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class GameEventDispatcher {
	private final IGameLookup gameLookup;

	public GameEventDispatcher(IGameLookup gameLookup) {
		this.gameLookup = gameLookup;
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		if (event.getWorld() instanceof IServerWorld) {
			ServerWorld world = ((IServerWorld) event.getWorld()).getWorld();
			IActiveGame game = gameLookup.getGameAt(world, event.getChunk().getPos().asBlockPos());
			if (game != null) {
				IChunk chunk = event.getChunk();
				Scheduler.INSTANCE.submit(s -> {
					game.invoker(GameWorldEvents.CHUNK_LOAD).onChunkLoad(game, chunk);
				});
			}
		}
	}

	@SubscribeEvent
	public void onPlayerHurt(LivingHurtEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayerEntity) {
			IActiveGame game = gameLookup.getGameFor(entity);
			if (game != null) {
				try {
					ActionResultType result = game.invoker(GamePlayerEvents.DAMAGE).onDamage(game, (ServerPlayerEntity) entity, event.getSource(), event.getAmount());
					if (result == ActionResultType.FAIL) {
						event.setCanceled(true);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player hurt event", e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		IActiveGame game = gameLookup.getGameFor(event.getPlayer());
		if (game != null && event.getPlayer() instanceof ServerPlayerEntity) {
			try {
				ActionResultType result = game.invoker(GamePlayerEvents.ATTACK).onAttack(game, (ServerPlayerEntity) event.getPlayer(), event.getTarget());
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player attack event", e);
			}
		}
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		IActiveGame game = gameLookup.getGameFor(entity);
		if (game != null) {
			if (entity instanceof ServerPlayerEntity && game.getParticipants().contains(entity)) {
				try {
					game.invoker(GamePlayerEvents.TICK).tick(game, (ServerPlayerEntity) entity);
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player tick event", e);
				}
			}

			try {
				game.invoker(GameLivingEntityEvents.TICK).tick(game, entity);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch living tick event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (entity instanceof ServerPlayerEntity) {
			IActiveGame game = gameLookup.getGameFor(entity);
			if (game != null) {
				try {
					ActionResultType result = game.invoker(GamePlayerEvents.DEATH).onDeath(game, (ServerPlayerEntity) entity, event.getSource());
					if (result == ActionResultType.FAIL) {
						event.setCanceled(true);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player death event", e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		IActiveGame game = gameLookup.getGameFor(event.getPlayer());
		if (game != null) {
			try {
				game.invoker(GamePlayerEvents.RESPAWN).onRespawn(game, (ServerPlayerEntity) event.getPlayer());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player respawn event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
		IActiveGame game = gameLookup.getGameFor(event.getPlayer());
		if (game != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

			try {
				game.invoker(GamePlayerEvents.INTERACT_ENTITY).onInteract(game, player, event.getTarget(), event.getHand());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player interact entity event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		IActiveGame game = gameLookup.getGameFor(event.getPlayer());
		if (game != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

			try {
				game.invoker(GamePlayerEvents.LEFT_CLICK_BLOCK).onLeftClickBlock(game, player, event.getPos());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player left click block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
		IActiveGame game = gameLookup.getGameFor(event.getPlayer());
		if (game != null) {
			try {
				ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
				ActionResultType result = game.invoker(GamePlayerEvents.BREAK_BLOCK).onBreakBlock(game, player, event.getPos(), event.getState());
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player break block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onEntityPlaceBlock(BlockEvent.EntityPlaceEvent event) {
		IActiveGame game = gameLookup.getGameFor(event.getEntity());
		if (game != null && event.getEntity() instanceof ServerPlayerEntity) {
			try {
				ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
				ActionResultType result = game.invoker(GamePlayerEvents.PLACE_BLOCK).onPlaceBlock(game, player, event.getPos(), event.getPlacedBlock(), event.getPlacedAgainst());
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player place block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Detonate event) {
		IActiveGame game = gameLookup.getGameAt(event.getWorld(), new BlockPos(event.getExplosion().getPosition()));
		if (game != null) {
			try {
				game.invoker(GameWorldEvents.EXPLOSION_DETONATE).onExplosionDetonate(game, event.getExplosion(), event.getAffectedBlocks(), event.getAffectedEntities());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch explosion event", e);
			}
		}
	}
}
