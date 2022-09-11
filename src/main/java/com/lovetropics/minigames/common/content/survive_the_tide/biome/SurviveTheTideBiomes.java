package com.lovetropics.minigames.common.content.survive_the_tide.biome;

import com.lovetropics.minigames.Constants;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class SurviveTheTideBiomes {
    public static final DeferredRegister<Biome> REGISTER = DeferredRegister.create(Registry.BIOME_REGISTRY, Constants.MODID);

    public static final RegistryObject<Biome> SURVIVE_THE_TIDE_1 = REGISTER.register("survive_the_tide_1", SurviveTheTideBiomes::createSurviveTheTide1);
    public static final RegistryObject<Biome> SURVIVE_THE_TIDE_2 = REGISTER.register("survive_the_tide_2", SurviveTheTideBiomes::createSurviveTheTide2);

    private static Biome createSurviveTheTide1() {
        return createSurviveTheTide(1.5f, 1.25f);
    }

    private static Biome createSurviveTheTide2() {
        return createSurviveTheTide(2.0f, 0.0f);
    }

    private static Biome createSurviveTheTide(final float temperature, final float downfall) {
        final BiomeSpecialEffects.Builder effects = new BiomeSpecialEffects.Builder()
                .fogColor(0xC0D8FF)
                .skyColor(0x0F331B)
                .waterColor(0x417251)
                .waterFogColor(0x0F331B);

        return new Biome.BiomeBuilder()
                .precipitation(Biome.Precipitation.RAIN)
                .temperature(temperature).downfall(downfall)
                .biomeCategory(Biome.BiomeCategory.OCEAN)
                .generationSettings(new BiomeGenerationSettings.Builder().build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build())
                .specialEffects(effects.build())
                .build();
    }
}
