package com.lovetropics.minigames.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;

@EventBusSubscriber
public class ConfigLT {

    private static final Builder CLIENT_BUILDER = new Builder();

    private static final Builder COMMON_BUILDER = new Builder();

    public static final CategoryGeneral GENERAL = new CategoryGeneral();
    public static final CategoryTechStack TECH_STACK = new CategoryTechStack();

    public static final class CategoryGeneral {

        public final DoubleValue Precipitation_Particle_effect_rate;

        public final BooleanValue UseCrouch;

        public final IntValue carePackageDelay;

        public final IntValue sabotagePackageDelay;

        public final IntValue chatEventDelay;

        private CategoryGeneral() {
            CLIENT_BUILDER.comment("General mod settings").push("general");

            Precipitation_Particle_effect_rate = CLIENT_BUILDER
                    .defineInRange("Precipitation_Particle_effect_rate", 0.7D, 0D, 1D);

            UseCrouch = CLIENT_BUILDER.comment("Enable crawling anywhere by pressing the sprint key while holding down the sneak key")
                    .define("UseCrawl", true);

            carePackageDelay = COMMON_BUILDER
                .comment("Delay (in seconds) between care packages")
                .defineInRange("carePackageDelay", 0, 0, 99999);

            sabotagePackageDelay = COMMON_BUILDER
                .comment("Delay (in seconds) between sabotage packages")
                .defineInRange("sabotagePackageDelay", 0, 0, 99999);

            chatEventDelay = COMMON_BUILDER
                .comment("Delay (in seconds) between chat events")
                .defineInRange("chatEventDelay", 0, 0, 99999);

            CLIENT_BUILDER.pop();
        }
    }

    public static final class CategoryTechStack {

        public final ConfigValue<String> baseUrl;
        public final ConfigValue<String> resultsEndpoint;
        public final ConfigValue<String> minigameStartEndpoint;
        public final ConfigValue<String> minigameEndEndpoint;
        public final ConfigValue<String> minigameTerminatedEndpoint;
        public final ConfigValue<String> minigamePlayerUpdateEndpoint;
        public final ConfigValue<String> packageDeliveredEndpoint;
        public final ConfigValue<String> pendingActionsEndpoint;
        public final IntValue port;
        public final ConfigValue<String> authToken;
        public final ConfigValue<String> webSocketUrl;
        public final IntValue webSocketPort;

        private CategoryTechStack() {
            COMMON_BUILDER.comment("Used for the LoveTropics charity drive.").push("techStack");

            baseUrl = COMMON_BUILDER
                    .comment("Base URL to use ")
                    .define("baseUrl", "http://localhost");
            resultsEndpoint = COMMON_BUILDER
                    .comment("Endpoint used to upload minigame results")
                    .define("resultsEndpoint", "minigame/result");
            minigameStartEndpoint = COMMON_BUILDER
                    .comment("Endpoint used when a minigame is started")
                    .define("minigameStartEndpoint", "minigame/start");
            minigameEndEndpoint = COMMON_BUILDER
                    .comment("Endpoint used when a minigame is successfully completed")
                    .define("minigameEndEndpoint", "minigame/end");
            minigameTerminatedEndpoint = COMMON_BUILDER
                    .comment("Endpoint used when a minigame is terminated early")
                    .define("minigameTerminatedEndpoint", "minigame/terminated");
            minigamePlayerUpdateEndpoint = COMMON_BUILDER
                    .comment("Endpoint used to update a player's status during a minigame")
                    .define("minigamePlayerUpdateEndpoint", "minigame/playerupdate");
            packageDeliveredEndpoint = COMMON_BUILDER
                    .comment("Endpoint used to notify the backend a care or sabotage package was received")
                    .define("packageDeliveredEndpoint", "minigame/packagedelivered");
            port = COMMON_BUILDER
                    .comment("Port number to use when POSTing data")
                    .defineInRange("port", 0, 0, 99999);
            authToken = COMMON_BUILDER
                    .comment("Auth token used to authenticate with the tech stack")
                    .define("authToken", "");
            webSocketPort = COMMON_BUILDER
                    .comment("Port number for the tech stack web socket")
                    .defineInRange("webSocketPort", 0, 0, 99999);
            webSocketUrl = COMMON_BUILDER
                    .comment("URL the web socket is running on")
                    .define("webSocketUrl", "localhost");
            pendingActionsEndpoint = COMMON_BUILDER
                    .comment("URL to receive any care/sabotage packages or chat events that were triggered but never acknowledged by the mod (maybe due to a premature shutdown)")
                    .define("pendingActionsEndpoint", "/minigame/pendingactions");
            COMMON_BUILDER.pop();
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
