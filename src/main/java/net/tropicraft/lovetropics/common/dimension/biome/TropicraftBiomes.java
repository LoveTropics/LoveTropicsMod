package net.tropicraft.lovetropics.common.dimension.biome;

import java.util.function.Supplier;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.OceanBiome;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tropicraft.lovetropics.Constants;

public class TropicraftBiomes {
    
    public static final DeferredRegister<Biome> BIOMES = new DeferredRegister<>(ForgeRegistries.BIOMES, Constants.MODID);

    public static final RegistryObject<Biome> SURVIVE_THE_TIDE = register("island_royale", OceanBiome::new); // TODO
   // public static final Biome TROPICS_LAKE = new TropicsLakeBiome();

    private static final <T extends Biome> RegistryObject<T> register(final String name, final Supplier<T> sup) {
        return BIOMES.register(name, sup);
    }
    
    public static void addFeatures() {
        for (Biome b : ForgeRegistries.BIOMES.getValues()) {
        }
    }
}
