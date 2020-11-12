package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.Util;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameterSets;
import net.minecraft.world.storage.loot.LootParameters;

public class LootPackageBehavior extends DonationPackageBehavior
{
	private final ResourceLocation lootTable;

	public LootPackageBehavior(final DonationPackageData data, final ResourceLocation lootTable) {
		super(data);

		this.lootTable = lootTable;
	}

	public static <T> LootPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);
		final ResourceLocation lootTable = new ResourceLocation(root.get("loot_table").asString(""));

		return new LootPackageBehavior(data, lootTable);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		addLootTableToInventory(player);
	}

	private void addLootTableToInventory(final ServerPlayerEntity player) {
		LootContext lootcontext = (new LootContext.Builder(player.getServerWorld())).withParameter(LootParameters.THIS_ENTITY, player).withParameter(LootParameters.POSITION, new BlockPos(player)).withRandom(player.getRNG()).withLuck(player.getLuck()).build(
				LootParameterSets.GIFT);
		boolean flag = false;

		for(ItemStack itemstack : player.server.getLootTableManager().getLootTableFromLocation(lootTable).generate(lootcontext)) {
			if (Util.addItemStackToInventory(player, itemstack)) {
				flag = true;
			}
		}

		if (flag) {
			player.container.detectAndSendChanges();
		}
	}
}
