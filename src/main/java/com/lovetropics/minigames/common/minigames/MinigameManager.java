package com.lovetropics.minigames.common.minigames;

import com.google.common.collect.Maps;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.data.TropicraftLangKeys;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
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
import org.apache.logging.log4j.util.TriConsumer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;

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
    private MinigameInstance currentInstance;

    /**
     * Currently polling game. Is null when there is no minigame polling or a minigame is running.
     */
    private MinigameInstance polling;

    /**
     * A list of players that are currently registered for the currently polling minigame.
     * Is empty when a minigame has started or stopped polling.
     */
    private final MinigameRegistrations registrations = new MinigameRegistrations();

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
    public IMinigameInstance getActiveOrPollingMinigame() {
        if (currentInstance != null) {
            return currentInstance;
        }
        return polling;
    }

    @Override
    public IMinigameInstance getCurrentMinigame() {
        return this.currentInstance;
    }

    @Override
    public ActionResult<ITextComponent> finish() {
        if (this.currentInstance == null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
        }

        try {
	        IMinigameDefinition def = this.currentInstance.getDefinition();
            Exception err = dispatchToBehaviors(false, IMinigameBehavior::onFinish);
            if (err != null) {
            	return failException("Failed to finish behaviors", err);
            }

	        for (ServerPlayerEntity player : currentInstance.getPlayers()) {
	            currentInstance.removePlayer(player);
	        }

	        // Send all players a message letting them know the minigame has finished
	        for (ServerPlayerEntity player : this.server.getPlayerList().getPlayers()) {
	            player.sendMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_FINISHED_MINIGAME,
	                    new TranslationTextComponent(def.getUnlocalizedName()).applyTextStyle(TextFormatting.ITALIC).applyTextStyle(TextFormatting.AQUA))
	                    .applyTextStyle(TextFormatting.GOLD), ChatType.CHAT);
	        }

            err = dispatchToBehaviors(false, IMinigameBehavior::onPostFinish);
            if (err != null) {
            	return failException("Failed to clean up behaviors", err);
            }

            currentInstance.getDefinition().getMapProvider().close(currentInstance);

            ITextComponent minigameName = new TranslationTextComponent(this.currentInstance.getDefinition().getUnlocalizedName()).applyTextStyle(TextFormatting.AQUA);

            return new ActionResult<>(ActionResultType.SUCCESS, new TranslationTextComponent(TropicraftLangKeys.COMMAND_STOPPED_MINIGAME, minigameName).applyTextStyle(TextFormatting.GREEN));
        } catch (Exception e) {
        	return failException("Unknown error finishing minigame", e);
        } finally {
        	this.currentInstance = null;
        }
    }

    @Override
    public ActionResult<ITextComponent> cancel() {
        if (currentInstance == null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME));
        }

        Exception err = dispatchToBehaviors(false, IMinigameBehavior::onCancel);
        if (err != null) {
            return failException("Failed to cancel behaviors", err);
        }

        return finish();
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
        this.polling = new MinigameInstance(definition, server);

        Exception err = dispatchToBehaviors(false, IMinigameBehavior::onConstruct);
        if (err != null) {
            this.polling = null;
            return failException("Failed to construct behaviors", err);
        }

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
        String minigameName = this.polling.getDefinition().getUnlocalizedName();

        this.polling = null;
        this.registrations.clear();

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

        IMinigameDefinition pollingDefinition = polling.getDefinition();
        IMinigameMapProvider mapProvider = pollingDefinition.getMapProvider();
        ActionResult<ITextComponent> openResult = mapProvider.canOpen(pollingDefinition, server);
        if (openResult.getType() == ActionResultType.FAIL) {
        	return CompletableFuture.completedFuture(openResult);
        }

        CompletableFuture<DimensionType> openMap = mapProvider.open(polling, server);

        return openMap.thenApplyAsync(dimension -> {
            this.currentInstance = this.polling;
            this.polling = null;

            currentInstance.setDimension(dimension);

            try {
                ActionResult<ITextComponent> res = dispatchToBehaviors(b -> b.ensureValidity(this.currentInstance));
                if (res.getType() == ActionResultType.FAIL) {
                    return res;
                }

				Set<ServerPlayerEntity> participants = new HashSet<>();
				Set<ServerPlayerEntity> spectators = new HashSet<>();

				registrations.collectInto(server, participants, spectators, pollingDefinition.getMaximumParticipantCount());

				for (ServerPlayerEntity player : participants) {
					this.currentInstance.addPlayer(player, PlayerRole.PARTICIPANT);
				}

				for (ServerPlayerEntity player : spectators) {
					this.currentInstance.addPlayer(player, PlayerRole.SPECTATOR);
				}

                Exception err = dispatchToBehaviors(true, IMinigameBehavior::onStart);
                if (err != null) {
                    return failException("Failed to start behaviors", err);
                }

                return new ActionResult<>(ActionResultType.SUCCESS, new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_STARTED).applyTextStyle(TextFormatting.GREEN));
            } catch (Exception e) {
                ActionResult<ITextComponent> res = failException("Unknown error starting minigame", e);
                if (this.currentInstance != null) {
                    ActionResult<ITextComponent> stopRes = finish();
                    if (stopRes.getType() == ActionResultType.FAIL) {
                        return ActionResult.resultFail(res.getResult().appendSibling(stopRes.getResult()));
                    }
                }
                return res;
            } finally {
                this.registrations.clear();
            }
        }, server);
    }
    
    @Override
    public ActionResult<ITextComponent> registerFor(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
        // Check if minigame has already started
        if (this.currentInstance != null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
        }

        // Check if a minigame is polling
        if (this.polling == null) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_MINIGAME_POLLING));
        }

        if (this.registrations.contains(player.getUniqueID())) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED));
        }

        this.registrations.add(player.getUniqueID(), requestedRole);

        if (this.registrations.participantCount() == this.polling.getDefinition().getMinimumParticipantCount()) {
            for (ServerPlayerEntity p : this.server.getPlayerList().getPlayers()) {
                p.sendMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_ENOUGH_PLAYERS).applyTextStyle(TextFormatting.AQUA));
            }
        }

        ITextComponent playerName = player.getDisplayName().deepCopy().applyTextStyle(TextFormatting.GOLD);
        ITextComponent minigameName = new TranslationTextComponent(this.polling.getDefinition().getUnlocalizedName()).applyTextStyle(TextFormatting.GREEN);

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
        if (!this.registrations.contains(player.getUniqueID())) {
            return new ActionResult<>(ActionResultType.FAIL, new TranslationTextComponent(TropicraftLangKeys.COMMAND_NOT_REGISTERED_FOR_MINIGAME));
        }

        this.registrations.remove(player.getUniqueID());

        if (this.registrations.participantCount() == this.polling.getDefintion().getMinimumParticipantCount() - 1) {
            for (ServerPlayerEntity p : this.server.getPlayerList().getPlayers()) {
                p.sendMessage(new TranslationTextComponent(TropicraftLangKeys.COMMAND_NO_LONGER_ENOUGH_PLAYERS).applyTextStyle(TextFormatting.RED));
            }
        }

        ITextComponent minigameName = new TranslationTextComponent(this.polling.getDefinition().getUnlocalizedName()).applyTextStyle(TextFormatting.AQUA);
        ITextComponent msg = new TranslationTextComponent(TropicraftLangKeys.COMMAND_UNREGISTERED_MINIGAME, minigameName).applyTextStyle(TextFormatting.RED);

        return new ActionResult<>(ActionResultType.SUCCESS, msg);
    }

    private Collection<IMinigameBehavior> getBehaviors() {
        return this.currentInstance != null ? this.currentInstance.getDefinition().getAllBehaviours() : this.polling.getDefinition().getAllBehaviours();
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
            dispatchToBehaviors(false, IMinigameBehavior::onPlayerHurt, event);
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (this.ifPlayerInInstance(event.getPlayer())) {
            dispatchToBehaviors(false, IMinigameBehavior::onPlayerAttackEntity, event);
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (this.ifPlayerInInstance(event.getEntity())) {
            dispatchToBehaviors(false, IMinigameBehavior::onPlayerUpdate, (ServerPlayerEntity) event.getEntity());
        }

        if (this.ifEntityInDimension(event.getEntity())) {
            dispatchToBehaviors(false, IMinigameBehavior::onLivingEntityUpdate, event.getEntityLiving());
        }
    }

    /**
     * Funnel into minigame definition onPlayerDeath() for convenience
     */
    @SubscribeEvent
    public void onPlayerDeath(LivingDeathEvent event) {
        if (this.ifPlayerInInstance(event.getEntity())) {
            dispatchToBehaviors(false, IMinigameBehavior::onPlayerDeath, (ServerPlayerEntity) event.getEntity());
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
            dispatchToBehaviors(false, IMinigameBehavior::onPlayerRespawn, (ServerPlayerEntity) event.getPlayer());
        }
    }

    /**
     * Funnel into definition worldUpdate() for convenience
     */
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (this.currentInstance != null && event.world.getDimension().getType() == this.currentInstance.getDimension()) {
                dispatchToBehaviors(true, IMinigameBehavior::worldUpdate, event.world);
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
            if (this.registrations.contains(player.getUniqueID())) {
                this.unregisterFor((ServerPlayerEntity) event.getPlayer());
            }
        }
    }

    private <T> Exception dispatchToBehaviors(boolean stopOnError, TriConsumer<IMinigameBehavior, IMinigameInstance, T> action, T argument) {
    	return dispatchToBehaviors(stopOnError, (b, m) -> action.accept(b, m, argument));
    }

    private Exception dispatchToBehaviors(boolean stopOnError, BiConsumer<IMinigameBehavior, IMinigameInstance> action) {
    	Exception res = null;
    	for (IMinigameBehavior behavior : getBehaviors()) {
    		try {
    			action.accept(behavior, getActiveOrPollingMinigame());
    		} catch (Exception e) {
    			if (stopOnError) {
    				cancel();
    			}
    			return e;
    		}
    	}
    	return res;
    }

    private ActionResult<ITextComponent> dispatchToBehaviors(Function<IMinigameBehavior, ActionResult<ITextComponent>> action) {
        for (final IMinigameBehavior behavior : getBehaviors()) {
        	try {
        		ActionResult<ITextComponent> res = action.apply(behavior);

                if (res.getType() == ActionResultType.FAIL) {
                    return res;
                }
        	} catch (Exception e) {
        		return failException("Failed to dispatch pre-start behavior event", e);
        	}
        }
        return ActionResult.resultSuccess(new StringTextComponent(""));
    }
    
    private ActionResult<ITextComponent> failException(String prefix, Exception e) {
    	e.printStackTrace();
    	return ActionResult.resultFail(new StringTextComponent(prefix + ": " + e.toString()));
    }

    private boolean ifPlayerInInstance(Entity entity) {
        return entity instanceof ServerPlayerEntity && this.currentInstance != null && this.currentInstance.getPlayers().contains(entity.getUniqueID());
    }

    private boolean ifEntityInDimension(Entity entity) {
        return this.currentInstance != null && entity.dimension == this.currentInstance.getDimension();
    }
}
