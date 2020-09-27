package com.lovetropics.minigames.common.minigames;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.config.MinigameConfig;
import com.lovetropics.minigames.common.minigames.config.MinigameConfigs;
import com.lovetropics.minigames.common.minigames.map.IMinigameMapProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Standard implementation of a minigame manager. Would prefer to do something other
 * than singleton-style implementation to allow for multiple managers to run multiple
 * minigames at once.
 */
public class MinigameManager implements IMinigameManager
{
    /**
     * Singleton instance that persists throughout server lifecycle.
     */
    private static IMinigameManager INSTANCE;

    /**
     * Registry map for minigame definitions. Used to fetch and start requested minigames.
     */
    private Map<ResourceLocation, IMinigameDefinition> registeredMinigames = Maps.newHashMap();

    /**
     * Current instance of the minigame. Agnostic from minigame definition and used to store
     * the players and spectators that are a part of the current minigame.
     *
     * Is null when there is no minigame running. Isn't set until a game has finished polling
     * and has started.
     */
    private IMinigameInstance currentInstance;

    /**
     * Currently polling game. Is null when there is no minigame polling or a minigame is running.
     */
    private IMinigameDefinition polling;

    /**
     * A list of players that are currently registered for the currently polling minigame.
     * Is empty when a minigame has started or stopped polling.
     */
    private List<UUID> registeredForMinigame = Lists.newArrayList();

    /**
     * Server reference to fetch players from player list.
     */
    private MinecraftServer server;

    private MinigameManager(MinecraftServer server) {
        this.server = server;
    }

    /**
     * Initialize the MinigameManager singleton. Registers itself for forge events
     * and registers some minigame definitions.
     * @param server The minecraft server used for fetching player list.
     */
    public static void init(MinecraftServer server) {
        INSTANCE = new MinigameManager(server);
        MinecraftForge.EVENT_BUS.register(INSTANCE);

        for (MinigameConfig config : MinigameConfigs.getConfigs()) {
            INSTANCE.register(new MinigameDefinitionGeneric(config));
        }
    }

    /**
     * Returns null if init() has not been called yet. Shouldn't be called
     * before the server has started.
     * @return The global instance of the minigame manager.
     */
    public static IMinigameManager getInstance() {
        return INSTANCE;
    }

    @Override
    public void register(IMinigameDefinition minigame) {
        if (this.registeredMinigames.containsKey(minigame.getID())) {
            throw new IllegalArgumentException("Minigame already registered with the following ID: " + minigame.getID());
        }

        this.registeredMinigames.put(minigame.getID(), minigame);
    }

    @Override
    public void unregister(ResourceLocation minigameID) {
        if (!this.registeredMinigames.containsKey(minigameID)) {
            TranslationTextComponent msg = new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_NOT_REGISTERED, minigameID);
            throw new IllegalArgumentException(msg.getFormattedText());
        }

        this.registeredMinigames.remove(minigameID);
    }
    
    @Override
    public Collection<IMinigameDefinition> getAllMinigames() {
        return Collections.unmodifiableCollection(this.registeredMinigames.values());
    }

    @Override
    public IMinigameInstance getCurrentMinigame() {
        return this.currentInstance;
    }

    @Override
    public void finishCurrentMinigame() {
        if (this.currentInstance == null) {
            throw new IllegalStateException("Attempted to finish a current minigame when none are running.");
        }

        IMinigameDefinition def = this.currentInstance.getDefinition();
        getBehaviours().forEach((b) -> b.onFinish(this.currentInstance));

        for (ServerPlayerEntity player : currentInstance.getPlayers()) {
            currentInstance.removePlayer(player);
        }

        // Send all players a message letting them know the minigame has finished
        for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
            player.sendMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_FINISHED_MINIGAME,
                    new TranslationTextComponent(def.getUnlocalizedName()).applyTextStyle(TextFormatting.ITALIC).applyTextStyle(TextFormatting.AQUA))
                    .applyTextStyle(TextFormatting.GOLD), ChatType.CHAT);
        }

        getBehaviours().forEach((b) -> b.onPostFinish(this.currentInstance));

        this.currentInstance = null;
    }

    @Override
    public ActionResult<ITextComponent> startPolling(ResourceLocation minigameId) {
        // Make sure minigame is registered with provided id
        if (!this.registeredMinigames.containsKey(minigameId)) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ID_INVALID));
        }

        // Make sure there isn't a currently running minigame
        if (this.currentInstance != null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
        }

        // Make sure another minigame isn't already polling
        if (this.polling != null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_ANOTHER_MINIGAME_POLLING));
        }

        IMinigameDefinition definition = this.registeredMinigames.get(minigameId);
        this.polling = definition;

        for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
            player.sendMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_POLLING,
                new TranslationTextComponent(definition.getUnlocalizedName()).applyTextStyle(TextFormatting.ITALIC).applyTextStyle(TextFormatting.AQUA),
                new StringTextComponent("/minigame register").applyTextStyle(TextFormatting.ITALIC).applyTextStyle(TextFormatting.GRAY))
                .applyTextStyle(TextFormatting.GOLD), ChatType.CHAT);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_POLLED));
    }

    @Override
    public ActionResult<ITextComponent> stopPolling() {
        // Check if a minigame is polling
        if (this.polling == null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
        }

        // Cache before killing the currently polled game
        String minigameName = this.polling.getUnlocalizedName();

        this.polling = null;
        this.registeredForMinigame.clear();

        for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
            player.sendMessage(new TranslationTextComponent(Constants.MODID + ".minigame.minigame_stopped_polling",
                    new TranslationTextComponent(minigameName).applyTextStyle(TextFormatting.ITALIC).applyTextStyle(TextFormatting.AQUA))
                    .applyTextStyle(TextFormatting.RED), ChatType.CHAT);
        }

        return new ActionResult<>(ActionResultType.SUCCESS,
            new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOP_POLL).applyTextStyle(TextFormatting.GREEN));
    }

    @Override
    public CompletableFuture<ActionResult<ITextComponent>> start() {
        // Check if any minigame is polling, can only start if so
        if (this.polling == null) {
            return CompletableFuture.completedFuture(new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING)));
        }

        // Check that have enough players to start minigame.
        /*if (this.registeredForMinigame.size() < this.polling.getMinimumParticipantCount()) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NOT_ENOUGH_PLAYERS, this.polling.getMinimumParticipantCount()));
        }*/

        IMinigameDefinition polling = this.polling;
        this.polling = null;

        IMinigameMapProvider mapProvider = polling.getMapProvider();

        ActionResult<ITextComponent> canOpenMap = mapProvider.canOpen(polling, server);
        if (canOpenMap.getType() == ActionResultType.FAIL) {
            return CompletableFuture.completedFuture(canOpenMap);
        }

        MapRegions mapRegions = new MapRegions();
        CompletableFuture<DimensionType> openMap = mapProvider.open(polling, server, mapRegions);

        return openMap.thenApplyAsync(dimension -> {
                MinigameInstance instance = new MinigameInstance(polling, server, dimension, mapRegions);
                polling.getAllBehaviours().forEach(b -> b.onConstruct(instance, server));

                int playersAvailable = Math.min(this.registeredForMinigame.size(), polling.getMaximumParticipantCount());
                List<UUID> chosenParticipants = Util.extractRandomElements(new Random(), this.registeredForMinigame, playersAvailable);

                for (UUID playerUUID : chosenParticipants) {
                    ServerPlayerEntity player = this.server.getPlayerList().getPlayerByUUID(playerUUID);
                    if (player != null) {
                        instance.addPlayer(player, PlayerRole.PARTICIPANT);
                    }
                }

                for (UUID spectatorUUID : this.registeredForMinigame) {
                    ServerPlayerEntity spectator = this.server.getPlayerList().getPlayerByUUID(spectatorUUID);
                    if (spectator != null) {
                        instance.addPlayer(spectator, PlayerRole.SPECTATOR);
                    }
                }

                this.currentInstance = instance;
                this.registeredForMinigame.clear();

                instance.getDefinition().getAllBehaviours().forEach((b) -> b.onStart(instance));

                return new ActionResult<>(ActionResultType.SUCCESS, new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_STARTED).applyTextStyle(TextFormatting.GREEN));
            }, server);
    }

    @Override
    public ActionResult<ITextComponent> stop() {
        // Can't stop a current minigame if doesn't exist
        if (this.currentInstance == null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
        }

        ITextComponent minigameName = new TranslationTextComponent(this.currentInstance.getDefinition().getUnlocalizedName()).applyTextStyle(TextFormatting.AQUA);

        this.finishCurrentMinigame();

        return new ActionResult<>(ActionResultType.SUCCESS, new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOPPED_MINIGAME, minigameName).applyTextStyle(TextFormatting.GREEN));
    }

    @Override
    public ActionResult<ITextComponent> registerFor(ServerPlayerEntity player) {
        // Check if minigame has already started
        if (this.currentInstance != null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
        }

        // Check if a minigame is polling
        if (this.polling == null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
        }

        if (this.registeredForMinigame.contains(player.getUniqueID())) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED));
        }

        this.registeredForMinigame.add(player.getUniqueID());

        if (this.registeredForMinigame.size() == this.polling.getMinimumParticipantCount()) {
            for (ServerPlayerEntity p : this.server.getPlayerList().getPlayers()) {
                p.sendMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_ENOUGH_PLAYERS).applyTextStyle(TextFormatting.AQUA));
            }
        }

        ITextComponent playerName = player.getDisplayName().deepCopy().applyTextStyle(TextFormatting.GOLD);
        ITextComponent minigameName = new TranslationTextComponent(this.polling.getUnlocalizedName()).applyTextStyle(TextFormatting.GREEN);

        for (ServerPlayerEntity p : this.server.getPlayerList().getPlayers()) {
            p.sendMessage(new TranslationTextComponent("%s has joined the %s minigame!", playerName, minigameName).applyTextStyle(TextFormatting.AQUA));
        }

        minigameName.applyTextStyle(TextFormatting.AQUA);
        ITextComponent msg = new TranslationTextComponent(TropicraftLangKeys.COMMAND_REGISTERED_FOR_MINIGAME, minigameName).applyTextStyle(TextFormatting.GREEN);

        return new ActionResult<>(ActionResultType.SUCCESS, msg);
    }

    @Override
    public ActionResult<ITextComponent> unregisterFor(ServerPlayerEntity player) {
        // Check if a minigame is polling
        if (this.polling == null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
        }

        // Check if minigame has already started
        if (!this.registeredForMinigame.contains(player.getUniqueID())) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NOT_REGISTERED_FOR_MINIGAME));
        }

        this.registeredForMinigame.remove(player.getUniqueID());

        if (this.registeredForMinigame.size() == this.polling.getMinimumParticipantCount() - 1) {
            for (ServerPlayerEntity p : this.server.getPlayerList().getPlayers()) {
                p.sendMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_LONGER_ENOUGH_PLAYERS).applyTextStyle(TextFormatting.RED));
            }
        }

        ITextComponent minigameName = new TranslationTextComponent(this.polling.getUnlocalizedName()).applyTextStyle(TextFormatting.AQUA);
        ITextComponent msg = new TranslationTextComponent(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, minigameName).applyTextStyle(TextFormatting.RED);

        return new ActionResult<>(ActionResultType.SUCCESS, msg);
    }

    private Collection<IMinigameBehavior> getBehaviours() {
        return this.currentInstance.getDefinition().getAllBehaviours();
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkDataEvent.Load event) {
        // TODO: WE DON'T NEED THIS PAST 1.15, HACKY DATA FIXING
        if (event.getStatus() == ChunkStatus.Type.LEVELCHUNK && this.currentInstance != null) {
            final IWorld world = event.getWorld();

            if (world != null) {
                final DimensionType dimensionType = world.getDimension().getType();

                if (dimensionType == this.currentInstance.getDimension()) {
                    final ListNBT entities = event.getData().getList("Entities", 10);

                    for(int i1 = 0; i1 < entities.size(); ++i1) {
                        CompoundNBT entityNBT = entities.getCompound(i1);
                        entityNBT.putInt("Dimension", dimensionType.getId());
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerHurt(LivingHurtEvent event) {
        if (this.ifPlayerInInstance(event.getEntity())) {
            getBehaviours().forEach((b) -> b.onPlayerHurt(this.currentInstance, event));
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (this.ifPlayerInInstance(event.getPlayer())) {
            getBehaviours().forEach((b) -> b.onPlayerAttackEntity(this.currentInstance, event));
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (this.ifPlayerInInstance(event.getEntity())) {
            getBehaviours().forEach((b) -> b.onPlayerUpdate(this.currentInstance, (ServerPlayerEntity) event.getEntity()));
        }

        if (this.ifEntityInDimension(event.getEntity())) {
            getBehaviours().forEach((b) -> b.onLivingEntityUpdate(this.currentInstance, event.getEntityLiving()));
        }
    }

    /**
     * Funnel into minigame definition onPlayerDeath() for convenience
     */
    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (this.ifPlayerInInstance(event.getEntity())) {
            getBehaviours().forEach((b) -> b.onPlayerDeath(this.currentInstance, (ServerPlayerEntity) event.getEntity()));
        }
    }

    /**
     * Funnel into definition onPlayerRespawn() for convenience.
     *
     * Also set player's respawn position defined by the minigame definition.
     */
    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (this.ifPlayerInInstance(event.getPlayer())) {
            getBehaviours().forEach((b) -> b.onPlayerRespawn(this.currentInstance, (ServerPlayerEntity) event.getPlayer()));
        }
    }

    /**
     * Funnel into definition worldUpdate() for convenience
     */
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (this.currentInstance != null && event.world.getDimension().getType() == this.currentInstance.getDimension()) {
                getBehaviours().forEach((b) -> b.worldUpdate(this.currentInstance, event.world));
                this.currentInstance.update();
            }
        }
    }

    /**
     * When a player logs out, remove them from the currently running minigame instance
     * if they are inside, and teleport back them to their original state.
     *
     * Also if they have registered for a minigame poll, they will be removed from the
     * list of registered players.
     */
    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

        if (this.currentInstance != null) {
            this.currentInstance.removePlayer(player);
        }

        if (this.polling != null) {
            if (this.registeredForMinigame.contains(player.getUniqueID())) {
                this.unregisterFor((ServerPlayerEntity) event.getPlayer());
            }
        }
    }

    private boolean ifPlayerInInstance(Entity entity) {
        return entity instanceof ServerPlayerEntity && this.currentInstance != null && this.currentInstance.getPlayers().contains(entity.getUniqueID());
    }

    private boolean ifEntityInDimension(Entity entity) {
        return this.currentInstance != null && entity.dimension == this.currentInstance.getDimension();
    }
}
