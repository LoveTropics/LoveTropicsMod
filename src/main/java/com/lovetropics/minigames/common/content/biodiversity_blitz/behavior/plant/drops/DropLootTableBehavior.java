package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.drops;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.LootTable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

public final class DropLootTableBehavior implements IGameBehavior {
	public static final Codec<DropLootTableBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ResourceLocation.CODEC.fieldOf("loot_table").forGetter(c -> c.lootTable)
	).apply(instance, DropLootTableBehavior::new));

	private final ResourceLocation lootTable;

	public DropLootTableBehavior(ResourceLocation lootTable) {
		this.lootTable = lootTable;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.BREAK, (player, plot, plant, pos) -> {
			LootTable lootTable = this.getLootTable(game.getServer());
			if (lootTable != null) {
				ServerWorld world = game.getWorld();

				LootContext context = this.buildLootContext(player, pos);
				for (ItemStack stack : lootTable.generate(context)) {
					Block.spawnAsEntity(world, pos, stack);
				}
			}
		});
	}

	private LootContext buildLootContext(ServerPlayerEntity player, BlockPos pos) {
		return new LootContext.Builder(player.getServerWorld())
				.withParameter(LootParameters.THIS_ENTITY, player)
				.withParameter(LootParameters.ORIGIN, Vector3d.copyCentered(pos))
				.withParameter(LootParameters.BLOCK_STATE, player.world.getBlockState(pos))
				.withParameter(LootParameters.TOOL, player.getActiveItemStack())
				.withRandom(player.getRNG())
				.withLuck(player.getLuck())
				.build(LootParameterSets.BLOCK);
	}

	@Nullable
	private LootTable getLootTable(MinecraftServer server) {
		if (this.lootTable != null) {
			return server.getLootTableManager().getLootTableFromLocation(this.lootTable);
		} else {
			return null;
		}
	}
}
