package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

public record PermanentItemBehavior(ItemStack item, int interval) implements IGameBehavior {
	public static final MapCodec<PermanentItemBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(c -> c.item),
			Codec.INT.optionalFieldOf("interval", 5).forGetter(c -> c.interval)
	).apply(i, PermanentItemBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, player -> {
			if (game.participants().contains(player) && game.ticks() % interval == 0) {
				int currentCount = player.getInventory().countItem(item.getItem());
				int targetCount = item.getCount();
				if (currentCount < targetCount) {
					player.getInventory().add(item.copyWithCount(targetCount - currentCount));
				}
			}
		});
	}
}
