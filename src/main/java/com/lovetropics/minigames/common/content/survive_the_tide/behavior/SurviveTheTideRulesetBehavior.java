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
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.lovetropics.minigames.common.core.game.state.ProgressionPoint;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
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

public class SurviveTheTideRulesetBehavior implements IGameBehavior {
	public static final Codec<SurviveTheTideRulesetBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.optionalFieldOf("spawn_area_region", "spawn_area").forGetter(c -> c.spawnAreaKey),
			ProgressionPoint.CODEC.fieldOf("free_participants_at").forGetter(c -> c.freeParticipantsAt),
			ProgressionPeriod.CODEC.fieldOf("safe_period").forGetter(c -> c.safePeriod),
			Codec.BOOL.optionalFieldOf("force_drop_items_on_death", true).forGetter(c -> c.forceDropItemsOnDeath),
			MoreCodecs.TEXT.fieldOf("message_on_set_players_free").forGetter(c -> c.messageOnSetPlayersFree)
	).apply(i, SurviveTheTideRulesetBehavior::new));

	private final String spawnAreaKey;
	@Nullable
	private BlockBox spawnArea;
	private final ProgressionPoint freeParticipantsAt;
	private final ProgressionPeriod safePeriod;
	private final boolean forceDropItemsOnDeath;
	private final Component messageOnSetPlayersFree;

	private boolean hasFreedParticipants = false;

	private GameProgressionState progression;

	public SurviveTheTideRulesetBehavior(final String spawnAreaKey, final ProgressionPoint freeParticipantsAt, final ProgressionPeriod safePeriod, final boolean forceDropItemsOnDeath, final Component messageOnSetPlayersFree) {
		this.spawnAreaKey = spawnAreaKey;
		this.freeParticipantsAt = freeParticipantsAt;
		this.safePeriod = safePeriod;
		this.forceDropItemsOnDeath = forceDropItemsOnDeath;
		this.messageOnSetPlayersFree = messageOnSetPlayersFree;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		progression = game.getState().getOrThrow(GameProgressionState.KEY);

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
		if ((source.getEntity() instanceof ServerPlayer || source.isProjectile()) && progression.is(safePeriod)) {
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	private InteractionResult onPlayerAttackEntity(ServerPlayer player, Entity target) {
		if (target instanceof ServerPlayer && progression.is(safePeriod)) {
			return InteractionResult.FAIL;
		}
		return InteractionResult.PASS;
	}

	private void tick(final IGamePhase game) {
		if (!hasFreedParticipants && progression.isAfter(freeParticipantsAt)) {
			hasFreedParticipants = true;
			setParticipantsFree(game);
		}
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
		game.getParticipants().addPotionEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, SharedConstants.TICKS_PER_SECOND * 10));
	}
}
