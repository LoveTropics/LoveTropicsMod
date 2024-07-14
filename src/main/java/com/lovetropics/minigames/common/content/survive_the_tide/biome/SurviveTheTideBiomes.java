package com.lovetropics.minigames.common.content.survive_the_tide.biome;

import com.lovetropics.minigames.LoveTropics;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@EventBusSubscriber(modid = LoveTropics.ID, bus = EventBusSubscriber.Bus.MOD)
public final class SurviveTheTideBiomes {
    public static final ResourceKey<Biome> SURVIVE_THE_TIDE_1 = createKey("survive_the_tide_1");
    public static final ResourceKey<Biome> SURVIVE_THE_TIDE_2 = createKey("survive_the_tide_2");
    public static final ResourceKey<Biome> SURVIVE_THE_TIDE_4_RUSTY = createKey("survive_the_tide_4_rusty");
    public static final ResourceKey<Biome> SURVIVE_THE_TIDE_4_GREEN = createKey("survive_the_tide_4_green");

    private static final RegistrySetBuilder REGISTRY_SET = new RegistrySetBuilder()
            .add(Registries.BIOME, SurviveTheTideBiomes::bootstrap);

    @SubscribeEvent
    public static void gatherData(final GatherDataEvent event) {
        PackOutput output = event.getGenerator().getPackOutput();
        event.getGenerator().addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), REGISTRY_SET, Set.of(LoveTropics.ID)));
    }

    private static void bootstrap(final BootstrapContext<Biome> context) {
        context.register(SURVIVE_THE_TIDE_1, createSurviveTheTide(1.5f, 1.25f, 0x417251, 0x0F331B, 0xC0D8FF));
        context.register(SURVIVE_THE_TIDE_2, createSurviveTheTide(2.0f, 0.0f, 0x417251, 0x0F331B, 0xC0D8FF));
        context.register(SURVIVE_THE_TIDE_4_RUSTY, createSurviveTheTide(1.5f, 1.25f, 0x69422C, 0x464242, 0x464242));
        context.register(SURVIVE_THE_TIDE_4_GREEN, createSurviveTheTide(1.5f, 1.25f, 0x4A422C, 0x464242, 0x464242));
    }

    private static Biome createSurviveTheTide(final float temperature, final float downfall, int waterColor, int skyColor, int fogColor) {
        final BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .fogColor(fogColor)
                .skyColor(skyColor)
                .waterColor(waterColor)
                .waterFogColor(skyColor);
        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(temperature).downfall(downfall)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(MobSpawnSettings.EMPTY)
                .specialEffects(effects.build())
                .build();
    }

    private static ResourceKey<Biome> createKey(String key) {
        return ResourceKey.create(Registries.BIOME, LoveTropics.location(key));
    }
}
