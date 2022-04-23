package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.resources.ResourceLocation;

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

	private boolean addLootTableToInventory(final ServerPlayer player) {
		LootContext context = (new LootContext.Builder(player.getLevel()))
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.withParameter(LootContextParams.ORIGIN, player.position())
				.withRandom(player.getRandom())
				.withLuck(player.getLuck())
				.create(LootContextParamSets.GIFT);

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
