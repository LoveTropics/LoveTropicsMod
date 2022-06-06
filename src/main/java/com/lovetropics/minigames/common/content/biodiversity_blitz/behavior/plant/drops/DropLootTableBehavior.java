package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.drops;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

public record DropLootTableBehavior(ResourceLocation lootTable) implements IGameBehavior {
	public static final Codec<DropLootTableBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			ResourceLocation.CODEC.fieldOf("loot_table").forGetter(c -> c.lootTable)
	).apply(i, DropLootTableBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.BREAK, (player, plot, plant, pos) -> {
			LootTable lootTable = this.getLootTable(game.getServer());
			if (lootTable != null) {
				ServerLevel world = game.getWorld();

				LootContext context = this.buildLootContext(player, pos);
				for (ItemStack stack : lootTable.getRandomItems(context)) {
					Block.popResource(world, pos, stack);
				}
			}
		});
	}

	private LootContext buildLootContext(ServerPlayer player, BlockPos pos) {
		return new LootContext.Builder(player.getLevel())
				.withParameter(LootContextParams.THIS_ENTITY, player)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
				.withParameter(LootContextParams.BLOCK_STATE, player.level.getBlockState(pos))
				.withParameter(LootContextParams.TOOL, player.getUseItem())
				.withRandom(player.getRandom())
				.withLuck(player.getLuck())
				.create(LootContextParamSets.BLOCK);
	}

	@Nullable
	private LootTable getLootTable(MinecraftServer server) {
		if (this.lootTable != null) {
			return server.getLootTables().get(this.lootTable);
		} else {
			return null;
		}
	}
}
