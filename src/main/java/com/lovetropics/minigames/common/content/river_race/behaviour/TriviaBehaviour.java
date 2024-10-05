package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.river_race.TriviaEvents;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlockEntity;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.network.trivia.ShowTriviaMessage;
import com.lovetropics.minigames.common.core.network.trivia.TriviaAnswerResponseMessage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class TriviaBehaviour implements IGameBehavior {

    public static final MapCodec<TriviaBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.nonEmptyList(TriviaZone.CODEC.listOf()).fieldOf("zones").forGetter(TriviaBehaviour::zones),
            Codec.INT.optionalFieldOf("question_lockout", 30).forGetter(TriviaBehaviour::questionLockout)
    ).apply(i, TriviaBehaviour::new));
    private final List<TriviaZone> zones;
    private final int questionLockout;
    private HashMap<Long, BlockPos> lockedOutTriviaBlocks = new HashMap<>();

    public TriviaBehaviour(List<TriviaZone> zones, int questionLockout) {
        this.zones = zones;
        this.questionLockout = questionLockout;
    }


    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        BlockBox spawnRegion = game.mapRegions().getOrThrow("spawn");
        events.listen(GamePlayerEvents.SPAWN, (playerId, spawn, role) -> {
            BlockPos floorPos = spawnRegion.sample(game.level().getRandom());
            spawn.teleportTo(game.level(), floorPos.above());
        });
        events.listen(GamePhaseEvents.TICK, () -> {
            Set<Long> longs = lockedOutTriviaBlocks.keySet();
            for (Long l : longs) {
                if(System.currentTimeMillis() >= l){
                    BlockPos blockPos = lockedOutTriviaBlocks.get(l);
                    BlockEntity blockEntity = game.level().getBlockEntity(blockPos);
                    if(blockEntity instanceof TriviaBlockEntity triviaBlockEntity){
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
            if (world.getBlockEntity(pos) instanceof TriviaBlockEntity triviaBlockEntity) {
                if (!triviaBlockEntity.hasQuestion()) {
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
                            //TODO: Prevent this selecting questions that are already in use.. maybe some kind of id?
                            TriviaZone triviaZone = first.get();
                            List<TriviaQuestion> filteredDifficultyList = triviaZone.questionPool.stream()
                                    .filter(question -> question.difficulty()
                                            .equalsIgnoreCase(triviaBlockEntity.getTriviaBlockType().difficulty()))
                                    .toList();
                            //Bad...
                            if (!filteredDifficultyList.isEmpty()) {
                                TriviaQuestion question = filteredDifficultyList.get(new Random().nextInt(filteredDifficultyList.size()));
                                triviaBlockEntity.setQuestion(question);
                            } else {
                                player.sendSystemMessage(Component.literal("Failed to pick a question from the question pool for this trivia block").withStyle(ChatFormatting.RED));
                            }
                        }
                    }
                }
                if (triviaBlockEntity.getQuestion() != null) {
                    PacketDistributor.sendToPlayer(player, new ShowTriviaMessage(pos, triviaBlockEntity.getQuestion(), triviaBlockEntity.getState()));
                }
                return InteractionResult.SUCCESS_NO_ITEM_USED;
            }
            return InteractionResult.PASS;
        });
        events.listen(TriviaEvents.ANSWER_TRIVIA_BLOCK_QUESTION, (ServerPlayer player, ServerLevel world,
                                                                  BlockPos pos,
                                                                  TriviaBlockEntity triviaBlockEntity,
                                                                  TriviaQuestion question, String answer) -> {
            if (question != null && !triviaBlockEntity.getState().lockedOut()) {
                TriviaQuestion.TriviaQuestionAnswer answerObj = question.getAnswer(answer);
                if (answerObj != null) {
                    if (answerObj.correct()) {
                        //TODO: Make this a translation key
                        player.sendSystemMessage(Component.
                                literal("Correct! Do something here!")
                                .withStyle(ChatFormatting.GREEN));
                        triviaBlockEntity.markAsCorrect();
                        PacketDistributor.sendToPlayer(player, new TriviaAnswerResponseMessage(pos, triviaBlockEntity.getState()));
                        return true;
                    } else {
                        //TODO: Make this a translation key
                        player.sendSystemMessage(Component.
                                literal("Incorrect! This question is now locked out for " + questionLockout() + " seconds!")
                                .withStyle(ChatFormatting.RED));
                        lockedOutTriviaBlocks.put(triviaBlockEntity.lockout(questionLockout()), pos);
                        PacketDistributor.sendToPlayer(player, new TriviaAnswerResponseMessage(pos, triviaBlockEntity.getState()));
                    }
                }
            }
            return false;
        });
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
