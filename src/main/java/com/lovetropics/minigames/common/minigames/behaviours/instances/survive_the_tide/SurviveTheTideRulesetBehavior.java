package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.client.data.TropicraftLangKeys;
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
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.List;

public class SurviveTheTideRulesetBehavior implements IMinigameBehavior
{
	private final BlockPos spawnAreaMin;
	private final BlockPos spawnAreaMax;
	private final String phaseToFreeParticipants;
	private final List<String> phasesWithNoPVP;
	private final boolean forceDropItemsOnDeath;

	private boolean hasFreedParticipants = false;

	public SurviveTheTideRulesetBehavior(final BlockPos spawnAreaMin, final BlockPos spawnAreaMax, final String phaseToFreeParticipants, final List<String> phasesWithNoPVP, final boolean forceDropItemsOnDeath) {
		this.spawnAreaMin = spawnAreaMin;
		this.spawnAreaMax = spawnAreaMax;
		this.phaseToFreeParticipants = phaseToFreeParticipants;
		this.phasesWithNoPVP = phasesWithNoPVP;
		this.forceDropItemsOnDeath = forceDropItemsOnDeath;
	}

	public static <T> SurviveTheTideRulesetBehavior parse(Dynamic<T> root) {
		final BlockPos spawnAreaMin = root.get("spawn_area_min").map(BlockPos::deserialize).orElse(BlockPos.ZERO);
		final BlockPos spawnAreaMax = root.get("spawn_area_max").map(BlockPos::deserialize).orElse(BlockPos.ZERO);
		final String phaseToFreeParticipants = root.get("phase_to_free_participants").asString("");
		final List<String> phasesWithNoPVP = root.get("phases_with_no_pvp").asList(d -> d.asString(""));
		final boolean forceDropItemsOnDeath = root.get("force_drop_items_on_death").asBoolean(true);

		return new SurviveTheTideRulesetBehavior(spawnAreaMin, spawnAreaMax, phaseToFreeParticipants, phasesWithNoPVP, forceDropItemsOnDeath);
	}

	@Override
	public ImmutableList<IMinigameBehaviorType<?>> dependencies() {
		return ImmutableList.of(MinigameBehaviorTypes.PHASES.get());
	}

	@Override
	public void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player) {
		if (forceDropItemsOnDeath && player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
			destroyVanishingCursedItems(player.inventory);
			player.inventory.dropAllItems();
		}
	}

	@Override
	public void onPlayerHurt(final IMinigameInstance minigame, LivingHurtEvent event) {
		minigame.getDefinition().getBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			if (event.getSource().getTrueSource() instanceof PlayerEntity && isSafePhase(phases.getCurrentPhase())) {
				event.setCanceled(true);
			}
		});
	}

	@Override
	public void onPlayerAttackEntity(final IMinigameInstance minigame, AttackEntityEvent event) {
		minigame.getDefinition().getBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			if (event.getTarget() instanceof PlayerEntity && isSafePhase(phases.getCurrentPhase())) {
				event.setCanceled(true);
			}
		});
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		if (!hasFreedParticipants) {
			minigame.getDefinition().getBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
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
		for (BlockPos p : BlockPos.getAllInBoxMutable(spawnAreaMin, spawnAreaMax)) {
			if (world.getBlockState(p).getBlock() instanceof FenceBlock) {
				world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
			}
		}

		int minutes = newPhase.getLengthInTicks() / 20 / 60;
		minigame.getPlayers().sendMessage(new TranslationTextComponent(TropicraftLangKeys.SURVIVE_THE_TIDE_PVP_DISABLED, new StringTextComponent(String.valueOf(minutes))).applyTextStyle(
				TextFormatting.YELLOW));

		// So players can drop down without fall damage
		minigame.getPlayers().addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 10 * 20));
	}
}
