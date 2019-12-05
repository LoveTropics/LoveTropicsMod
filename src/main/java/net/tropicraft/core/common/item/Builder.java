package net.tropicraft.core.common.item;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.tropicraft.LoveTropics;

public class Builder {
    
    public static Supplier<Item> item() {
        return item(getDefaultProperties());
    }
    
    public static Supplier<Item> item(Item.Properties properties) {
        return item(Item::new, properties);
    }
    
    public static <T> Supplier<T> item(Function<Item.Properties, T> ctor) {
        return item(ctor, getDefaultProperties());
    }
    
    public static <T> Supplier<T> item(Function<Item.Properties, T> ctor, Item.Properties properties) {
        return item(ctor, () -> properties);
    }
    
    public static <T> Supplier<T> item(Function<Item.Properties, T> ctor, Supplier<Item.Properties> properties) {
        return () -> ctor.apply(properties.get());
    }

    private static Item.Properties getDefaultProperties() {
        return new Item.Properties().group(LoveTropics.TROPICRAFT_ITEM_GROUP);
    }
}
