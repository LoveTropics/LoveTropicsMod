package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GamePhase;
import com.lovetropics.minigames.common.core.game.state.GamePhaseState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.List;

public class SurviveTheTideRulesetBehavior implements IGameBehavior {
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
	@Nullable
	private BlockBox spawnArea;
	private final String phaseToFreeParticipants;
	private final List<String> phasesWithNoPVP;
	private final boolean forceDropItemsOnDeath;
	private final Component messageOnSetPlayersFree;

	private boolean hasFreedParticipants = false;

	private GamePhaseState phases;

	public SurviveTheTideRulesetBehavior(final String spawnAreaKey, final String phaseToFreeParticipants, final List<String> phasesWithNoPVP, final boolean forceDropItemsOnDeath, final Component messageOnSetPlayersFree) {
		this.spawnAreaKey = spawnAreaKey;
		this.phaseToFreeParticipants = phaseToFreeParticipants;
		this.phasesWithNoPVP = phasesWithNoPVP;
		this.forceDropItemsOnDeath = forceDropItemsOnDeath;
		this.messageOnSetPlayersFree = messageOnSetPlayersFree;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		phases = game.getState().getOrThrow(GamePhaseState.KEY);

		spawnArea = game.getMapRegions().getAny(spawnAreaKey);

		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
		events.listen(GamePlayerEvents.DAMAGE, this::onPlayerHurt);
		events.listen(GamePlayerEvents.ATTACK, this::onPlayerAttackEntity);

		events.listen(GamePhaseEvents.TICK, () -> tick(game));

		events.listen(GameLivingEntityEvents.ENDER_PEARL_TELEPORT, (player, x, y, z, damage, callback) -> {
			callback.accept(0f); // Set ender pearl damage to 0
		});
	}

	private InteractionResult onPlayerDeath(ServerPlayer player, DamageSource damageSource) {
		if (forceDropItemsOnDeath && player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			destroyVanishingCursedItems(player.getInventory());
			player.getInventory().dropAll();
		}
		return InteractionResult.PASS;
	}

	private InteractionResult onPlayerHurt(ServerPlayer player, DamageSource source, float amount) {
		if ((source.getEntity() instanceof ServerPlayer || source.isProjectile()) && phases.is(this::isSafePhase)) {
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	private InteractionResult onPlayerAttackEntity(ServerPlayer player, Entity target) {
		if (target instanceof ServerPlayer && phases.is(this::isSafePhase)) {
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	private void tick(final IGamePhase game) {
		if (!hasFreedParticipants && phases.is(phaseToFreeParticipants)) {
			hasFreedParticipants = true;
			setParticipantsFree(game);
		}
	}

	public boolean isSafePhase(GamePhase phase) {
		return phasesWithNoPVP.contains(phase.key);
	}

	private void destroyVanishingCursedItems(Container inventory) {
		for (int i = 0; i < inventory.getContainerSize(); ++i) {
			ItemStack itemstack = inventory.getItem(i);
			if (!itemstack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemstack)) {
				inventory.removeItemNoUpdate(i);
			}
		}
	}

	private void setParticipantsFree(final IGamePhase game) {
		// Destroy all fences blocking players from getting out of spawn area for phase 0
		ServerLevel world = game.getWorld();
		if (spawnArea != null) {
			for (BlockPos p : spawnArea) {
				if (world.getBlockState(p).getBlock() instanceof FenceBlock) {
					world.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
				}
			}
		}

		game.getAllPlayers().sendMessage(messageOnSetPlayersFree);

		// So players can drop down without fall damage
		game.getParticipants().addPotionEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 20 * 20));
	}
}
