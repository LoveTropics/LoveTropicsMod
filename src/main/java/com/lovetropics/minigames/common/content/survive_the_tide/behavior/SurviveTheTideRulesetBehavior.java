package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.lovetropics.minigames.common.core.map.MapRegion;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.instances.PhasesGameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import java.util.List;

public class SurviveTheTideRulesetBehavior implements IGameBehavior
{
	public static final Codec<SurviveTheTideRulesetBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("spawn_area_region", "spawn_area").forGetter(c -> c.spawnAreaKey),
				Codec.STRING.fieldOf("phase_to_free_participants").forGetter(c -> c.phaseToFreeParticipants),
				Codec.STRING.listOf().fieldOf("phases_with_no_pvp").forGetter(c -> c.phasesWithNoPVP),
				Codec.BOOL.optionalFieldOf("force_drop_items_on_death", true).forGetter(c -> c.forceDropItemsOnDeath),
				MoreCodecs.TEXT.fieldOf("message_on_set_players_free").forGetter(c -> c.messageOnSetPlayersFree)
		).apply(instance, SurviveTheTideRulesetBehavior::new);
	});

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

	@Override
	public ImmutableList<GameBehaviorType<? extends IGameBehavior>> dependencies() {
		return ImmutableList.of(GameBehaviorTypes.PHASES.get());
	}

	@Override
	public void onConstruct(IGameInstance minigame) {
		spawnArea = minigame.getMapRegions().getOne(spawnAreaKey);
	}

	@Override
	public void onPlayerDeath(final IGameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {
		if (forceDropItemsOnDeath && player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
			destroyVanishingCursedItems(player.inventory);
			player.inventory.dropAllItems();
		}
	}

	@Override
	public void onPlayerHurt(final IGameInstance minigame, LivingHurtEvent event) {
		minigame.getOneBehavior(GameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			if (event.getSource().getTrueSource() instanceof PlayerEntity && isSafePhase(phases.getCurrentPhase())) {
				event.setCanceled(true);
			}
		});
	}

	@Override
	public void onPlayerAttackEntity(final IGameInstance minigame, AttackEntityEvent event) {
		minigame.getOneBehavior(GameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			if (event.getTarget() instanceof PlayerEntity && isSafePhase(phases.getCurrentPhase())) {
				event.setCanceled(true);
			}
		});
	}

	@Override
	public void worldUpdate(final IGameInstance minigame, ServerWorld world) {
		if (!hasFreedParticipants) {
			minigame.getOneBehavior(GameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
				if (phases.getCurrentPhase().getKey().equals(phaseToFreeParticipants)) {
					hasFreedParticipants = true;
					setParticipantsFree(minigame, world, phases.getCurrentPhase());
				}
			});
		}
	}

	@Override
	public void onFinish(final IGameInstance minigame) {
		hasFreedParticipants = false;
	}

	public boolean isSafePhase(PhasesGameBehavior.MinigamePhase phase) {
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

	private void setParticipantsFree(final IGameInstance minigame, final World world, final PhasesGameBehavior.MinigamePhase newPhase) {
		// Destroy all fences blocking players from getting out of spawn area for phase 0
		for (BlockPos p : spawnArea) {
			if (world.getBlockState(p).getBlock() instanceof FenceBlock) {
				world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
			}
		}

		minigame.getPlayers().sendMessage(messageOnSetPlayersFree);

		// So players can drop down without fall damage
		minigame.getPlayers().addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 20 * 20));
	}
}