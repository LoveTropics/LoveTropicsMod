package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.IGameLookup;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.lovetropics.minigames.common.util.Scheduler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

public final class GameEventDispatcher {
	private final IGameLookup gameLookup;

	public GameEventDispatcher(IGameLookup gameLookup) {
		this.gameLookup = gameLookup;
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		if (event.getWorld() instanceof IServerWorld) {
			ServerWorld world = ((IServerWorld) event.getWorld()).getWorld();
			IChunk chunk = event.getChunk();
			IGamePhase game = gameLookup.getGamePhaseAt(world, chunk.getPos().asBlockPos());
			if (game != null) {
				Scheduler.nextTick().run(server -> {
					game.invoker(GameWorldEvents.CHUNK_LOAD).onChunkLoad(chunk);
				});
			}
		}
	}

	@SubscribeEvent
	public void onPlayerHurt(LivingHurtEvent event) {
		Entity entity = event.getEntity();
		if (entity instanceof ServerPlayerEntity) {
			IGamePhase game = gameLookup.getGamePhaseFor(entity);
			if (game != null) {
				try {
					ActionResultType result = game.invoker(GamePlayerEvents.DAMAGE).onDamage((ServerPlayerEntity) entity, event.getSource(), event.getAmount());
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
		Entity target = event.getEntity();

		PlayerEntity player = event.getPlayer();
		IGamePhase game = gameLookup.getGamePhaseFor(player);
		if (game != null) {
			if (this.dispatchAttackEvent(game, (ServerPlayerEntity) player,target)) {
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onDamageEntityIndirect(LivingHurtEvent event) {
		Entity target = event.getEntity();

		IGamePhase game = gameLookup.getGamePhaseFor(target);
		if (game != null) {
			Entity indirectSource = getIndirectSource(event.getSource());
			if (indirectSource instanceof ServerPlayerEntity) {
				ServerPlayerEntity sourcePlayer = (ServerPlayerEntity) indirectSource;
				if (this.dispatchAttackEvent(game, sourcePlayer, target)) {
					event.setCanceled(true);
				}
			}
		}
	}

	@Nullable
	private Entity getIndirectSource(DamageSource source) {
		if (source.getTrueSource() != source.getImmediateSource()) {
			return source.getTrueSource();
		}
		return null;
	}

	private boolean dispatchAttackEvent(IGamePhase game, ServerPlayerEntity player, Entity target) {
		try {
			ActionResultType result = game.invoker(GamePlayerEvents.ATTACK).onAttack(player, target);
			return result == ActionResultType.FAIL;
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player attack event", e);
		}
		return false;
	}

	@SubscribeEvent
	public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		IGamePhase game = gameLookup.getGamePhaseFor(entity);
		if (game != null) {
			if (entity instanceof ServerPlayerEntity && game.getParticipants().contains(entity)) {
				try {
					game.invoker(GamePlayerEvents.TICK).tick((ServerPlayerEntity) entity);
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player tick event", e);
				}
			}

			try {
				game.invoker(GameLivingEntityEvents.TICK).tick(entity);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch living tick event", e);
			}
		}
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();

		IGamePhase game = gameLookup.getGamePhaseFor(entity);
		if (game != null) {
			if (entity instanceof ServerPlayerEntity) {
				try {
					ActionResultType result = game.invoker(GamePlayerEvents.DEATH).onDeath((ServerPlayerEntity) entity, event.getSource());
					if (result == ActionResultType.FAIL) {
						event.setCanceled(true);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player death event", e);
				}
			} else {
				try {
					ActionResultType result = game.invoker(GameLivingEntityEvents.DEATH).onDeath(entity, event.getSource());
					if (result == ActionResultType.FAIL) {
						event.setCanceled(true);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch entity death event", e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onMobDrop(LivingDropsEvent event) {
		LivingEntity entity = event.getEntityLiving();

		IGamePhase game = gameLookup.getGamePhaseFor(entity);
		if (game != null) {
			try {
				ActionResultType result = game.invoker(GameLivingEntityEvents.MOB_DROP).onMobDrop(entity, event.getSource(), event.getDrops());
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch entity mob drop event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getPlayer());
		if (game != null) {
			try {
				game.invoker(GamePlayerEvents.RESPAWN).onRespawn((ServerPlayerEntity) event.getPlayer());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player respawn event", e);
			}
		}
	}

	@SubscribeEvent
	public void onFarmlandTrample(BlockEvent.FarmlandTrampleEvent event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getEntity());
		if (game != null) {
			try {
				ActionResultType result = game.invoker(GameLivingEntityEvents.FARMLAND_TRAMPLE).onFarmlandTrample(event.getEntity(), event.getPos(), event.getState());
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch farmland trample event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerThrowItem(ItemTossEvent event) {
		PlayerEntity player = event.getPlayer();
		IGamePhase game = gameLookup.getGamePhaseFor(player);
		if (game != null) {
			try {
				ItemEntity item = event.getEntityItem();
				ActionResultType result = game.invoker(GamePlayerEvents.THROW_ITEM).onThrowItem((ServerPlayerEntity) player, item);
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player throw item event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getPlayer());
		if (game != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

			try {
				ActionResultType result = game.invoker(GamePlayerEvents.INTERACT_ENTITY).onInteractEntity(player, event.getTarget(), event.getHand());
				if (result != ActionResultType.PASS) {
					event.setCancellationResult(result);
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player interact entity event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getPlayer());
		if (game != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

			try {
				game.invoker(GamePlayerEvents.LEFT_CLICK_BLOCK).onLeftClickBlock(player, player.getServerWorld(), event.getPos());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player left click block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getPlayer());
		if (game != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

			try {
				ActionResultType result = game.invoker(GamePlayerEvents.USE_BLOCK).onUseBlock(player, player.getServerWorld(), event.getPos(), event.getHand(), event.getHitVec());
				if (result != ActionResultType.PASS) {
					event.setCanceled(true);
					event.setCancellationResult(result);
					this.resendPlayerHeldItem(player);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player use block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getPlayer());
		if (game != null) {
			ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

			try {
				ActionResultType result = game.invoker(GamePlayerEvents.USE_ITEM).onUseItem(player, event.getHand());
				if (result != ActionResultType.PASS) {
					event.setCancellationResult(result);
					event.setCanceled(true);
					this.resendPlayerHeldItem(player);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player item use event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerBreakBlock(BlockEvent.BreakEvent event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getPlayer());
		if (game != null) {
			try {
				ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();
				Hand hand = player.getActiveHand();
				ActionResultType result = game.invoker(GamePlayerEvents.BREAK_BLOCK).onBreakBlock(player, event.getPos(), event.getState(), hand);
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
		IGamePhase game = gameLookup.getGamePhaseFor(event.getEntity());
		if (game != null && event.getEntity() instanceof ServerPlayerEntity) {
			try {
				ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
				ActionResultType result = game.invoker(GamePlayerEvents.PLACE_BLOCK).onPlaceBlock(player, event.getPos(), event.getPlacedBlock(), event.getPlacedAgainst());
				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
					this.resendPlayerHeldItem(player);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player place block event", e);
			}
		}
	}

	private void resendPlayerHeldItem(ServerPlayerEntity player) {
		Hand hand = player.getActiveHand();
		int handSlot = hand == Hand.MAIN_HAND ? player.inventory.currentItem : 40;
		ItemStack handItem = player.getHeldItem(hand);
		player.connection.sendPacket(new SSetSlotPacket(-2, handSlot, handItem));
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Detonate event) {
		IGamePhase game = gameLookup.getGamePhaseAt(event.getWorld(), new BlockPos(event.getExplosion().getPosition()));
		if (game != null) {
			try {
				game.invoker(GameWorldEvents.EXPLOSION_DETONATE).onExplosionDetonate(event.getExplosion(), event.getAffectedBlocks(), event.getAffectedEntities());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch explosion event", e);
			}
		}
	}

	@SubscribeEvent
	public void onTreeGrow(SaplingGrowTreeEvent event) {
		IGamePhase game = gameLookup.getGamePhaseAt((World) event.getWorld(), event.getPos());
		if (game != null) {
			try {
				ActionResultType result = game.invoker(GameWorldEvents.SAPLING_GROW).onSaplingGrow((World) event.getWorld(), event.getPos());

				if (result == ActionResultType.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch tree grow event", e);
			}
		}
	}
}
