package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.ResourceLocation;

public class LootPackageBehavior implements IGameBehavior {
	public static final Codec<LootPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				ResourceLocation.CODEC.fieldOf("loot_table").forGetter(c -> c.lootTable)
		).apply(instance, LootPackageBehavior::new);
	});

	private final ResourceLocation lootTable;

	public LootPackageBehavior(ResourceLocation lootTable) {
		this.lootTable = lootTable;
	}

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) throws GameException {
		events.listen(GamePackageEvents.APPLY_PACKAGE, (game, player, sendingPlayer) -> addLootTableToInventory(player));
	}

	private void addLootTableToInventory(final ServerPlayerEntity player) {
		LootContext context = (new LootContext.Builder(player.getServerWorld()))
				.withParameter(LootParameters.THIS_ENTITY, player)
				.withParameter(LootParameters.ORIGIN, player.getPositionVec())
				.withRandom(player.getRNG())
				.withLuck(player.getLuck())
				.build(LootParameterSets.GIFT);

		boolean changed = false;
		for (ItemStack stack : player.server.getLootTableManager().getLootTableFromLocation(lootTable).generate(context)) {
			if (Util.addItemStackToInventory(player, stack)) {
				changed = true;
			}
		}

		if (changed) {
			player.container.detectAndSendChanges();
		}
	}
}
