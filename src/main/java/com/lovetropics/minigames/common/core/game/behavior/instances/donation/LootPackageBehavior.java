package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.ResourceLocation;

public class LootPackageBehavior extends DonationPackageBehavior
{
	public static final Codec<LootPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				ResourceLocation.CODEC.fieldOf("loot_table").forGetter(c -> c.lootTable)
		).apply(instance, LootPackageBehavior::new);
	});

	private final ResourceLocation lootTable;

	public LootPackageBehavior(final DonationPackageData data, final ResourceLocation lootTable) {
		super(data);
		this.lootTable = lootTable;
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		addLootTableToInventory(player);
	}

	private void addLootTableToInventory(final ServerPlayerEntity player) {
		LootContext lootcontext = (new LootContext.Builder(player.getServerWorld()))
				.withParameter(LootParameters.THIS_ENTITY, player)
				.withParameter(LootParameters.ORIGIN, player.getPositionVec())
				.withRandom(player.getRNG()).withLuck(player.getLuck())
				.build(LootParameterSets.GIFT);
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
