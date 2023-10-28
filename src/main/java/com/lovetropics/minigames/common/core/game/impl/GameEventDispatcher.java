package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.IGameLookup;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.lovetropics.minigames.common.util.Scheduler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

public final class GameEventDispatcher {
	private final IGameLookup gameLookup;

	public GameEventDispatcher(IGameLookup gameLookup) {
		this.gameLookup = gameLookup;
	}

	@SubscribeEvent
	public void onChunkLoad(ChunkEvent.Load event) {
		if (event.getLevel() instanceof ServerLevelAccessor levelAccessor) {
			ChunkAccess chunk = event.getChunk();
			IGamePhase game = gameLookup.getGamePhaseAt(levelAccessor.getLevel(), chunk.getPos().getWorldPosition());
			if (game != null) {
				Scheduler.nextTick().run(server -> {
					game.invoker(GameWorldEvents.CHUNK_LOAD).onChunkLoad(chunk);
				});
			}
		}
	}

	@SubscribeEvent
	public void onPlayerHurt(LivingHurtEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			IGamePhase game = gameLookup.getGamePhaseFor(player);
			if (game != null) {
				try {
					DamageSource source = event.getSource();
					float amount = event.getAmount();
					InteractionResult result = game.invoker(GamePlayerEvents.DAMAGE).onDamage(player, source, amount);
					if (result == InteractionResult.FAIL) {
						event.setCanceled(true);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player hurt event", e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerDamage(LivingDamageEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			IGamePhase game = gameLookup.getGamePhaseFor(player);
			if (game != null) {
				try {
					DamageSource source = event.getSource();
					float amount = event.getAmount();
					float newAmount = game.invoker(GamePlayerEvents.DAMAGE_AMOUNT).getDamageAmount(player, source, amount, amount);
					if (newAmount != amount) {
						event.setAmount(newAmount);
					}
				} catch (Exception e) {
					LoveTropics.LOGGER.warn("Failed to dispatch player damage amount event", e);
				}
			}
		}
	}

	@SubscribeEvent
	public void onAttackEntity(AttackEntityEvent event) {
		Entity target = event.getTarget();

		Player player = event.getEntity();
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
	public void onLivingUpdate(LivingEvent.LivingTickEvent event) {
		LivingEntity entity = event.getEntity();
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
		LivingEntity entity = event.getEntity();

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
		LivingEntity entity = event.getEntity();

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
		IGamePhase game = gameLookup.getGamePhaseFor(event.getEntity());
		if (game != null) {
			try {
				game.invoker(GamePlayerEvents.RESPAWN).onRespawn((ServerPlayer) event.getEntity());
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
	public void onEnderTeleport(EntityTeleportEvent.EnderPearl event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getEntity());
		if (game != null) {
			try {
				game.invoker(GameLivingEntityEvents.ENDER_PEARL_TELEPORT).onEnderPearlTeleport(event.getPlayer(), event.getTargetX(), event.getTargetY(), event.getTargetZ(), event.getAttackDamage(), event::setAttackDamage);
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
				ItemEntity item = event.getEntity();
				InteractionResult result = game.invoker(GamePlayerEvents.THROW_ITEM).onThrowItem(player, item);
				if (result == InteractionResult.FAIL) {
					event.setCanceled(true);
					player.getInventory().add(item.getItem());
					player.containerMenu.broadcastFullState();
				}
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player throw item event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getEntity());
		if (game != null) {
			ServerPlayer player = (ServerPlayer) event.getEntity();

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
		IGamePhase game = gameLookup.getGamePhaseFor(event.getEntity());
		if (game != null) {
			ServerPlayer player = (ServerPlayer) event.getEntity();

			try {
				game.invoker(GamePlayerEvents.LEFT_CLICK_BLOCK).onLeftClickBlock(player, player.serverLevel(), event.getPos());
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player left click block event", e);
			}
		}
	}

	@SubscribeEvent
	public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		IGamePhase game = gameLookup.getGamePhaseFor(event.getEntity());
		if (game != null) {
			ServerPlayer player = (ServerPlayer) event.getEntity();

			try {
				InteractionResult result = game.invoker(GamePlayerEvents.USE_BLOCK).onUseBlock(player, player.serverLevel(), event.getPos(), event.getHand(), event.getHitVec());
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
		IGamePhase game = gameLookup.getGamePhaseFor(event.getEntity());
		if (game != null) {
			ServerPlayer player = (ServerPlayer) event.getEntity();

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
		int handSlot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : Inventory.SLOT_OFFHAND;
		ItemStack handItem = player.getItemInHand(hand);
		player.connection.send(new ClientboundContainerSetSlotPacket(ClientboundContainerSetSlotPacket.PLAYER_INVENTORY, 0, handSlot, handItem));
	}

	@SubscribeEvent
	public void onExplosion(ExplosionEvent.Detonate event) {
		IGamePhase game = gameLookup.getGamePhaseAt(event.getLevel(), BlockPos.containing(event.getExplosion().getPosition()));
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
		IGamePhase game = gameLookup.getGamePhaseAt((Level) event.getLevel(), event.getPos());
		if (game != null) {
			try {
				InteractionResult result = game.invoker(GameWorldEvents.SAPLING_GROW).onSaplingGrow((Level) event.getLevel(), event.getPos());

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

	@SubscribeEvent
	public void onItemPickup(PlayerEvent.ItemPickupEvent event) {
		ItemEntity itemEntity = event.getOriginalEntity();
		IGamePhase game = gameLookup.getGamePhaseFor(itemEntity);
		if (game == null) {
			return;
		}
		if (event.getEntity() instanceof ServerPlayer serverPlayer && game.getAllPlayers().contains(serverPlayer)) {
			try {
				game.invoker(GamePlayerEvents.PICK_UP_ITEM).onPickUpItem(serverPlayer, itemEntity);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch item pickup event", e);
			}
		}
	}
}
