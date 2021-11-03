package com.lovetropics.minigames;

import com.google.common.base.Preconditions;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.block.LoveTropicsBlocks;
import com.lovetropics.minigames.common.content.block.TrashType;
import com.lovetropics.minigames.common.content.build_competition.BuildCompetition;
import com.lovetropics.minigames.common.content.hide_and_seek.HideAndSeek;
import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTide;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodRider;
import com.lovetropics.minigames.common.content.trash_dive.TrashDive;
import com.lovetropics.minigames.common.core.command.ExtendedBossBarCommand;
import com.lovetropics.minigames.common.core.command.LoveTropicsEntityOptions;
import com.lovetropics.minigames.common.core.command.MapCommand;
import com.lovetropics.minigames.common.core.command.game.*;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensions;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.impl.GameEventDispatcher;
import com.lovetropics.minigames.common.core.game.impl.MultiGameManager;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.integration.Telemetry;
import com.lovetropics.minigames.common.core.map.VoidChunkGenerator;
import com.lovetropics.minigames.common.core.map.item.MapWorkspaceItems;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.mojang.brigadier.CommandDispatcher;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.NonNullLazyValue;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.RegisterCommandsEvent;
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
        @Override
        @OnlyIn(Dist.CLIENT)
        public ItemStack createIcon() {
            return new ItemStack(LoveTropicsBlocks.TRASH.get(TrashType.COLA).get());
        }
    });

    private static final NonNullLazyValue<LoveTropicsRegistrate> REGISTRATE = new NonNullLazyValue<>(() ->
    	LoveTropicsRegistrate.create(Constants.MODID)
    			  .itemGroup(() -> LOVE_TROPICS_ITEM_GROUP));

    @CapabilityInject(DriftwoodRider.class)
    private static Capability<DriftwoodRider> driftwoodRiderCap;

    @CapabilityInject(PlayerDisguise.class)
    private static Capability<PlayerDisguise> playerDisguiseCap;

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
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        modBus.addListener(ConfigLT::onLoad);
        modBus.addListener(ConfigLT::onFileChange);

        // Registry objects
        LoveTropicsBlocks.init();
        MapWorkspaceItems.init();

        GameBehaviorTypes.init(modBus);
        GameClientStateTypes.init(modBus);

        BuildCompetition.init();
        HideAndSeek.init();
        SurviveTheTide.init();
        TrashDive.init();

        LoveTropicsEntityOptions.register();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigLT.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigLT.SERVER_CONFIG);

        GameEventDispatcher eventDispatcher = new GameEventDispatcher(IGameManager.get());
        MinecraftForge.EVENT_BUS.register(eventDispatcher);
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

    public static LoveTropicsRegistrate registrate() {
        return REGISTRATE.getValue();
    }

    @OnlyIn(Dist.CLIENT)
    private void setupClient(final FMLClientSetupEvent event) {
        ForgeConfig.CLIENT.alwaysSetupTerrainOffThread.set(true);
        ((ForgeConfigSpec)ObfuscationReflectionHelper.getPrivateValue(ForgeConfig.class, null, "clientSpec")).save();
    }

    private void setup(final FMLCommonSetupEvent event) {
        LoveTropicsNetwork.register();

        VoidChunkGenerator.register();

        CapabilityManager.INSTANCE.register(DriftwoodRider.class, DriftwoodRider.STORAGE, () -> {
            throw new UnsupportedOperationException();
        });

        CapabilityManager.INSTANCE.register(PlayerDisguise.class, PlayerDisguise.STORAGE, () -> {
            throw new UnsupportedOperationException();
        });
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        JoinGameCommand.register(dispatcher);
        StartGameCommand.register(dispatcher);
        FinishGameCommand.register(dispatcher);
        CancelGameCommand.register(dispatcher);
        LeaveGameCommand.register(dispatcher);
        GameControlCommand.register(dispatcher);
        MapCommand.register(dispatcher);
        GamePackageCommand.register(dispatcher);
        ManageGameLobbyCommand.register(dispatcher);
        ExtendedBossBarCommand.register(dispatcher);
    }

    private void onServerAboutToStart(final FMLServerAboutToStartEvent event) {
        Telemetry.INSTANCE.sendOpen();
    }

    private void onServerStopping(final FMLServerStoppingEvent event) {
        Telemetry.INSTANCE.sendClose();
    }

    private void gatherData(GatherDataEvent event) {
        registrate().addDataGenerator(ProviderType.LANG, prov -> {
            prov.add(LoveTropics.LOVE_TROPICS_ITEM_GROUP, "Love Tropics");

            GameTexts.collectTranslations(prov::add);
            MinigameTexts.collectTranslations(prov::add);
            BiodiversityBlitzTexts.collectTranslations(prov::add);
        });
    }

    public static Capability<DriftwoodRider> driftwoodRiderCap() {
        return Preconditions.checkNotNull(driftwoodRiderCap, "driftwood rider capability not initialized");
    }

    public static Capability<PlayerDisguise> playerDisguiseCap() {
        return Preconditions.checkNotNull(playerDisguiseCap, "player disguise capability not initialized");
    }

    public static void onServerStoppingUnsafely(MinecraftServer server) {
        MultiGameManager.onServerStoppingUnsafely(server);
        RuntimeDimensions.onServerStoppingUnsafely(server);
    }
}
