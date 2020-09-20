package net.tropicraft.lovetropics.common.minigames.dimensions;

import net.minecraft.world.gen.ChunkGeneratorType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.tropicraft.lovetropics.Constants;
import net.tropicraft.lovetropics.common.dimension.TropicraftChunkGenerator;
import net.tropicraft.lovetropics.common.dimension.TropicraftGeneratorSettings;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class TropicraftChunkGeneratorTypes {

    public static final DeferredRegister<ChunkGeneratorType<?, ?>> CHUNK_GENERATOR_TYPES = new DeferredRegister<>(ForgeRegistries.CHUNK_GENERATOR_TYPES, Constants.MODID);

    public static final RegistryObject<ChunkGeneratorType<TropicraftGeneratorSettings, TropicraftChunkGenerator>> TROPICS = register(
            "tropicraft_chunk_generator_type", () -> new ChunkGeneratorType<>(TropicraftChunkGenerator::new, true, TropicraftGeneratorSettings::new));

    private static <T extends ChunkGeneratorType<?, ?>> RegistryObject<T> register(final String name, final Supplier<T> sup) {
        return CHUNK_GENERATOR_TYPES.register(name, sup);
    }
}