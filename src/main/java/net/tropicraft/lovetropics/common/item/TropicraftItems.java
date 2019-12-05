package net.tropicraft.lovetropics.common.item;

import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tropicraft.lovetropics.Constants;
import net.tropicraft.lovetropics.LoveTropics;
import net.tropicraft.lovetropics.common.item.minigame.AcidRepellentUmbrellaItem;
import net.tropicraft.lovetropics.common.item.minigame.SuperSunscreenItem;

public class TropicraftItems {
    
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<>(ForgeRegistries.ITEMS, Constants.MODID);
 
    public static final RegistryObject<Item> SUPER_SUNSCREEN = register("super_sunscreen", () -> new SuperSunscreenItem(new Item.Properties().group(LoveTropics.TROPICRAFT_ITEM_GROUP)));
    public static final RegistryObject<Item> ACID_REPELLENT_UMBRELLA = register("acid_repellent_umbrella", () -> new AcidRepellentUmbrellaItem(new Item.Properties().group(LoveTropics.TROPICRAFT_ITEM_GROUP)));

    private static <T extends Item> RegistryObject<T> register(final String name, final Supplier<T> sup) {
        return ITEMS.register(name, sup);
    }
}
