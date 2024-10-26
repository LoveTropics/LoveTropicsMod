package com.lovetropics.minigames.common.content.crafting_bee;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.function.Supplier;

public class CraftingBee {
    private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

    public static final GameBehaviorEntry<CraftingBeeBehavior> CRAFTING_BEE = REGISTRATE.object("crafting_bee")
            .behavior(CraftingBeeBehavior.CODEC)
            .register();

    public static final Supplier<DataComponentType<ItemContainerContents>> CRAFTED_USING = REGISTRATE.simple(
            "crafted_using",
            Registries.DATA_COMPONENT_TYPE,
            () -> DataComponentType.<ItemContainerContents>builder()
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC)
                    .persistent(ItemContainerContents.CODEC)
                    .cacheEncoding()
                    .build()
    );

    public static void init() {
    }
}
