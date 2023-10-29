package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public record ItemDropperBehavior(Either<ItemStack, ResourceLocation> loot, String regionKey, boolean combined, IntProvider intervalTicks, Optional<GameActionList<Void>> announcement) implements IGameBehavior {
	public static final MapCodec<ItemDropperBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.either(MoreCodecs.ITEM_STACK, ResourceLocation.CODEC).fieldOf("loot").forGetter(ItemDropperBehavior::loot),
			Codec.STRING.fieldOf("region").forGetter(ItemDropperBehavior::regionKey),
			Codec.BOOL.optionalFieldOf("combined", false).forGetter(ItemDropperBehavior::combined),
			IntProvider.POSITIVE_CODEC.fieldOf("interval_ticks").forGetter(ItemDropperBehavior::intervalTicks),
			GameActionList.VOID_CODEC.optionalFieldOf("announcement").forGetter(ItemDropperBehavior::announcement)
	).apply(i, ItemDropperBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final Collection<BlockBox> regions = game.getMapRegions().get(regionKey);
		if (regions.isEmpty()) {
			return;
		}

		announcement.ifPresent(action -> action.register(game, events));

		final Supplier<List<ItemStack>> lootProvider = createLootProvider(game);
		final List<Dropper> droppers;
		if (combined) {
			droppers = List.of(new Dropper(regions.stream().map(BlockBox::center).toList(), lootProvider));
		} else {
			droppers = regions.stream().map(box -> new Dropper(List.of(box.center()), lootProvider)).toList();
		}

		events.listen(GamePhaseEvents.START, () -> {
			for (final Dropper dropper : droppers) {
				dropper.resetDelay(game);
			}
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			for (final Dropper dropper : droppers) {
				dropper.tick(game);
			}
		});
	}

	private Supplier<List<ItemStack>> createLootProvider(final IGamePhase game) {
		return loot.map(
				stack -> () -> List.of(stack.copy()),
				tableId -> {
					final LootTable lootTable = game.getServer().getLootData().getLootTable(tableId);
					final LootParams params = new LootParams.Builder(game.getLevel()).create(LootContextParamSets.EMPTY);
					return () -> lootTable.getRandomItems(params);
				}
		);
	}

	private class Dropper {
		private final List<Vec3> positions;
		private final Supplier<List<ItemStack>> lootProvider;
		private int dropInTicks;

		@Nullable
		private ItemEntity lastDroppedItem;

		private Dropper(final List<Vec3> positions, final Supplier<List<ItemStack>> lootProvider) {
			this.positions = positions;
			this.lootProvider = lootProvider;
		}

		public void tick(final IGamePhase game) {
			if (lastDroppedItem != null && lastDroppedItem.isAlive()) {
				return;
			}

			if (dropInTicks == 0) {
				resetDelay(game);
				Util.getRandomSafe(lootProvider.get(), game.getRandom()).ifPresent(item -> spawnItem(game, item));
			} else {
				dropInTicks--;
			}
		}

		private void resetDelay(final IGamePhase game) {
			dropInTicks = intervalTicks.sample(game.getRandom());
		}

		private void spawnItem(final IGamePhase game, final ItemStack item) {
			final Vec3 position = Util.getRandom(positions, game.getRandom());

			final ServerLevel level = game.getLevel();
			final ItemEntity itemEntity = new ItemEntity(level, position.x, position.y, position.z, item, 0.0, 0.1, 0.0);
			level.addFreshEntity(itemEntity);
			lastDroppedItem = itemEntity;

			announcement.ifPresent(action -> action.apply(game, GameActionContext.builder().set(GameActionParameter.ITEM, item).build()));
		}
	}
}
