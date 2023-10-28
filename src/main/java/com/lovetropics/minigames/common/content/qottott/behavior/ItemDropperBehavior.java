package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import java.util.List;
import java.util.function.Supplier;

public record ItemDropperBehavior(Either<ItemStack, ResourceLocation> loot, String regionKey, IntProvider intervalTicks) implements IGameBehavior {
	public static final MapCodec<ItemDropperBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.either(MoreCodecs.ITEM_STACK, ResourceLocation.CODEC).fieldOf("loot").forGetter(ItemDropperBehavior::loot),
			Codec.STRING.fieldOf("region").forGetter(ItemDropperBehavior::regionKey),
			IntProvider.POSITIVE_CODEC.fieldOf("interval_ticks").forGetter(ItemDropperBehavior::intervalTicks)
	).apply(i, ItemDropperBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final Supplier<List<ItemStack>> lootProvider = createLootProvider(game);
		final List<Dropper> droppers = game.getMapRegions().get(regionKey).stream()
				.map(box -> new Dropper(box.center(), lootProvider))
				.toList();

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
		private final Vec3 position;
		private final Supplier<List<ItemStack>> lootProvider;
		private int dropInTicks;

		@Nullable
		private ItemEntity lastDroppedItem;

		private Dropper(final Vec3 position, final Supplier<List<ItemStack>> lootProvider) {
			this.position = position;
			this.lootProvider = lootProvider;
		}

		public void tick(final IGamePhase game) {
			if (lastDroppedItem != null && lastDroppedItem.isAlive()) {
				return;
			}

			if (dropInTicks > 0) {
				dropInTicks--;
				return;
			}

			final ServerLevel level = game.getLevel();
			for (final ItemStack item : lootProvider.get()) {
				final ItemEntity itemEntity = new ItemEntity(level, position.x, position.y, position.z, item, 0.0, 0.0, 0.0);
				level.addFreshEntity(itemEntity);
				lastDroppedItem = itemEntity;
			}

			dropInTicks = intervalTicks.sample(game.getRandom());
		}
	}
}
