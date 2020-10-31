package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.behaviours.instances.PhasesMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.List;

public class SurviveTheTideRulesetBehavior implements IMinigameBehavior
{
	private final String spawnAreaKey;
	private MapRegion spawnArea;
	private final String phaseToFreeParticipants;
	private final List<String> phasesWithNoPVP;
	private final boolean forceDropItemsOnDeath;
	private final ITextComponent messageOnSetPlayersFree;

	private boolean hasFreedParticipants = false;

	public SurviveTheTideRulesetBehavior(final String spawnAreaKey, final String phaseToFreeParticipants, final List<String> phasesWithNoPVP, final boolean forceDropItemsOnDeath, final ITextComponent messageOnSetPlayersFree) {
		this.spawnAreaKey = spawnAreaKey;
		this.phaseToFreeParticipants = phaseToFreeParticipants;
		this.phasesWithNoPVP = phasesWithNoPVP;
		this.forceDropItemsOnDeath = forceDropItemsOnDeath;
		this.messageOnSetPlayersFree = messageOnSetPlayersFree;
	}

	public static <T> SurviveTheTideRulesetBehavior parse(Dynamic<T> root) {
		final String spawnAreaKey = root.get("spawn_area_region").asString("spawn_area");
		final String phaseToFreeParticipants = root.get("phase_to_free_participants").asString("");
		final List<String> phasesWithNoPVP = root.get("phases_with_no_pvp").asList(d -> d.asString(""));
		final boolean forceDropItemsOnDeath = root.get("force_drop_items_on_death").asBoolean(true);
		final ITextComponent messageOnSetPlayersFree = Util.getText(root, "message_on_set_players_free");

		return new SurviveTheTideRulesetBehavior(spawnAreaKey, phaseToFreeParticipants, phasesWithNoPVP, forceDropItemsOnDeath, messageOnSetPlayersFree);
	}

	@Override
	public ImmutableList<IMinigameBehaviorType<? extends IMinigameBehavior>> dependencies() {
		return ImmutableList.of(MinigameBehaviorTypes.PHASES.get());
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		spawnArea = minigame.getMapRegions().getOne(spawnAreaKey);
	}

	@Override
	public void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		if (forceDropItemsOnDeath && player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
			destroyVanishingCursedItems(player.inventory);
			player.inventory.dropAllItems();
		}
	}

	@Override
	public void onPlayerHurt(final IMinigameInstance minigame, LivingHurtEvent event) {
		minigame.getOneBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			if (event.getSource().getTrueSource() instanceof PlayerEntity && isSafePhase(phases.getCurrentPhase())) {
				event.setCanceled(true);
			}
		});
	}

	@Override
	public void onPlayerAttackEntity(final IMinigameInstance minigame, AttackEntityEvent event) {
		minigame.getOneBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			if (event.getTarget() instanceof PlayerEntity && isSafePhase(phases.getCurrentPhase())) {
				event.setCanceled(true);
			}
		});
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		if (!hasFreedParticipants) {
			minigame.getOneBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
				if (phases.getCurrentPhase().getKey().equals(phaseToFreeParticipants)) {
					hasFreedParticipants = true;
					setParticipantsFree(minigame, world, phases.getCurrentPhase());
				}
			});
		}
	}

	@Override
	public void onFinish(final IMinigameInstance minigame) {
		hasFreedParticipants = false;
	}

	public boolean isSafePhase(PhasesMinigameBehavior.MinigamePhase phase) {
		return phasesWithNoPVP.contains(phase.getKey());
	}

	private void destroyVanishingCursedItems(IInventory inventory) {
		for(int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack itemstack = inventory.getStackInSlot(i);
			if (!itemstack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemstack)) {
				inventory.removeStackFromSlot(i);
			}
		}
	}

	private void setParticipantsFree(final IMinigameInstance minigame, final World world, final PhasesMinigameBehavior.MinigamePhase newPhase) {
		// Destroy all fences blocking players from getting out of spawn area for phase 0
		for (BlockPos p : spawnArea) {
			if (world.getBlockState(p).getBlock() instanceof FenceBlock) {
				world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
			}
		}

		minigame.getPlayers().sendMessage(messageOnSetPlayersFree);

		// So players can drop down without fall damage
		minigame.getPlayers().addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 10 * 20));
	}
}
