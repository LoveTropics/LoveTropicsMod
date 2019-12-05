package net.tropicraft.lovetropics;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.types.templates.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.tropicraft.lovetropics.client.BasicColorHandler;
import net.tropicraft.lovetropics.client.data.TropicraftBlockstateProvider;
import net.tropicraft.lovetropics.client.data.TropicraftItemModelProvider;
import net.tropicraft.lovetropics.client.data.TropicraftLangProvider;
import net.tropicraft.lovetropics.client.entity.render.*;
import net.tropicraft.lovetropics.client.tileentity.BambooChestRenderer;
import net.tropicraft.lovetropics.client.tileentity.DrinkMixerRenderer;
import net.tropicraft.lovetropics.client.tileentity.SifterRenderer;
import net.tropicraft.lovetropics.common.block.TropicraftBlocks;
import net.tropicraft.lovetropics.common.block.TropicraftFlower;
import net.tropicraft.lovetropics.common.block.tileentity.BambooChestTileEntity;
import net.tropicraft.lovetropics.common.block.tileentity.DrinkMixerTileEntity;
import net.tropicraft.lovetropics.common.block.tileentity.SifterTileEntity;
import net.tropicraft.lovetropics.common.block.tileentity.TropicraftTileEntityTypes;
import net.tropicraft.lovetropics.common.command.CommandDonation;
import net.tropicraft.lovetropics.common.command.CommandReloadConfig;
import net.tropicraft.lovetropics.common.command.CommandTropicsTeleport;
import net.tropicraft.lovetropics.common.command.minigames.*;
import net.tropicraft.lovetropics.common.config.ConfigLT;
import net.tropicraft.lovetropics.common.data.TropicraftBlockTagsProvider;
import net.tropicraft.lovetropics.common.data.TropicraftItemTagsProvider;
import net.tropicraft.lovetropics.common.data.TropicraftLootTableProvider;
import net.tropicraft.lovetropics.common.data.TropicraftRecipeProvider;
import net.tropicraft.lovetropics.common.dimension.TropicraftWorldUtils;
import net.tropicraft.lovetropics.common.dimension.biome.TropicraftBiomeProviderTypes;
import net.tropicraft.lovetropics.common.dimension.biome.TropicraftBiomes;
import net.tropicraft.lovetropics.common.dimension.carver.TropicraftCarvers;
import net.tropicraft.lovetropics.common.dimension.chunk.TropicraftChunkGeneratorTypes;
import net.tropicraft.lovetropics.common.dimension.feature.TropicraftFeatures;
import net.tropicraft.lovetropics.common.entity.BambooItemFrame;
import net.tropicraft.lovetropics.common.entity.SeaTurtleEntity;
import net.tropicraft.lovetropics.common.entity.TropicraftEntities;
import net.tropicraft.lovetropics.common.entity.hostile.TropiSkellyEntity;
import net.tropicraft.lovetropics.common.entity.neutral.EIHEntity;
import net.tropicraft.lovetropics.common.entity.neutral.IguanaEntity;
import net.tropicraft.lovetropics.common.entity.neutral.TreeFrogEntity;
import net.tropicraft.lovetropics.common.entity.passive.EntityKoaHunter;
import net.tropicraft.lovetropics.common.entity.passive.FailgullEntity;
import net.tropicraft.lovetropics.common.entity.passive.TropiCreeperEntity;
import net.tropicraft.lovetropics.common.entity.placeable.BeachFloatEntity;
import net.tropicraft.lovetropics.common.entity.placeable.ChairEntity;
import net.tropicraft.lovetropics.common.entity.placeable.UmbrellaEntity;
import net.tropicraft.lovetropics.common.entity.placeable.WallItemEntity;
import net.tropicraft.lovetropics.common.entity.projectile.PoisonBlotEntity;
import net.tropicraft.lovetropics.common.entity.underdasea.MarlinEntity;
import net.tropicraft.lovetropics.common.entity.underdasea.SeahorseEntity;
import net.tropicraft.lovetropics.common.entity.underdasea.TropicraftDolphinEntity;
import net.tropicraft.lovetropics.common.item.IColoredItem;
import net.tropicraft.lovetropics.common.item.TropicraftItems;
import net.tropicraft.lovetropics.common.minigames.MinigameManager;
import net.tropicraft.lovetropics.common.network.TropicraftPackets;

@Mod(Constants.MODID)
public class LoveTropics {
    public static final ItemGroup TROPICRAFT_ITEM_GROUP = (new ItemGroup("tropicraft") {
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(TropicraftFlower.RED_ANTHURIUM.get());
        }
    });

    public static final ItemGroup LOVE_TROPICS_ITEM_GROUP = (new ItemGroup("love_tropics") {
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(TropicraftBlocks.SMALL_BONGO_DRUM.get());
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
            modBus.addListener(this::registerItemColors);
        });
        
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigLT::onLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(ConfigLT::onFileChange);

        // Registry objects
        TropicraftBlocks.BLOCKS.register(modBus);
        TropicraftItems.ITEMS.register(modBus);
        TropicraftTileEntityTypes.TILE_ENTITIES.register(modBus);
        TropicraftEntities.ENTITIES.register(modBus);
        TropicraftBiomes.BIOMES.register(modBus);
        TropicraftBiomeProviderTypes.BIOME_PROVIDER_TYPES.register(modBus);
        TropicraftWorldUtils.DIMENSIONS.register(modBus);
        TropicraftCarvers.CARVERS.register(modBus);
        TropicraftFeatures.FEATURES.register(modBus);
        TropicraftChunkGeneratorTypes.CHUNK_GENERATOR_TYPES.register(modBus);

        // Hack in our item frame models the way vanilla does
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            StateContainer<Block, BlockState> frameState = new StateContainer.Builder<Block, BlockState>(Blocks.AIR).add(BooleanProperty.create("map")).create(BlockState::new);
    
            ModelBakery.STATE_CONTAINER_OVERRIDES = ImmutableMap.<ResourceLocation, StateContainer<Block, BlockState>>builder()
                    .putAll(ModelBakery.STATE_CONTAINER_OVERRIDES)
                    .put(BambooItemFrameRenderer.LOCATION_BLOCK, frameState)
                    .build();
        });

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigLT.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigLT.SERVER_CONFIG);
    }
    
    @OnlyIn(Dist.CLIENT)
    private void setupClient(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityKoaHunter.class, RenderKoaMan::new);
        RenderingRegistry.registerEntityRenderingHandler(TropiCreeperEntity.class, TropiCreeperRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(IguanaEntity.class, IguanaRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(UmbrellaEntity.class, UmbrellaRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ChairEntity.class, ChairRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(BeachFloatEntity.class, BeachFloatRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(TropiSkellyEntity.class, TropiSkellyRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(EIHEntity.class, EIHRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(WallItemEntity.class, RenderWallItemEntity::new);
        RenderingRegistry.registerEntityRenderingHandler(BambooItemFrame.class, BambooItemFrameRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SeaTurtleEntity.class, RenderSeaTurtle::new);
        RenderingRegistry.registerEntityRenderingHandler(MarlinEntity.class, MarlinRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(FailgullEntity.class, FailgullRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(TropicraftDolphinEntity.class, TropicraftDolphinRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SeahorseEntity.class, SeahorseRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(TreeFrogEntity.class, TreeFrogRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(PoisonBlotEntity.class, PoisonBlotRenderer::new);

        ClientRegistry.bindTileEntitySpecialRenderer(BambooChestTileEntity.class, new BambooChestRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(SifterTileEntity.class, new SifterRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(DrinkMixerTileEntity.class, new DrinkMixerRenderer());
        
        ForgeConfig.CLIENT.alwaysSetupTerrainOffThread.set(true);
        ((ForgeConfigSpec)ObfuscationReflectionHelper.getPrivateValue(ForgeConfig.class, null, "clientSpec")).save();
    }
    
    @OnlyIn(Dist.CLIENT)
    private void registerItemColors(ColorHandlerEvent.Item evt) {
        BasicColorHandler basic = new BasicColorHandler();
        for (RegistryObject<Item> ro : TropicraftItems.ITEMS.getEntries()) {
            Item item = ro.get();
            if (item instanceof IColoredItem) {
                evt.getItemColors().register(basic, item);
            }
        }
        evt.getItemColors().register((stack, index) -> index == 0 ? Fluids.WATER.getAttributes().getColor() : -1, TropicraftBlocks.WATER_BARRIER.get());
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        TropicraftPackets.init();
        TropicraftBiomes.addFeatures();
    }
    
    private void onServerStarting(final FMLServerStartingEvent event) {
        MinigameManager.init(event.getServer());

        CommandTropicsTeleport.register(event.getServer().getCommandManager().getDispatcher());
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
            TropicraftBlockstateProvider blockstates = new TropicraftBlockstateProvider(gen, event.getExistingFileHelper());
            gen.addProvider(blockstates);
            gen.addProvider(new TropicraftItemModelProvider(gen, blockstates.getExistingHelper()));
            gen.addProvider(new TropicraftLangProvider(gen));
        }
        if (event.includeServer()) {
            gen.addProvider(new TropicraftBlockTagsProvider(gen));
            gen.addProvider(new TropicraftItemTagsProvider(gen));
            gen.addProvider(new TropicraftRecipeProvider(gen));
            gen.addProvider(new TropicraftLootTableProvider(gen));
        }
    }
}
