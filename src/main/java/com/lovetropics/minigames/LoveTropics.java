package com.lovetropics.minigames;

import com.lovetropics.minigames.client.game.handler.GameSidebarRenderer;
import com.lovetropics.minigames.client.game.handler.spectate.SpectatingUi;
import com.lovetropics.minigames.client.lobby.LobbyKeybinds;
import com.lovetropics.minigames.client.lobby.LobbyStateGui;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.render.BbClientRenderEffects;
import com.lovetropics.minigames.common.content.block.LoveTropicsBlocks;
import com.lovetropics.minigames.common.content.block.TrashType;
import com.lovetropics.minigames.common.content.block_party.BlockParty;
import com.lovetropics.minigames.common.content.block_party.BlockPartyTexts;
import com.lovetropics.minigames.common.content.build_competition.BuildCompetition;
import com.lovetropics.minigames.common.content.hide_and_seek.HideAndSeek;
import com.lovetropics.minigames.common.content.spleef.Spleef;
import com.lovetropics.minigames.common.content.qottott.Qottott;
import com.lovetropics.minigames.common.content.qottott.QottottTexts;
import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTide;
import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTideTexts;
import com.lovetropics.minigames.common.content.survive_the_tide.entity.DriftwoodRider;
import com.lovetropics.minigames.common.content.trash_dive.TrashDive;
import com.lovetropics.minigames.common.content.trash_dive.TrashDiveTexts;
import com.lovetropics.minigames.common.content.turtle_race.TurtleRace;
import com.lovetropics.minigames.common.content.turtle_race.TurtleRaceTexts;
import com.lovetropics.minigames.common.core.chat.ChatChannelStore;
import com.lovetropics.minigames.common.core.command.ChatCommand;
import com.lovetropics.minigames.common.core.command.ExtendedBossBarCommand;
import com.lovetropics.minigames.common.core.command.LoveTropicsEntityOptions;
import com.lovetropics.minigames.common.core.command.MapCommand;
import com.lovetropics.minigames.common.core.command.ParticleLineCommand;
import com.lovetropics.minigames.common.core.command.TemporaryDimensionCommand;
import com.lovetropics.minigames.common.core.command.game.CancelGameCommand;
import com.lovetropics.minigames.common.core.command.game.FinishGameCommand;
import com.lovetropics.minigames.common.core.command.game.GameControlCommand;
import com.lovetropics.minigames.common.core.command.game.GamePackageCommand;
import com.lovetropics.minigames.common.core.command.game.JoinGameCommand;
import com.lovetropics.minigames.common.core.command.game.LeaveGameCommand;
import com.lovetropics.minigames.common.core.command.game.ManageGameLobbyCommand;
import com.lovetropics.minigames.common.core.command.game.StartGameCommand;
import com.lovetropics.minigames.common.core.diguise.PlayerDisguise;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensions;
import com.lovetropics.minigames.common.core.entity.MinigameEntities;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.action.ActionTargetTypes;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.impl.GameEventDispatcher;
import com.lovetropics.minigames.common.core.game.predicate.entity.EntityPredicates;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.integration.BackendIntegrations;
import com.lovetropics.minigames.common.core.map.VoidChunkGenerator;
import com.lovetropics.minigames.common.core.item.MinigameItems;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.role.StreamHosts;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.util.function.BiConsumer;
import java.util.regex.Pattern;

@Mod(Constants.MODID)
public class LoveTropics {

    public static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation TAB_ID = new ResourceLocation(Constants.MODID, "ltminigames");
    public static final ResourceKey<CreativeModeTab> TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, TAB_ID);

    private static final NonNullLazy<LoveTropicsRegistrate> REGISTRATE = NonNullLazy.of(() -> LoveTropicsRegistrate.create(Constants.MODID).defaultCreativeTab(ResourceKey.create(Registries.CREATIVE_MODE_TAB, TAB_ID)));

    public static final Capability<DriftwoodRider> DRIFTWOOD_RIDER = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<PlayerDisguise> PLAYER_DISGUISE = CapabilityManager.get(new CapabilityToken<>() {});
    public static final Capability<ChatChannelStore> CHAT_CHANNEL = CapabilityManager.get(new CapabilityToken<>() {});

    public LoveTropics() {
    	// Compatible with all versions that match the semver (excluding the qualifier e.g. "-beta+42")
    	ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(LoveTropics::getCompatVersion, (s, v) -> LoveTropics.isCompatibleVersion(s)));

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // General mod setup
        modBus.addListener(this::setup);

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            // Client setup
            modBus.addListener(this::setupClient);
            modBus.addListener(this::registerDimensionSpecialEffects);
        });

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        modBus.addListener(ConfigLT::onLoad);
        modBus.addListener(ConfigLT::onFileChange);

        registrate()
                .addDataGenerator(ProviderType.LANG, prov -> {
                    BiConsumer<String, String> consumer = prov::add;
                    GameTexts.collectTranslations(consumer);
                    MinigameTexts.KEYS.forEach(consumer);
                    BiodiversityBlitzTexts.collectTranslations(consumer);
                    BlockPartyTexts.KEYS.forEach(consumer);
                    SurviveTheTideTexts.KEYS.forEach(consumer);
                    TrashDiveTexts.KEYS.forEach(consumer);
                    TurtleRaceTexts.KEYS.forEach(consumer);
                    QottottTexts.KEYS.forEach(consumer);
                })
                .generic(TAB_ID.getPath(), Registries.CREATIVE_MODE_TAB, () -> CreativeModeTab.builder()
                        .title(registrate().addLang("itemGroup", TAB_ID, "LTMinigames"))
                        .icon(() -> LoveTropicsBlocks.TRASH.get(TrashType.COLA).asStack())
                        .build()
                ).build();

        // Registry objects
        LoveTropicsBlocks.init();
        MinigameItems.init();
        MinigameEntities.init();

        GameBehaviorTypes.init(modBus);
        ActionTargetTypes.init(modBus);
        EntityPredicates.init(modBus);
        GameClientStateTypes.init(modBus);
        StreamHosts.init();

        BuildCompetition.init();
        HideAndSeek.init();
        SurviveTheTide.init();
        TrashDive.init();
        BlockParty.init();
        TurtleRace.init();
        Qottott.init();
        Spleef.init();

        SoundRegistry.REGISTER.register(modBus);

        LoveTropicsEntityOptions.register();

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigLT.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigLT.SERVER_CONFIG);

        GameEventDispatcher eventDispatcher = new GameEventDispatcher(IGameManager.get());
        MinecraftForge.EVENT_BUS.register(eventDispatcher);

        modBus.addListener((RegisterGuiOverlaysEvent event) -> {
            LobbyStateGui.registerOverlays(event);
            GameSidebarRenderer.registerOverlays(event);
            SpectatingUi.registerOverlays(event);
            BbClientRenderEffects.registerOverlays(event);
        });
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
        return REGISTRATE.get();
    }

    @OnlyIn(Dist.CLIENT)
    private void setupClient(final FMLClientSetupEvent event) {
        LobbyKeybinds.init();
        ForgeConfig.CLIENT.alwaysSetupTerrainOffThread.set(true);
        ((ForgeConfigSpec) ObfuscationReflectionHelper.getPrivateValue(ForgeConfig.class, null, "clientSpec")).save();
    }

    @OnlyIn(Dist.CLIENT)
    private void registerDimensionSpecialEffects(final RegisterDimensionSpecialEffectsEvent event) {
        event.register(new ResourceLocation(Constants.MODID, "raised_clouds"), new DimensionSpecialEffects(250.0f, true, DimensionSpecialEffects.SkyType.NORMAL, false, false) {
            @Override
            public Vec3 getBrightnessDependentFogColor(final Vec3 color, final float brightness) {
                return color.multiply(brightness * 0.94f + 0.06f, brightness * 0.94f + 0.06f, brightness * 0.91f + 0.09f);
            }

            @Override
            public boolean isFoggyAt(final int x, final int z) {
                return false;
            }
        });
    }

    private void setup(final FMLCommonSetupEvent event) {
        LoveTropicsNetwork.register();

        VoidChunkGenerator.register();
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        JoinGameCommand.register(dispatcher);
        StartGameCommand.register(dispatcher);
        FinishGameCommand.register(dispatcher);
        CancelGameCommand.register(dispatcher);
        LeaveGameCommand.register(dispatcher);
        GameControlCommand.register(dispatcher);
        MapCommand.register(dispatcher);
        TemporaryDimensionCommand.register(dispatcher);
        GamePackageCommand.register(dispatcher);
        ManageGameLobbyCommand.register(dispatcher);
        ExtendedBossBarCommand.register(dispatcher);
        ParticleLineCommand.register(event.getBuildContext(), dispatcher);
        ChatCommand.register(dispatcher);
    }

    private void onServerAboutToStart(final ServerAboutToStartEvent event) {
        BackendIntegrations.get().sendOpen();
    }

    private void onServerStopping(final ServerStoppingEvent event) {
        BackendIntegrations.get().sendClose();
    }

    public static void onServerStoppingUnsafely(MinecraftServer server) {
        RuntimeDimensions.onServerStoppingUnsafely(server);
    }
}
