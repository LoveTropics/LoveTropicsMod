package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record PermanentItemBehavior(Item item, int count, int interval) implements IGameBehavior {
	public static final MapCodec<PermanentItemBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(c -> c.item),
			Codec.INT.fieldOf("count").forGetter(c -> c.count),
			Codec.INT.optionalFieldOf("interval", 5).forGetter(c -> c.interval)
	).apply(i, PermanentItemBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, player -> {
			if (game.getParticipants().contains(player) && game.ticks() % interval == 0) {
				int currentCount = player.getInventory().countItem(item);
				if (currentCount < this.count) {
					player.getInventory().add(new ItemStack(item, this.count - currentCount));
				}
			}
		});
	}
}
