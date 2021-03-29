package com.lovetropics.minigames;

import com.google.common.base.Preconditions;
import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.block.LoveTropicsBlocks;
import com.lovetropics.minigames.common.block.TrashType;
import com.lovetropics.minigames.common.command.CommandMap;
import com.lovetropics.minigames.common.command.minigames.*;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.entity.DriftwoodRider;
import com.lovetropics.minigames.common.entity.MinigameEntities;
import com.lovetropics.minigames.common.item.MinigameItems;
import com.lovetropics.minigames.common.map.VoidChunkGenerator;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.network.LTNetwork;
import com.lovetropics.minigames.common.telemetry.Telemetry;
import com.mojang.brigadier.CommandDispatcher;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.NonNullLazyValue;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

@Mod(Constants.MODID)
public class LoveTropics {

    public static final Logger LOGGER = LogManager.getLogger(Constants.MODID);

    public static final ItemGroup LOVE_TROPICS_ITEM_GROUP = (new ItemGroup("love_tropics") {
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(LoveTropicsBlocks.TRASH.get(TrashType.COLA).get());
        }
    });

    private static NonNullLazyValue<Registrate> registrate = new NonNullLazyValue<>(() ->
    	Registrate.create(Constants.MODID)
    			  .itemGroup(() -> LOVE_TROPICS_ITEM_GROUP));

    @CapabilityInject(DriftwoodRider.class)
    private static Capability<DriftwoodRider> driftwoodRiderCap;

    public LoveTropics() {
    	// Compatible with all versions that match the semver (excluding the qualifier e.g. "-beta+42")
    	ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(LoveTropics::getCompatVersion, (s, v) -> LoveTropics.isCompatibleVersion(s)));

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // General mod setup
        modBus.addListener(this::setup);
        modBus.addListener(this::gatherData);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            // Client setup
            modBus.addListener(this::setupClient);
        });

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);

        modBus.addListener(ConfigLT::onLoad);
        modBus.addListener(ConfigLT::onFileChange);

        // Registry objects
        LoveTropicsBlocks.init();
        MinigameItems.init();
        MinigameEntities.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigLT.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigLT.SERVER_CONFIG);

        MinigameBehaviorTypes.MINIGAME_BEHAVIOURS_REGISTER.register(modBus);
    }

    private static final Pattern QUALIFIER = Pattern.compile("-\\w+\\+\\d+");
    public static String getCompatVersion() {
    	return getCompatVersion(ModList.get().getModContainerById(Constants.MODID).orElseThrow(IllegalStateException::new).getModInfo().getVersion().toString());
    }
    private static String getCompatVersion(String fullVersion) {
    	return QUALIFIER.matcher(fullVersion).replaceAll("");
    }
    public static boolean isCompatibleVersion(String version) {
    	return getCompatVersion().equals(getCompatVersion(version));
    }

    public static Registrate registrate() {
        return registrate.getValue();
    }

    @OnlyIn(Dist.CLIENT)
    private void setupClient(final FMLClientSetupEvent event) {
        ForgeConfig.CLIENT.alwaysSetupTerrainOffThread.set(true);
        ((ForgeConfigSpec)ObfuscationReflectionHelper.getPrivateValue(ForgeConfig.class, null, "clientSpec")).save();
    }

    private void setup(final FMLCommonSetupEvent event) {
        LTNetwork.register();

        VoidChunkGenerator.register();

        CapabilityManager.INSTANCE.register(DriftwoodRider.class, DriftwoodRider.STORAGE, () -> {
            throw new UnsupportedOperationException();
        });
    }

    private void onServerAboutToStart(final FMLServerAboutToStartEvent event) {
        Telemetry.INSTANCE.sendOpen();

        MinigameManager.init(event.getServer());

        // we register commands in the about-to-start event because the 'proper' starting event loads after datapacks,
        // causing functions to load incorrectly
        CommandDispatcher<CommandSource> dispatcher = event.getServer().getCommandManager().getDispatcher();
        CommandPollMinigame.register(dispatcher);
        CommandRegisterMinigame.register(dispatcher);
        CommandStartMinigame.register(dispatcher);
        CommandFinishMinigame.register(dispatcher);
        CommandCancelMinigame.register(dispatcher);
        CommandUnregisterMinigame.register(dispatcher);
        CommandStopPollingMinigame.register(dispatcher);
        CommandResetIslandChests.register(dispatcher);
        CommandScanArea.register(dispatcher);
        CommandMinigameControl.register(dispatcher);
        CommandMap.register(dispatcher);
        CommandMinigameDonate.register(dispatcher);
        CommandMinigamePackage.register(dispatcher);
    }

    private void onServerStopping(final FMLServerStoppingEvent event) {
        if (MinigameManager.getInstance().getActiveMinigame() != null) {
            MinigameManager.getInstance().cancel();
        }

        Telemetry.INSTANCE.sendClose();
    }

    private void gatherData(GatherDataEvent event) {
        registrate().addDataGenerator(ProviderType.LANG, prov -> {
            prov.add(LoveTropics.LOVE_TROPICS_ITEM_GROUP, "Love Tropics");

            // TODO move this into an enum
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED, "You've already registered for the current minigame!");
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_NOT_REGISTERED, "Minigame with that ID has not been registered: %s");
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_ID_INVALID, "A minigame with that ID doesn't exist!");
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_STARTED, "Another minigame is already in progress! Stop that one first before polling another.");
            prov.add(TropicraftLangKeys.COMMAND_ANOTHER_MINIGAME_POLLING, "Another minigame is already polling! Stop that one first before polling another.");
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_POLLING, "Minigame %s is polling. Type %s to get a chance to play!");
            prov.add(TropicraftLangKeys.COMMAND_SORRY_ALREADY_STARTED, "Sorry, the current minigame has already started! You can join as a spectator with /minigame spectate");
            prov.add(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING, "There is no minigame currently polling.");
            prov.add(TropicraftLangKeys.COMMAND_REGISTERED_FOR_MINIGAME, "You have registered for Minigame %s. When the minigame starts, random registered players will be picked to play. Please wait for hosts to start the minigame. You can continue to do what you were doing until then.");
            prov.add(TropicraftLangKeys.COMMAND_NOT_REGISTERED_FOR_MINIGAME, "You are not currently registered for any minigames.");
            prov.add(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, "You have unregistered for Minigame %s.");
            prov.add(TropicraftLangKeys.COMMAND_ENTITY_NOT_PLAYER, "Entity that attempted command is not player.");
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_POLLED, "Minigame successfully polled!");
            prov.add(TropicraftLangKeys.COMMAND_NOT_ENOUGH_PLAYERS, "There aren't enough players to start this minigame. It requires at least %s amount of players.");
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_STARTED, "You have started the minigame.");
            prov.add(TropicraftLangKeys.MINIGAME_SURVIVE_THE_TIDE_1, "Survive The Tide I");
            prov.add(TropicraftLangKeys.MINIGAME_SURVIVE_THE_TIDE_1_TEAMS, "Survive The Tide I (Teams)");
            prov.add(TropicraftLangKeys.MINIGAME_SURVIVE_THE_TIDE_2, "Survive The Tide II");
            prov.add(TropicraftLangKeys.MINIGAME_SURVIVE_THE_TIDE_2_TEAMS, "Survive The Tide II (Teams)");
            prov.add(TropicraftLangKeys.MINIGAME_SIGNATURE_RUN, "Signature Run");
            prov.add(TropicraftLangKeys.MINIGAME_TRASH_DIVE, "Trash Dive");
            prov.add(TropicraftLangKeys.MINIGAME_CONSERVATION_EXPLORATION, "Conservation Exploration");
            prov.add(TropicraftLangKeys.MINIGAME_VOLCANO_SPLEEF, "Volcano Spleef");
            prov.add(TropicraftLangKeys.MINIGAME_TREASURE_HUNT_X, "Treasure Hunt X");
            prov.add(TropicraftLangKeys.MINIGAME_BUILD_COMPETITION, "Build Competition");
            prov.add(TropicraftLangKeys.MINIGAME_TURTLE_RACE, "Turtle Race");
            prov.add(TropicraftLangKeys.MINIGAME_TURTLE_RACE_ARCADE, "Turtle Race (Arcade)");
            prov.add(TropicraftLangKeys.MINIGAME_FLYING_TURTLE_RACE, "Flying Turtle Race");
            prov.add(TropicraftLangKeys.MINIGAME_TURTLE_SPRINT, "Turtle Sprint");
            prov.add(TropicraftLangKeys.MINIGAME_ELYTRA_RACE, "Elytra Race");
            prov.add(TropicraftLangKeys.MINIGAME_ONE_MANS_TRASH, "One Man's Trash");

            prov.add(TropicraftLangKeys.COMMAND_NO_LONGER_ENOUGH_PLAYERS, "There are no longer enough players to start the minigame!");
            prov.add(TropicraftLangKeys.COMMAND_ENOUGH_PLAYERS, "There are now enough players to start the minigame!");
            prov.add(TropicraftLangKeys.COMMAND_NO_MINIGAME, "There is no currently running minigame to stop!");
            prov.add(TropicraftLangKeys.COMMAND_STOPPED_MINIGAME, "You have stopped the %s minigame.");
            prov.add(TropicraftLangKeys.COMMAND_FINISHED_MINIGAME, "The minigame %s has finished. If you were inside the minigame, you have been teleported back to your original position.");
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_STOPPED_POLLING, "An operator has stopped polling the minigame %s.");
            prov.add(TropicraftLangKeys.COMMAND_STOP_POLL, "You have successfully stopped the poll.");

            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH1, "Through the rising sea levels, the volatile and chaotic weather, and the struggle to survive, one player remains: %s.");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH2, "\nThose who have fallen have been swept away by the encroaching tides that engulf countless landmasses in this dire future.");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH3, "\nThe lone survivor of this island, %s, has won - but at what cost? The world is not what it once was, and they must survive in this new apocalyptic land.");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_FINISH4, "\nWhat would you do different next time? Together, we could stop this from becoming our future.");

            prov.add(TropicraftLangKeys.MINIGAME_FINISH, "The minigame will end in 10 seconds...");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO1, "The year...2050. Human-caused climate change has gone unmitigated and the human population has been forced to flee to higher ground.");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO2, "\nYour task, should you choose to accept it, which you have to because of climate change, is to survive the rising tides, unpredictable weather, and other players.");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO3, "\nBrave the conditions and defeat the others who are just trying to survive, like you. And remember...your resources are as limited as your time.");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO4, "\nSomeone else may have the tool or food you need to survive. What kind of person will you be when the world is falling apart?");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_INTRO5, "\nLet's see!");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_PVP_DISABLED, "NOTE: PvP is disabled for %s minutes! Go fetch resources before time runs out.");
            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_PVP_ENABLED, "WARNING: PVP HAS BEEN ENABLED! Beware of other players...");

            prov.add(TropicraftLangKeys.SURVIVE_THE_TIDE_DOWN_TO_TWO, "IT'S DOWN TO TWO PLAYERS! %s and %s are now head to head - who will triumph above these rising tides?");
        });
    }

    public static Capability<DriftwoodRider> driftwoodRiderCap() {
        Preconditions.checkNotNull(driftwoodRiderCap, "driftwood rider capability not initialized");
        return driftwoodRiderCap;
    }
}
