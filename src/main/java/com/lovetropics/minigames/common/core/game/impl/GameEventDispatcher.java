package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.IGameLookup;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.lovetropics.minigames.common.util.Scheduler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
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
		if (event.getWorld() instanceof ServerLevelAccessor) {
			ServerLevel world = ((ServerLevelAccessor) event.getWorld()).getLevel();
			ChunkAccess chunk = event.getChunk();
			IGamePhase game = gameLookup.getGamePhaseAt(world, chunk.getPos().getWorldPosition());
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
		if (entity instanceof ServerPlayer) {
			IGamePhase game = gameLookup.getGamePhaseFor(entity);
			if (game != null) {
				try {
					ServerPlayer player = (ServerPlayer) entity;
					DamageSource source = event.getSource();
					float amount = event.getAmount();

					InteractionResult result = game.invoker(GamePlayerEvents.DAMAGE).onDamage(player, source, amount);
					if (result == InteractionResult.FAIL) {
						event.setCanceled(true);
						return;
					}

					float newAmount = game.invoker(GamePlayerEvents.DAMAGE_AMOUNT).getDamageAmount(player, source, amount, amount);
					if (newAmount != amount) {
						event.setAmount(newAmount);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player hurt event", e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		Entity target = event.getTarget();

		Player player = event.getPlayer();
		IGamePhase game = gameLookup.getGamePhaseFor(player);
		if (game != null) {
			if (this.dispatchAttackEvent(game, (ServerPlayer) player, target)) {
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
			if (indirectSource instanceof ServerPlayer) {
				ServerPlayer sourcePlayer = (ServerPlayer) indirectSource;
				if (this.dispatchAttackEvent(game, sourcePlayer, target)) {
					event.setCanceled(true);
				}
			}
		}
	}

	@Nullable
	private Entity getIndirectSource(DamageSource source) {
		if (source.getEntity() != source.getDirectEntity()) {
			return source.getEntity();
		}
		return null;
	}

	private boolean dispatchAttackEvent(IGamePhase game, ServerPlayer player, Entity target) {
		try {
			InteractionResult result = game.invoker(GamePlayerEvents.ATTACK).onAttack(player, target);
			return result == InteractionResult.FAIL;
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
			if (entity instanceof ServerPlayer && game.getParticipants().contains(entity)) {
				try {
					game.invoker(GamePlayerEvents.TICK).tick((ServerPlayer) entity);
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
			if (entity instanceof ServerPlayer) {
				try {
					InteractionResult result = game.invoker(GamePlayerEvents.DEATH).onDeath((ServerPlayer) entity, event.getSource());
					if (result == InteractionResult.FAIL) {
						event.setCanceled(true);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player death event", e);
				}
			} else {
				try {
					InteractionResult result = game.invoker(GameLivingEntityEvents.DEATH).onDeath(entity, event.getSource());
					if (result == InteractionResult.FAIL) {
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
				InteractionResult result = game.invoker(GameLivingEntityEvents.MOB_DROP).onMobDrop(entity, event.getSource(), event.getDrops());
				if (result == InteractionResult.FAIL) {
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
				game.invoker(GamePlayerEvents.RESPAWN).onRespawn((ServerPlayer) event.getPlayer());
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
				InteractionResult result = game.invoker(GameLivingEntityEvents.FARMLAND_TRAMPLE).onFarmlandTrample(event.getEntity(), event.getPos(), event.getState());
				if (result == InteractionResult.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch farmland trample event", e);
			}
		}
	}

	@SubscribeEvent
	public void onEnderTeleport(EnderTeleportEvent event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getEntity());
		if (game != null) {
			try {
				game.invoker(GameLivingEntityEvents.ENDER_TELEPORT).onEnderTeleport(event.getEntityLiving(), event.getTargetX(), event.getTargetY(), event.getTargetZ(), event.getAttackDamage(), event::setAttackDamage);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch ender teleport event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerThrowItem(ItemTossEvent event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getPlayer());
		if (game != null) {
			try {
				ServerPlayer player = (ServerPlayer) event.getPlayer();
				ItemEntity item = event.getEntityItem();
				InteractionResult result = game.invoker(GamePlayerEvents.THROW_ITEM).onThrowItem(player, item);
				if (result == InteractionResult.FAIL) {
					event.setCanceled(true);
					player.inventory.add(item.getItem());
					player.refreshContainer(player.containerMenu);
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
			ServerPlayer player = (ServerPlayer) event.getPlayer();

			try {
				InteractionResult result = game.invoker(GamePlayerEvents.INTERACT_ENTITY).onInteractEntity(player, event.getTarget(), event.getHand());
				if (result != InteractionResult.PASS) {
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
			ServerPlayer player = (ServerPlayer) event.getPlayer();

			try {
				game.invoker(GamePlayerEvents.LEFT_CLICK_BLOCK).onLeftClickBlock(player, player.getLevel(), event.getPos());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player left click block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getPlayer());
		if (game != null) {
			ServerPlayer player = (ServerPlayer) event.getPlayer();

			try {
				InteractionResult result = game.invoker(GamePlayerEvents.USE_BLOCK).onUseBlock(player, player.getLevel(), event.getPos(), event.getHand(), event.getHitVec());
				if (result != InteractionResult.PASS) {
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
			ServerPlayer player = (ServerPlayer) event.getPlayer();

			try {
				InteractionResult result = game.invoker(GamePlayerEvents.USE_ITEM).onUseItem(player, event.getHand());
				if (result != InteractionResult.PASS) {
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
				ServerPlayer player = (ServerPlayer) event.getPlayer();
				InteractionHand hand = player.getUsedItemHand();
				InteractionResult result = game.invoker(GamePlayerEvents.BREAK_BLOCK).onBreakBlock(player, event.getPos(), event.getState(), hand);
				if (result == InteractionResult.FAIL) {
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
		if (game != null && event.getEntity() instanceof ServerPlayer) {
			try {
				ServerPlayer player = (ServerPlayer) event.getEntity();
				InteractionResult result = game.invoker(GamePlayerEvents.PLACE_BLOCK).onPlaceBlock(player, event.getPos(), event.getPlacedBlock(), event.getPlacedAgainst());
				if (result == InteractionResult.FAIL) {
					event.setCanceled(true);
					this.resendPlayerHeldItem(player);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player place block event", e);
			}
		}
	}

	private void resendPlayerHeldItem(ServerPlayer player) {
		InteractionHand hand = player.getUsedItemHand();
		int handSlot = hand == InteractionHand.MAIN_HAND ? player.inventory.selected : 40;
		ItemStack handItem = player.getItemInHand(hand);
		player.connection.send(new ClientboundContainerSetSlotPacket(-2, handSlot, handItem));
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
		IGamePhase game = gameLookup.getGamePhaseAt((Level) event.getWorld(), event.getPos());
		if (game != null) {
			try {
				InteractionResult result = game.invoker(GameWorldEvents.SAPLING_GROW).onSaplingGrow((Level) event.getWorld(), event.getPos());

				if (result == InteractionResult.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch tree grow event", e);
			}
		}
	}

	@SubscribeEvent
	public void onEntityMount(EntityMountEvent event) {
		Entity entityMounting = event.getEntityMounting();
		Entity entityBeingMounted = event.getEntityBeingMounted();

		IGamePhase game = gameLookup.getGamePhaseFor(entityBeingMounted);
		if (game != null) {
			try {
				InteractionResult result = game.invoker(GameEntityEvents.MOUNTED).onEntityMounted(entityMounting, entityBeingMounted);
				if (result == InteractionResult.FAIL) {
					event.setCanceled(true);
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch entity mount event", e);
			}
		}
	}
}
