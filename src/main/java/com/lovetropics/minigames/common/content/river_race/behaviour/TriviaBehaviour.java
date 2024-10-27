package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.minigames.common.content.river_race.RiverRaceTexts;
import com.lovetropics.minigames.common.content.river_race.TriviaEvents;
import com.lovetropics.minigames.common.content.river_race.block.HasTrivia;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlock;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.util.GameScheduler;
import com.lovetropics.minigames.common.core.network.trivia.ShowTriviaMessage;
import com.lovetropics.minigames.common.core.network.trivia.TriviaAnswerResponseMessage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class TriviaBehaviour implements IGameBehavior {

    public static final MapCodec<TriviaBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.nonEmptyList(TriviaZone.CODEC.listOf()).fieldOf("zones").forGetter(TriviaBehaviour::zones),
            Codec.INT.optionalFieldOf("question_lockout", 30).forGetter(TriviaBehaviour::questionLockout)
    ).apply(i, TriviaBehaviour::new));

    private static final TagKey<Block> STAINED_GLASS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("lt", "all_stained_glass"));

    private final List<TriviaZone> zones;
    private final int questionLockout;
    private final GameScheduler scheduler = new GameScheduler();
    private final Map<Long, BlockPos> lockedOutTriviaBlocks = new ConcurrentHashMap<>();
    private final List<TriviaQuestion> usedQuestions = new ArrayList<>();

    public TriviaBehaviour(List<TriviaZone> zones, int questionLockout) {
        this.zones = zones;
        this.questionLockout = questionLockout;
    }


    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GamePhaseEvents.TICK, () -> {
            scheduler.tick();
            Set<Long> longs = lockedOutTriviaBlocks.keySet();
            for (Long l : longs) {
                if(game.level().getGameTime() >= l){
                    BlockPos blockPos = lockedOutTriviaBlocks.get(l);
                    BlockEntity blockEntity = game.level().getBlockEntity(blockPos);
                    if(blockEntity instanceof HasTrivia triviaBlockEntity){
                        triviaBlockEntity.unlock();
                        lockedOutTriviaBlocks.remove(l);
                    }
                }
            }
        });
        events.listen(GamePlayerEvents.USE_BLOCK, (ServerPlayer player, ServerLevel world,
                                                   BlockPos pos, InteractionHand hand, BlockHitResult traceResult) -> {
            if (hand == InteractionHand.OFF_HAND) {
                return InteractionResult.PASS;
            }
            if (world.getBlockEntity(pos) instanceof HasTrivia hasTrivia && !hasTrivia.getState().isAnswered()) {
                if (!hasTrivia.hasQuestion()) {
                    String inRegion = null;
                    for (String region : game.mapRegions().keySet()) {
                        if (region.startsWith("zone_")) {
                            if (game.mapRegions().getOrThrow(region).contains(pos)) {
                                inRegion = region;
                                break;
                            }
                        }
                    }
                    if (inRegion != null) {
                        int zone = Integer.parseInt(inRegion.split("_")[1]);
                        Optional<TriviaZone> first = zones.stream().filter(triviaZone -> triviaZone.zone == zone).findFirst();
                        if (first.isPresent()) {
                            TriviaZone triviaZone = first.get();
                            List<TriviaQuestion> filteredDifficultyList = triviaZone.questionPool.stream()
                                    .filter(question -> question.difficulty()
                                            .equalsIgnoreCase(hasTrivia.getTriviaType().difficulty()))
                                    .filter(question -> !usedQuestions.contains(question))
                                    .toList();
                            //Bad...
                            if (!filteredDifficultyList.isEmpty()) {
                                TriviaQuestion question = filteredDifficultyList.get(new Random().nextInt(filteredDifficultyList.size()));
                                usedQuestions.add(question);
                                hasTrivia.setQuestion(question);
                            } else {
                                player.sendSystemMessage(Component.literal("Failed to pick a question from the question pool for this trivia block").withStyle(ChatFormatting.RED));
                            }
                        }
                    }
                }
                if (hasTrivia.getQuestion() != null) {
                    PacketDistributor.sendToPlayer(player, new ShowTriviaMessage(pos, hasTrivia.getQuestion(), hasTrivia.getState()));
                }
                return InteractionResult.SUCCESS_NO_ITEM_USED;
            }
            return InteractionResult.PASS;
        });
        events.listen(TriviaEvents.ANSWER_TRIVIA_BLOCK_QUESTION, (ServerPlayer player, ServerLevel world,
                                                                  BlockPos pos,
                                                                  HasTrivia triviaBlockEntity,
                                                                  TriviaQuestion question, String answer) -> {
            if (question != null && !triviaBlockEntity.getState().lockedOut()) {
                TriviaQuestion.TriviaQuestionAnswer answerObj = question.getAnswer(answer);
                if (answerObj != null) {
                    if (answerObj.correct()) {
                        player.sendSystemMessage(RiverRaceTexts.CORRECT_ANSWER);
                        if(triviaBlockEntity.getTriviaType() == TriviaBlock.TriviaType.GATE){
                            world.destroyBlock(pos, false);
                            Block blockType = null;
                            findNeighboursOfTypeAndDestroy(scheduler, world, pos, blockType);
                        } else if(triviaBlockEntity.getTriviaType() == TriviaBlock.TriviaType.COLLECTABLE){
                            String inRegion = null;
                            for (String region : game.mapRegions().keySet()) {
                                if (region.startsWith("zone_")) {
                                    if (game.mapRegions().getOrThrow(region).contains(pos)) {
                                        inRegion = region;
                                        break;
                                    }
                                }
                            }
                            if(inRegion != null) {
                                CollectablesBehaviour collectables = game.state().getOrNull(CollectablesBehaviour.COLLECTABLES);
                                if (collectables != null) {
                                    CollectablesBehaviour.Collectable collectable = collectables.getCollectableForZone(inRegion);
                                    if(collectable != null){
                                        collectables.givePlayerCollectable(game, collectable, player);
                                    }
                                }
                            }
                        }
                        triviaBlockEntity.markAsCorrect();
                        PacketDistributor.sendToPlayer(player, new TriviaAnswerResponseMessage(pos, triviaBlockEntity.getState()));
                    } else {
                        player.sendSystemMessage(RiverRaceTexts.INCORRECT_ANSWER.apply(questionLockout()));
                        lockedOutTriviaBlocks.put(triviaBlockEntity.lockout(questionLockout()), pos);
                        PacketDistributor.sendToPlayer(player, new TriviaAnswerResponseMessage(pos, triviaBlockEntity.getState()));
                    }
                    if (player instanceof final ServerPlayer serverPlayer) {
                        game.invoker(RiverRaceEvents.ANSWER_QUESTION).onAnswer(serverPlayer, triviaBlockEntity.getTriviaType(), answerObj.correct());
                    }
                    return answerObj.correct();
                }
            }
            return false;
        });
    }

    private static void findNeighboursOfTypeAndDestroy(GameScheduler scheduler, ServerLevel world, BlockPos pos, Block blockType) {
        for (Direction direction : Direction.values()) {
            BlockPos relative = pos.relative(direction);
            BlockState blockState = world.getBlockState(relative);
            if(!blockState.isAir()){
                if(blockType == null){
                    blockType = blockState.getBlock();
                }
                if(blockState.is(blockType)){
                    world.destroyBlock(relative, false);
                    Block finalBlockType = blockType;
                    scheduler.delayedTickEvent("gateDestroy" + relative, () -> {
                        findNeighboursOfTypeAndDestroy(scheduler, world, relative, finalBlockType);
                    }, 10);
                }
            }
        }
    }

    public List<TriviaZone> zones() {
        return zones;
    }

    public int questionLockout() {
        return questionLockout;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TriviaBehaviour) obj;
        return Objects.equals(this.zones, that.zones) &&
                this.questionLockout == that.questionLockout;
    }

    @Override
    public int hashCode() {
        return Objects.hash(zones, questionLockout);
    }

    @Override
    public String toString() {
        return "TriviaBehaviour[" +
                "zones=" + zones + ", " +
                "questionLockout=" + questionLockout + ']';
    }


    public record TriviaZone(int zone, List<TriviaQuestion> questionPool) {
        public static final Codec<TriviaZone> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("zone_id").forGetter(TriviaZone::zone),
                ExtraCodecs.nonEmptyList(TriviaQuestion.CODEC.listOf()).fieldOf("questions").forGetter(TriviaZone::questionPool)
        ).apply(i, TriviaZone::new));
    }

    public record TriviaQuestion(String question, List<TriviaQuestionAnswer> answers, String difficulty) {
        public static final Codec<TriviaQuestion> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("question").forGetter(TriviaQuestion::question),
                ExtraCodecs.nonEmptyList(TriviaQuestionAnswer.CODEC.listOf()).fieldOf("answers").forGetter(TriviaQuestion::answers),
                Codec.STRING.fieldOf("difficulty").forGetter(TriviaQuestion::difficulty)
        ).apply(i, TriviaQuestion::new));

        public static final StreamCodec<ByteBuf, TriviaQuestion> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, TriviaQuestion::question,
                TriviaQuestionAnswer.STREAM_CODEC.apply(ByteBufCodecs.list()), TriviaQuestion::answers,
                ByteBufCodecs.STRING_UTF8, TriviaQuestion::difficulty,
                TriviaQuestion::new
        );

        public record TriviaQuestionAnswer(String text, boolean correct) {
            public static final StreamCodec<ByteBuf, TriviaQuestionAnswer> STREAM_CODEC = StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, TriviaQuestionAnswer::text,
                    ByteBufCodecs.BOOL, TriviaQuestionAnswer::correct,
                    TriviaQuestionAnswer::new
            );
            public static final Codec<TriviaQuestionAnswer> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Codec.STRING.fieldOf("text").forGetter(TriviaQuestionAnswer::text),
                    Codec.BOOL.fieldOf("correct").forGetter(TriviaQuestionAnswer::correct)
            ).apply(i, TriviaQuestionAnswer::new));
        }


        @Nullable
        public TriviaBehaviour.TriviaQuestion.TriviaQuestionAnswer getAnswer(String text) {
            for (TriviaQuestionAnswer answer : answers) {
                if (answer.text().equalsIgnoreCase(text)) {
                    return answer;
                }
            }
            return null;
        }
    }
}
