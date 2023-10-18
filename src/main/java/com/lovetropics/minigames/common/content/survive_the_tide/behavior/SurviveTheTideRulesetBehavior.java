package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;

public class SurviveTheTideRulesetBehavior implements IGameBehavior {
	public static final MapCodec<SurviveTheTideRulesetBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ProgressionPeriod.CODEC.fieldOf("safe_period").forGetter(c -> c.safePeriod),
			Codec.BOOL.optionalFieldOf("force_drop_items_on_death", true).forGetter(c -> c.forceDropItemsOnDeath)
	).apply(i, SurviveTheTideRulesetBehavior::new));

	private final ProgressionPeriod safePeriod;
	private final boolean forceDropItemsOnDeath;

	private GameProgressionState progression;

	public SurviveTheTideRulesetBehavior(final ProgressionPeriod safePeriod, final boolean forceDropItemsOnDeath) {
		this.safePeriod = safePeriod;
		this.forceDropItemsOnDeath = forceDropItemsOnDeath;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		progression = game.getState().getOrThrow(GameProgressionState.KEY);

		events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
		events.listen(GamePlayerEvents.DAMAGE, this::onPlayerHurt);
		events.listen(GamePlayerEvents.ATTACK, this::onPlayerAttackEntity);

		events.listen(GameLivingEntityEvents.ENDER_PEARL_TELEPORT, (player, x, y, z, damage, callback) -> {
			callback.accept(0f); // Set ender pearl damage to 0
		});
	}

	private InteractionResult onPlayerDeath(ServerPlayer player, DamageSource damageSource) {
		if (forceDropItemsOnDeath && player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			destroyVanishingCursedItems(player.getInventory());
			player.getInventory().dropAll();
		}
		return InteractionResult.PASS;
	}

	private InteractionResult onPlayerHurt(ServerPlayer player, DamageSource source, float amount) {
		if ((source.getEntity() instanceof ServerPlayer || source.is(DamageTypeTags.IS_PROJECTILE)) && progression.is(safePeriod)) {
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

	private void destroyVanishingCursedItems(Container inventory) {
		for (int i = 0; i < inventory.getContainerSize(); ++i) {
			ItemStack itemstack = inventory.getItem(i);
			if (!itemstack.isEmpty() && EnchantmentHelper.hasVanishingCurse(itemstack)) {
				inventory.removeItemNoUpdate(i);
			}
		}
	}
}
