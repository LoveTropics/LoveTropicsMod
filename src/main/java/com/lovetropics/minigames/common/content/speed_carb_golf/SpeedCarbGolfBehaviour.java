package com.lovetropics.minigames.common.content.speed_carb_golf;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.util.world.gamedata.GameDataStorage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.Util;
import net.minecraft.commands.CommandResultCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.execution.ExecutionContext;
import net.minecraft.commands.functions.CommandFunction;
import net.minecraft.commands.functions.InstantiatedFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.scores.Objective;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public record SpeedCarbGolfBehaviour(Map<ResourceLocation, String> potentialHoles, Map<GameTeamKey,
        List<String>> holeRegions, ResourceLocation changeHoleNumberFunction, ResourceLocation startHoleFunction) implements IGameBehavior {
    public static final MapCodec<SpeedCarbGolfBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.STRING).fieldOf("possible_holes").forGetter(SpeedCarbGolfBehaviour::potentialHoles),
            Codec.unboundedMap(GameTeamKey.CODEC, Codec.STRING.listOf()).optionalFieldOf("team_holes", Map.of()).forGetter(b -> b.holeRegions),
            ResourceLocation.CODEC.optionalFieldOf("change_hole_number_function", ResourceLocation.fromNamespaceAndPath("lt", "world_games/minigolf/core/change_id")).forGetter(SpeedCarbGolfBehaviour::changeHoleNumberFunction),
            ResourceLocation.CODEC.optionalFieldOf("start_hole_function", ResourceLocation.fromNamespaceAndPath("lt", "world_games/minigolf/core/start")).forGetter(SpeedCarbGolfBehaviour::startHoleFunction)
    ).apply(i, SpeedCarbGolfBehaviour::new));

    private static final Holder<EntityType<?>> FIDDLER_CRAB = DeferredHolder.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("tropicraft", "fiddler_crab"));

    private static final Logger LOGGER = LogManager.getLogger(SpeedCarbGolfBehaviour.class);

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        ServerLevel level = game.level();
        ServerFunctionManager serverFunctionManager = game.server().getFunctions();
        CommandFunction<CommandSourceStack> changeHoleNumber = serverFunctionManager.get(changeHoleNumberFunction).orElseThrow();
        CommandFunction<CommandSourceStack> startHole = serverFunctionManager.get(startHoleFunction).orElseThrow();
        CommandSourceStack commandSourceStack = game.server().createCommandSourceStack().withLevel(level)
                .withSuppressedOutput().withPermission(3).withSource(game.server());
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        ResourceLocation blankHoleId = ResourceLocation.fromNamespaceAndPath("lt","golf/blank");
        List<ResourceLocation> holesToPickFrom = new ArrayList<>(potentialHoles.keySet());
        int holesPerTeam = holeRegions.get(teams.iterator().next().key()).size();
        List<ResourceLocation> pickedHoles = new ArrayList<>();
        StructurePlaceSettings structureplacesettings = new StructurePlaceSettings().addProcessor(BlockIgnoreProcessor.AIR);
        Map<UUID, String> assignedHoles = new HashMap<>();
        Map<GameTeamKey, List<String>> teamProgress = new HashMap<>();
        Map<String, HoleConfig> holeConfigs = new HashMap<>();
        for(int i = 0; i < holesPerTeam; i++) {
            Util.shuffle(holesToPickFrom, level.getRandom());
            ResourceLocation pickedHole = holesToPickFrom.removeFirst();
            pickedHoles.add(pickedHole);
        }
        // This is neat but is extra work!
//        int perTick = 2;
//        AtomicInteger tickCount = new AtomicInteger(perTick);
//        AtomicInteger cycles = new AtomicInteger(0);
//        AtomicBoolean holeState = new AtomicBoolean(false);
//        AtomicInteger currentPlacedHole = new AtomicInteger(0);
//        AtomicInteger regionToPlace = new AtomicInteger(0);
//        events.listen(GamePhaseEvents.TICK, () -> {
//            if(tickCount.get() > 0){
//                tickCount.decrementAndGet();
//            }
//            if(tickCount.get() == 0){
//                if(holeState.get()){
//                    for(GameTeam team : teams){
//                        String regionKey = holeRegions.get(team.key()).get(regionToPlace.get());
//                        BlockBox region = game.mapRegions().getOrThrow(regionKey);
//                        quickLoadGolfHole(team, level, blankHoleId, region, structureplacesettings, regionKey, false);
//                    }
//                    holeState.set(false);
//                    tickCount.set(perTick);
//                } else {
//                    ResourceLocation pickedHole = pickedHoles.get(currentPlacedHole.getAndIncrement());
//                    for(GameTeam team : teams){
//                        String regionKey = holeRegions.get(team.key()).get(regionToPlace.get());
//                        BlockBox region = game.mapRegions().getOrThrow(regionKey);
//                        quickLoadGolfHole(team, level, pickedHole, region, structureplacesettings, regionKey, true);
//                    }
//                    if(currentPlacedHole.get() >= pickedHoles.size()){
//                        currentPlacedHole.set(0);
//                        if(cycles.incrementAndGet() == 5){
//                            tickCount.set(-1);
//                            holeState.set(true);
//                            return;
//                        }
//                    }
//                    holeState.set(true);
//                    tickCount.set(perTick);
//                }
//            }
//        });
        events.listen(GameWorldEvents.ENTITY_REMOVED, (entity) -> {
            if(entity.getType().builtInRegistryHolder().is(FIDDLER_CRAB)){
                if(entity.getTags().contains("golfCrab")){
                    String hole = "";
                    for(String tag : entity.getTags()){
                        if(tag.startsWith("hole")){
                            hole = tag;
                            break;
                        }
                    }
                    if(!hole.isBlank()){
                        hole = hole.replace("hole", "");
                        hole = hole.replace("Crab", "");
                        Objective globalGolfObjective = game.level().getScoreboard().getObjective("golf.global");
                        if(game.level().getScoreboard().getPlayerScoreInfo(entity, globalGolfObjective).value() == 2){
                            UUID foundPlayer = null;
                            for (Map.Entry<UUID, String> uuidStringEntry : assignedHoles.entrySet()) {
                                if(uuidStringEntry.getValue().equalsIgnoreCase(hole)){
                                    foundPlayer = uuidStringEntry.getKey();
                                    break;
                                }
                            }
                            if(foundPlayer != null){
                                ServerPlayer player = game.allPlayers().getPlayerBy(foundPlayer);
                                CompoundTag gameData = GameDataStorage.get(level).get(ResourceLocation.fromNamespaceAndPath("lt", "golf"), foundPlayer);
                                GameTeamKey teamKey = teams.getTeamForPlayer(player);
                                game.statistics().forTeam(teamKey)
                                        .incrementInt(StatisticKey.POINTS, gameData.getInt("hole" + hole));
                                teamProgress.get(teamKey).remove(hole);
                                String nextHole = teamProgress.get(teamKey).getFirst();
                                UUID nextPlayer = null;
                                for (Map.Entry<UUID, String> uuidStringEntry : assignedHoles.entrySet()) {
                                    if(uuidStringEntry.getValue().equalsIgnoreCase(nextHole)){
                                        nextPlayer = uuidStringEntry.getKey();
                                        break;
                                    }
                                }
                                // WHOS THE NEXT PLAYER?!
                                if(nextPlayer != null){
                                    ServerPlayer nextPlayerP = game.allPlayers().getPlayerBy(nextPlayer);
                                    startHoleForPlayer(game, nextPlayerP, nextHole, level, startHole);
                                } else {
                                    boolean areAllTeamsEmpty = true;
                                    for (GameTeamKey gameTeamKey : teamProgress.keySet()) {
                                        if(!teamProgress.get(gameTeamKey).isEmpty()){
                                            areAllTeamsEmpty = false;
                                            break;
                                        }
                                    }
                                    if(areAllTeamsEmpty) {
                                        if (!game.invoker(GameLogicEvents.REQUEST_GAME_OVER).requestGameOver()) {
                                            game.requestStop(GameStopReason.finished());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });
        events.listen(GameWorldEvents.ENTITY_ADDED, (entity) -> {
            if(entity.getType() == EntityType.MARKER){
                if(entity.getTags().contains("golfStart") || entity.getTags().contains("golfEnd")){
                    System.out.println(entity.getTags());
                    boolean isStart = entity.getTags().contains("golfStart");
                    for (String s : game.mapRegions().keySet()) {
                        if(game.mapRegions().getOrThrow(s).contains(entity.blockPosition())){
                            HoleConfig holeConfig = holeConfigs.get(s);
                            String oldTag = "hole" + holeConfig.oldName + (isStart ? "Start" : "End");
                            String newTag = "hole" + holeConfig.newName + (isStart ? "Start" : "End");
                            System.out.println("Found marker in " + s + " and removing " + oldTag + " and adding " + newTag);
                            entity.removeTag(oldTag);
                            entity.addTag(newTag);
                            return;
                        }
                    }
                }
            }
        });
        events.listen(GameTeamEvents.TEAMS_ALLOCATED, () -> {
            int x = 1;
            for(GameTeam team : teams){
                Iterator<ServerPlayer> players = teams.getPlayersForTeam(team.key()).iterator();
                List<String> holes = new ArrayList<>();
                for (int i = 0; i < pickedHoles.size(); i++) {
                    String holeNumber = "1" + x + "0" + i;
                    ResourceLocation pickedHole = pickedHoles.get(i);
                    String regionKey = holeRegions.get(team.key()).get(i);
                    BlockBox region = game.mapRegions().getOrThrow(regionKey);
                    holeConfigs.put(regionKey, new HoleConfig(potentialHoles.get(pickedHole), holeNumber));
                    loadGolfHole(game, team, level, pickedHole, structureplacesettings, region, regionKey, holeNumber,
                            changeHoleNumber, commandSourceStack, true);
                    if(players.hasNext()) {
                        assignedHoles.put(players.next().getUUID(), holeNumber);
                    }
                    holes.add(holeNumber);
                }
                teamProgress.put(team.key(), holes);
                x++;
            }
        });
        events.listen(GamePlayerEvents.SPAWN, (player, spawnBuilder, otherThing) -> {
            GameTeamKey playerTeam = teams.getTeamForPlayer(player);
            String playerHole = assignedHoles.get(player);
            if(playerHole.equalsIgnoreCase(teamProgress.get(playerTeam).getFirst())){
                spawnBuilder.run(serverPlayer -> {
                    startHoleForPlayer(game, serverPlayer, playerHole, level, startHole);
                });
            }
        });
    }

    private static void startHoleForPlayer(IGamePhase game, ServerPlayer serverPlayer, String playerHole, ServerLevel level, CommandFunction<CommandSourceStack> startHole) {
        serverPlayer.addTag("in.golf.area");
        CompoundTag startArgs = new CompoundTag();
        startArgs.putString("hole", playerHole);
        CommandSourceStack commandSourceStack1 = game.server().createCommandSourceStack().withLevel(level)
                .withEntity(serverPlayer)
                .withSuppressedOutput().withPermission(3).withSource(game.server());
        executeFunction(startHole, startArgs, commandSourceStack1);
    }

    private void quickLoadGolfHole(GameTeam team, ServerLevel level,
                                   ResourceLocation pickedHole, BlockBox region,
                                   StructurePlaceSettings structureplacesettings, String regionKey, boolean ignoreAir) {
        Optional<StructureTemplate> structureTemplate = level.getStructureManager().get(pickedHole);
        if(structureTemplate.isPresent()){
            StructurePlaceSettings teamSettings = structureplacesettings.copy();
            if(team.key().id().equalsIgnoreCase("blue")){
                teamSettings.setRotation(Rotation.CLOCKWISE_180);
                teamSettings.setRotationPivot(new BlockPos(9, 0, 9));
            }
            teamSettings.setIgnoreEntities(true);
            if(!ignoreAir) {
                teamSettings.clearProcessors();
            }
            boolean hasImported = structureTemplate
                    .get().placeInWorld(level, region.min().offset(0, -4, 0),
                            region.min(), teamSettings, level.getRandom(), 2);
            if(hasImported){
                LOGGER.info("Have placed {} in region {}", pickedHole, regionKey);
            }
        } else {
            LOGGER.warn("Unable to load structure {}", pickedHole);
        }
    }

    private void loadGolfHole(IGamePhase game, GameTeam team, ServerLevel level, ResourceLocation pickedHole,
                              StructurePlaceSettings structureplacesettings, BlockBox region, String regionKey,
                              String holeNumber, CommandFunction<CommandSourceStack> changeHoleNumber,
                              CommandSourceStack commandSourceStack, boolean renameHole) {
        Optional<StructureTemplate> structureTemplate = level.getStructureManager().get(pickedHole);
        if(structureTemplate.isPresent()){
            StructurePlaceSettings teamSettings = structureplacesettings.copy();
            if(team.key().id().equalsIgnoreCase("blue")){
                teamSettings.setRotation(Rotation.CLOCKWISE_180);
                teamSettings.setRotationPivot(new BlockPos(9, 0, 9));
            }
           boolean hasImported = structureTemplate
                    .get().placeInWorld(level, region.min().offset(0, -3, 0),
                            region.min(), teamSettings, level.getRandom(), 2);
           if(hasImported){
               if(renameHole){
                   game.scheduler().runAfterSeconds(5, () -> {
//                       CompoundTag args = new CompoundTag();
//                       args.putString("region", regionKey);
//                       args.putString("old", potentialHoles.get(pickedHole));
//                       args.putString("new", holeNumber);
//                       executeFunction(changeHoleNumber, args, commandSourceStack);
                       // Run it again, just in-case
//                       executeFunction(changeHoleNumber, args, commandSourceStack);
                   });
               }
               LOGGER.info("Have placed {} in region {}", pickedHole, regionKey);
           }
        } else {
            LOGGER.warn("Unable to load structure {}", pickedHole);
        }
    }

    private static void executeFunction(CommandFunction<CommandSourceStack> changeHoleNumber, CompoundTag args, CommandSourceStack commandSourceStack) {
        try {
            InstantiatedFunction<CommandSourceStack> instantiate = changeHoleNumber.instantiate(args, commandSourceStack.dispatcher());
            Commands.executeCommandInContext(commandSourceStack, (consumer) -> {
                ExecutionContext.queueInitialFunctionCall(consumer, instantiate, commandSourceStack, CommandResultCallback.EMPTY);
            });
        } catch (Exception e){}
    }

    public record HoleConfig(String oldName, String newName) {}
}
