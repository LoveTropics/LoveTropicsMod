package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

public class PermanentItemBehavior implements IGameBehavior {
	public static final Codec<PermanentItemBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ITEM.fieldOf("item").forGetter(c -> c.item),
				Codec.INT.fieldOf("count").forGetter(c -> c.count),
				Codec.INT.optionalFieldOf("interval", 5).forGetter(c -> c.interval)
		).apply(instance, PermanentItemBehavior::new);
	});

	private final Item item;
	private final int count;
	private final int interval;

	public PermanentItemBehavior(Item item, int count, int interval) {
		this.item = item;
		this.count = count;
		this.interval = interval;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, player -> {
			if (game.getParticipants().contains(player) && game.ticks() % interval == 0) {
				int currentCount = player.inventory.count(item);
				if (currentCount < this.count) {
					player.addItemStackToInventory(new ItemStack(item, this.count - currentCount));
				}
			}
		});
	}
}
