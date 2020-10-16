package com.lovetropics.minigames.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber
public class ConfigLT {

    private static final Builder CLIENT_BUILDER = new Builder();

    private static final Builder COMMON_BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();

    public static final class CategoryGeneral {

        public final DoubleValue Precipitation_Particle_effect_rate;

        public final BooleanValue UseCrouch;

        private CategoryGeneral() {
            CLIENT_BUILDER.comment("General mod settings").push("general");

            Precipitation_Particle_effect_rate = CLIENT_BUILDER
                    .defineInRange("Precipitation_Particle_effect_rate", 0.7D, 0D, 1D);

            UseCrouch = CLIENT_BUILDER.comment("Enable crawling anywhere by pressing the sprint key while holding down the sneak key")
                    .define("UseCrawl", true);

            CLIENT_BUILDER.pop();
        }
    }

    public static final ForgeConfigSpec CLIENT_CONFIG = CLIENT_BUILDER.build();
    public static final ForgeConfigSpec SERVER_CONFIG = COMMON_BUILDER.build();

    public static void onLoad(final ModConfig.Loading configEvent) {
    }

    /**
     * values used during runtime that require processing from disk
     */
    public static void onFileChange(final ModConfig.Reloading configEvent) {
        //System.out.println("file changed!" + configEvent.toString());
    }
}
