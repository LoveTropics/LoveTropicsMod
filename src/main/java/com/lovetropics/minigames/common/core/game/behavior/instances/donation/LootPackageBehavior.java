package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
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
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		events.listen(GamePackageEvents.APPLY_PACKAGE_TO_PLAYER, (player, sendingPlayer) -> addLootTableToInventory(player));
	}

	private boolean addLootTableToInventory(final ServerPlayerEntity player) {
		LootContext context = (new LootContext.Builder(player.getLevel()))
				.withParameter(LootParameters.THIS_ENTITY, player)
				.withParameter(LootParameters.ORIGIN, player.position())
				.withRandom(player.getRandom())
				.withLuck(player.getLuck())
				.create(LootParameterSets.GIFT);

		boolean changed = false;
		for (ItemStack stack : player.server.getLootTables().get(lootTable).getRandomItems(context)) {
			if (Util.addItemStackToInventory(player, stack)) {
				changed = true;
			}
		}

		if (changed) {
			player.inventoryMenu.broadcastChanges();
			return true;
		} else {
			return false;
		}
	}
}
