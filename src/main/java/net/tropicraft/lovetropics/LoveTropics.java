package net.tropicraft.lovetropics;

import net.minecraft.data.DataGenerator;
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
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.tropicraft.lovetropics.client.data.TropicraftLangProvider;
import net.tropicraft.lovetropics.common.block.LoveTropicsBlocks;
import net.tropicraft.lovetropics.common.command.CommandDonation;
import net.tropicraft.lovetropics.common.command.CommandReloadConfig;
import net.tropicraft.lovetropics.common.command.minigames.CommandAddConfigIceberg;
import net.tropicraft.lovetropics.common.command.minigames.CommandIslandSetStartPos;
import net.tropicraft.lovetropics.common.command.minigames.CommandPollMinigame;
import net.tropicraft.lovetropics.common.command.minigames.CommandRegisterMinigame;
import net.tropicraft.lovetropics.common.command.minigames.CommandResetIsland;
import net.tropicraft.lovetropics.common.command.minigames.CommandResetIslandChests;
import net.tropicraft.lovetropics.common.command.minigames.CommandSaveIsland;
import net.tropicraft.lovetropics.common.command.minigames.CommandStartMinigame;
import net.tropicraft.lovetropics.common.command.minigames.CommandStopMinigame;
import net.tropicraft.lovetropics.common.command.minigames.CommandStopPollingMinigame;
import net.tropicraft.lovetropics.common.command.minigames.CommandUnregisterMinigame;
import net.tropicraft.lovetropics.common.config.ConfigLT;
import net.tropicraft.lovetropics.common.dimension.TropicraftWorldUtils;
import net.tropicraft.lovetropics.common.dimension.biome.TropicraftBiomes;
import net.tropicraft.lovetropics.common.item.MinigameItems;
import net.tropicraft.lovetropics.common.minigames.MinigameManager;

@Mod(Constants.MODID)
public class LoveTropics {

    public static final ItemGroup LOVE_TROPICS_ITEM_GROUP = (new ItemGroup("love_tropics") {
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(LoveTropicsBlocks.DONATION.get());
        }
    });

    public LoveTropics() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        
        // General mod setup
        modBus.addListener(this::setup);
        modBus.addListener(this::gatherData);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            // Client setup
            modBus.addListener(this::setupClient);
        });
        
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigLT::onLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigLT::onFileChange);

        // Registry objects
        LoveTropicsBlocks.init();
        MinigameItems.init();
        TropicraftBiomes.BIOMES.register(modBus);
        TropicraftWorldUtils.DIMENSIONS.register(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigLT.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigLT.SERVER_CONFIG);
    }
    
    @OnlyIn(Dist.CLIENT)
    private void setupClient(final FMLClientSetupEvent event) {
        ForgeConfig.CLIENT.alwaysSetupTerrainOffThread.set(true);
        ((ForgeConfigSpec)ObfuscationReflectionHelper.getPrivateValue(ForgeConfig.class, null, "clientSpec")).save();
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        TropicraftBiomes.addFeatures();
    }
    
    private void onServerStarting(final FMLServerStartingEvent event) {
        MinigameManager.init(event.getServer());

        CommandPollMinigame.register(event.getServer().getCommandManager().getDispatcher());
        CommandRegisterMinigame.register(event.getServer().getCommandManager().getDispatcher());
        CommandStartMinigame.register(event.getServer().getCommandManager().getDispatcher());
        CommandStopMinigame.register(event.getServer().getCommandManager().getDispatcher());
        CommandUnregisterMinigame.register(event.getServer().getCommandManager().getDispatcher());
        CommandStopPollingMinigame.register(event.getServer().getCommandManager().getDispatcher());
        CommandReloadConfig.register(event.getServer().getCommandManager().getDispatcher());
        CommandDonation.register(event.getServer().getCommandManager().getDispatcher());
        CommandAddConfigIceberg.register(event.getServer().getCommandManager().getDispatcher());
        CommandResetIsland.register(event.getServer().getCommandManager().getDispatcher());
        CommandSaveIsland.register(event.getServer().getCommandManager().getDispatcher());
        CommandIslandSetStartPos.register(event.getServer().getCommandManager().getDispatcher());
        CommandResetIslandChests.register(event.getServer().getCommandManager().getDispatcher());
    }

    private void onServerStopping(final FMLServerStoppingEvent event) {
        if (MinigameManager.getInstance().getCurrentMinigame() != null) {
            MinigameManager.getInstance().finishCurrentMinigame();
        }
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator gen = event.getGenerator();

        if (event.includeClient()) {
            gen.addProvider(new TropicraftLangProvider(gen));
        }
        if (event.includeServer()) {
        }
    }
}
