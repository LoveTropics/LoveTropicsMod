package com.lovetropics.minigames.common.content.survive_the_tide.biome;

import com.lovetropics.minigames.Constants;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

@Mod.EventBusSubscriber(modid = Constants.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class SurviveTheTideBiomes {
    public static final ResourceKey<Biome> SURVIVE_THE_TIDE_1 = createKey("survive_the_tide_1");
    public static final ResourceKey<Biome> SURVIVE_THE_TIDE_2 = createKey("survive_the_tide_2");

    private static final RegistrySetBuilder REGISTRY_SET = new RegistrySetBuilder()
            .add(Registries.BIOME, SurviveTheTideBiomes::bootstrap);

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        event.getGenerator().addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), REGISTRY_SET, Set.of(Constants.MODID)));
    }

    private static void bootstrap(final BootstapContext<Biome> context) {
        context.register(SURVIVE_THE_TIDE_1, createSurviveTheTide(1.5f, 1.25f));
        context.register(SURVIVE_THE_TIDE_2, createSurviveTheTide(2.0f, 0.0f));
    }

    private static Biome createSurviveTheTide(final float temperature, final float downfall) {
        final BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .fogColor(0xC0D8FF)
                .skyColor(0x0F331B)
                .waterColor(0x417251)
                .waterFogColor(0x0F331B);
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(temperature).downfall(downfall)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .specialEffects(effects.build())
                .build();
    }

    private static ResourceKey<Biome> createKey(String key) {
        return ResourceKey.create(Registries.BIOME, new ResourceLocation(Constants.MODID, key));
    }
}
