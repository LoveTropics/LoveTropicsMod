package com.lovetropics.minigames;

import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.block.LoveTropicsBlocks;
import com.lovetropics.minigames.common.block.TrashType;
import com.lovetropics.minigames.common.command.CommandMap;
import com.lovetropics.minigames.common.command.CommandReloadConfig;
import com.lovetropics.minigames.common.command.minigames.CommandAddConfigIceberg;
import com.lovetropics.minigames.common.command.minigames.CommandIslandSetStartPos;
import com.lovetropics.minigames.common.command.minigames.CommandMinigameControl;
import com.lovetropics.minigames.common.command.minigames.CommandPollMinigame;
import com.lovetropics.minigames.common.command.minigames.CommandRegisterMinigame;
import com.lovetropics.minigames.common.command.minigames.CommandResetIsland;
import com.lovetropics.minigames.common.command.minigames.CommandResetIslandChests;
import com.lovetropics.minigames.common.command.minigames.CommandSaveIsland;
import com.lovetropics.minigames.common.command.minigames.CommandStartMinigame;
import com.lovetropics.minigames.common.command.minigames.CommandStopMinigame;
import com.lovetropics.minigames.common.command.minigames.CommandStopPollingMinigame;
import com.lovetropics.minigames.common.command.minigames.CommandUnregisterMinigame;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.dimension.DimensionUtils;
import com.lovetropics.minigames.common.dimension.biome.TropicraftBiomes;
import com.lovetropics.minigames.common.item.MinigameItems;
import com.lovetropics.minigames.common.map.VoidChunkGenerator;
import com.lovetropics.minigames.common.map.workspace.MapWorkspaceDimension;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.config.MinigameConfigs;
import com.lovetropics.minigames.common.minigames.map.MinigameMapProviderTypes;
import com.lovetropics.minigames.common.network.LTNetwork;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public LoveTropics() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // General mod setup
        modBus.addListener(this::setup);
        modBus.addListener(this::gatherData);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            // Client setup
            modBus.addListener(this::setupClient);
        });

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);

        modBus.addListener(ConfigLT::onLoad);
        modBus.addListener(ConfigLT::onFileChange);

        // Registry objects
        LoveTropicsBlocks.init();
        MinigameItems.init();
        DimensionUtils.init();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigLT.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigLT.SERVER_CONFIG);

        VoidChunkGenerator.REGISTER.register(modBus);
        MapWorkspaceDimension.REGISTER.register(modBus);
        MinigameBehaviorTypes.MINIGAME_BEHAVIOURS_REGISTER.register(modBus);
        MinigameMapProviderTypes.REGISTER.register(modBus);
        TropicraftBiomes.BIOMES.register(modBus);
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
//        TODO TropicraftBiomes.addFeatures();

        LTNetwork.register();
    }

    private void onServerAboutToStart(final FMLServerAboutToStartEvent event) {
        MinigameConfigs.init(event.getServer());
    }

    private void onServerStarting(final FMLServerStartingEvent event) {
        MinigameManager.init(event.getServer());

        CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();
        CommandPollMinigame.register(dispatcher);
        CommandRegisterMinigame.register(dispatcher);
        CommandStartMinigame.register(dispatcher);
        CommandStopMinigame.register(dispatcher);
        CommandUnregisterMinigame.register(dispatcher);
        CommandStopPollingMinigame.register(dispatcher);
        CommandReloadConfig.register(dispatcher);
        CommandAddConfigIceberg.register(dispatcher);
        CommandResetIsland.register(dispatcher);
        CommandSaveIsland.register(dispatcher);
        CommandIslandSetStartPos.register(dispatcher);
        CommandResetIslandChests.register(dispatcher);
        CommandMinigameControl.register(dispatcher);
        CommandMap.register(dispatcher);
    }

    private void onServerStopping(final FMLServerStoppingEvent event) {
        if (MinigameManager.getInstance().getCurrentMinigame() != null) {
            MinigameManager.getInstance().finishCurrentMinigame();
        }
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
            prov.add(TropicraftLangKeys.COMMAND_SORRY_ALREADY_STARTED, "Sorry, the current minigame has already started!");
            prov.add(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING, "There is no minigame currently polling.");
            prov.add(TropicraftLangKeys.COMMAND_REGISTERED_FOR_MINIGAME, "You have registered for Minigame %s. When the minigame starts, random registered players will be picked to play. Please wait for hosts to start the minigame. You can continue to do what you were doing until then.");
            prov.add(TropicraftLangKeys.COMMAND_NOT_REGISTERED_FOR_MINIGAME, "You are not currently registered for any minigames.");
            prov.add(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, "You have unregistered for Minigame %s.");
            prov.add(TropicraftLangKeys.COMMAND_ENTITY_NOT_PLAYER, "Entity that attempted command is not player.");
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_POLLED, "Minigame successfully polled!");
            prov.add(TropicraftLangKeys.COMMAND_NOT_ENOUGH_PLAYERS, "There aren't enough players to start this minigame. It requires at least %s amount of players.");
            prov.add(TropicraftLangKeys.COMMAND_MINIGAME_STARTED, "You have started the minigame.");
            prov.add(TropicraftLangKeys.MINIGAME_SURVIVE_THE_TIDE, "Survive The Tide");
            prov.add(TropicraftLangKeys.MINIGAME_SIGNATURE_RUN, "Signature Run");
            prov.add(TropicraftLangKeys.MINIGAME_UNDERWATER_TRASH_HUNT, "Underwater Trash Hunt");
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
}
