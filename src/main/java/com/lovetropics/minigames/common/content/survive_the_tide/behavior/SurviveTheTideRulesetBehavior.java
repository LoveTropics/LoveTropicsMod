package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.instances.GamePhase;
import com.lovetropics.minigames.common.core.game.state.instances.GamePhaseState;
import com.lovetropics.minigames.common.core.map.MapRegion;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.block.FenceBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.server.ServerWorld;

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

	private GamePhaseState phases;

	public SurviveTheTideRulesetBehavior(final String spawnAreaKey, final String phaseToFreeParticipants, final List<String> phasesWithNoPVP, final boolean forceDropItemsOnDeath, final ITextComponent messageOnSetPlayersFree) {
		this.spawnAreaKey = spawnAreaKey;
		this.phaseToFreeParticipants = phaseToFreeParticipants;
		this.phasesWithNoPVP = phasesWithNoPVP;
		this.forceDropItemsOnDeath = forceDropItemsOnDeath;
		this.messageOnSetPlayersFree = messageOnSetPlayersFree;
	}

	@Override
	public void register(IActiveGame game, EventRegistrar events) throws GameException {
		spawnArea = game.getMapRegions().getOne(spawnAreaKey);
		phases = game.getState().getOrThrow(GamePhaseState.TYPE);

		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
		events.listen(GamePlayerEvents.DAMAGE, this::onPlayerHurt);
		events.listen(GamePlayerEvents.ATTACK, this::onPlayerAttackEntity);

		events.listen(GameLifecycleEvents.TICK, this::tick);
	}

	private ActionResultType onPlayerDeath(final IActiveGame game, ServerPlayerEntity player, DamageSource damageSource) {
		if (forceDropItemsOnDeath && player.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) {
			destroyVanishingCursedItems(player.inventory);
			player.inventory.dropAllItems();
		}
		return ActionResultType.PASS;
	}

	private ActionResultType onPlayerHurt(final IActiveGame game, ServerPlayerEntity player, DamageSource source, float amount) {
		if (source.getTrueSource() instanceof ServerPlayerEntity && phases.is(this::isSafePhase)) {
			return ActionResultType.FAIL;
		}
		return ActionResultType.PASS;
	}

	private ActionResultType onPlayerAttackEntity(final IActiveGame game, ServerPlayerEntity player, Entity target) {
		if (target instanceof ServerPlayerEntity && phases.is(this::isSafePhase)) {
			return ActionResultType.FAIL;
		}
		return ActionResultType.PASS;
	}

	private void tick(final IActiveGame game) {
		if (!hasFreedParticipants && phases.is(phaseToFreeParticipants)) {
			hasFreedParticipants = true;
			setParticipantsFree(game);
		}
	}

	public boolean isSafePhase(GamePhase phase) {
		return phasesWithNoPVP.contains(phase.key);
	}

	private void destroyVanishingCursedItems(IInventory inventory) {
		for(int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack itemstack = inventory.getStackInSlot(i);
			if (!itemstack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemstack)) {
				inventory.removeStackFromSlot(i);
			}
		}
	}

	private void setParticipantsFree(final IActiveGame game) {
		// Destroy all fences blocking players from getting out of spawn area for phase 0
		ServerWorld world = game.getWorld();
		for (BlockPos p : spawnArea) {
			if (world.getBlockState(p).getBlock() instanceof FenceBlock) {
				world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
			}
		}

		game.getAllPlayers().sendMessage(messageOnSetPlayersFree);

		// So players can drop down without fall damage
		game.getParticipants().addPotionEffect(new EffectInstance(Effects.SLOW_FALLING, 20 * 20));
	}
}
