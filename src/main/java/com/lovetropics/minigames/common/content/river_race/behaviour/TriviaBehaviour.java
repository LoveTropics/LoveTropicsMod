package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.BlockBox;
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
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.GameScheduler;
import com.lovetropics.minigames.common.core.network.trivia.ShowTriviaMessage;
import com.lovetropics.minigames.common.core.network.trivia.TriviaAnswerResponseMessage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class TriviaBehaviour implements IGameBehavior {

    public static final MapCodec<TriviaBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.nonEmptyList(TriviaZone.CODEC.listOf()).fieldOf("zones").forGetter(b -> List.copyOf(b.zonesById.values())),
            Codec.INT.optionalFieldOf("question_lockout", 30).forGetter(b -> b.questionLockout)
    ).apply(i, TriviaBehaviour::new));

    private static final TagKey<Block> STAINED_GLASS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("lt", "all_stained_glass"));

    private final Int2ObjectMap<TriviaZone> zonesById = new Int2ObjectOpenHashMap<>();
    private final int questionLockout;
    private final GameScheduler scheduler = new GameScheduler();
    private final Map<Long, BlockPos> lockedOutTriviaBlocks = new ConcurrentHashMap<>();
    private final Set<TriviaQuestion> usedQuestions = new ObjectOpenHashSet<>();

    private final List<ZoneRegion> zoneRegions = new ArrayList<>();

    public TriviaBehaviour(List<TriviaZone> zones, int questionLockout) {
        for (TriviaZone zone : zones) {
            zonesById.put(zone.id, zone);
        }
        this.questionLockout = questionLockout;
    }

    @Nullable
    private TriviaZone getZoneByPos(BlockPos pos) {
        for (ZoneRegion region : zoneRegions) {
            if (region.box.contains(pos)) {
                return region.zone;
            }
        }
        return null;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        for (TriviaZone zone : zonesById.values()) {
            Collection<BlockBox> regions = game.mapRegions().get(zone.regionKey());
            for (BlockBox region : regions) {
                zoneRegions.add(new ZoneRegion(region, zone));
            }
        }

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
        events.listen(GamePlayerEvents.USE_BLOCK, (player, world, pos, hand, traceResult) -> {
            if (hand == InteractionHand.OFF_HAND) {
                return InteractionResult.PASS;
            }
            if (world.getBlockEntity(pos) instanceof HasTrivia hasTrivia) {
                return useTriviaBlock(game, player, pos, hasTrivia);
            }
            return InteractionResult.PASS;
        });
        events.listen(TriviaEvents.ANSWER_TRIVIA_BLOCK_QUESTION, (player, level, pos, triviaBlockEntity, question, answer) -> {
            if (question == null || triviaBlockEntity.getState().lockedOut()) {
                return false;
            }
            TriviaQuestion.TriviaQuestionAnswer answerObj = question.getAnswer(answer);
            if (answerObj != null) {
                if (answerObj.correct()) {
                    player.sendSystemMessage(RiverRaceTexts.CORRECT_ANSWER);
					switch (triviaBlockEntity.getTriviaType()) {
						case GATE -> {
							level.destroyBlock(pos, false);
							findNeighboursOfTypeAndDestroy(scheduler, level, pos, null);
						}
						case COLLECTABLE -> giveCollectableToPlayer(game, player, pos);
					}
                    triviaBlockEntity.markAsCorrect();
                    PacketDistributor.sendToPlayer(player, new TriviaAnswerResponseMessage(pos, triviaBlockEntity.getState()));
                } else {
                    player.sendSystemMessage(RiverRaceTexts.INCORRECT_ANSWER.apply(questionLockout));
                    lockedOutTriviaBlocks.put(triviaBlockEntity.lockout(questionLockout), pos);
                    PacketDistributor.sendToPlayer(player, new TriviaAnswerResponseMessage(pos, triviaBlockEntity.getState()));
                }
                game.invoker(RiverRaceEvents.ANSWER_QUESTION).onAnswer(player, triviaBlockEntity.getTriviaType(), answerObj.correct());
                return answerObj.correct();
            }
            return false;
        });
    }

    private InteractionResult useTriviaBlock(IGamePhase game, ServerPlayer player, BlockPos pos, HasTrivia hasTrivia) {
        if (!game.participants().contains(player)) {
            return InteractionResult.FAIL;
        }
        if (!hasTrivia.hasQuestion()) {
            TriviaQuestion pickedQuestion = pickTriviaForPos(game, pos, hasTrivia.getTriviaType());
            if (pickedQuestion != null) {
                usedQuestions.add(pickedQuestion);
                hasTrivia.setQuestion(pickedQuestion);
            } else {
                player.sendSystemMessage(Component.literal("Failed to pick a question from the question pool for this trivia block").withStyle(ChatFormatting.RED));
            }
        }
        if (hasTrivia.getQuestion() != null && !hasTrivia.getState().isAnswered()) {
            PacketDistributor.sendToPlayer(player, new ShowTriviaMessage(pos, hasTrivia.getQuestion(), hasTrivia.getState()));
        } else {
            if (hasTrivia.getTriviaType() == TriviaBlock.TriviaType.COLLECTABLE){
                giveCollectableToPlayer(game, player, pos);
            }
        }
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    @Nullable
    private TriviaQuestion pickTriviaForPos(IGamePhase game, BlockPos pos, TriviaBlock.TriviaType triviaType) {
        TriviaZone triviaZone = getZoneByPos(pos);
        if (triviaZone != null) {
            List<TriviaQuestion> questionPool = triviaZone.questionsByDifficulty(triviaType.difficulty())
                    .filter(question -> !usedQuestions.contains(question))
                    .toList();
            return Util.getRandomSafe(questionPool, game.random()).orElse(null);
        }
        return null;
    }

    private void giveCollectableToPlayer(IGamePhase game, ServerPlayer player, BlockPos pos) {
        TriviaZone inZone = getZoneByPos(pos);
        CollectablesBehaviour collectables = game.game().instanceState().getOrNull(CollectablesBehaviour.COLLECTABLES);
        if (inZone == null || collectables == null) {
            return;
        }
        CollectablesBehaviour.Collectable collectable = collectables.getCollectableForZone(inZone.regionKey());
        if (collectable != null) {
            collectables.givePlayerCollectable(game, collectable, player);
        }
    }

    private static void findNeighboursOfTypeAndDestroy(GameScheduler scheduler, ServerLevel world, BlockPos pos, @Nullable Block blockType) {
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

    public record TriviaZone(int id, List<TriviaQuestion> questionPool) {
        public static final Codec<TriviaZone> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("zone_id").forGetter(TriviaZone::id),
                ExtraCodecs.nonEmptyList(TriviaQuestion.CODEC.listOf()).fieldOf("questions").forGetter(TriviaZone::questionPool)
        ).apply(i, TriviaZone::new));

        private Stream<TriviaQuestion> questionsByDifficulty(String difficulty) {
            return questionPool.stream()
                    .filter(question -> question.difficulty().equalsIgnoreCase(difficulty));
        }

        public String regionKey() {
            return "zone_" + id;
        }
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

    private record ZoneRegion(BlockBox box, TriviaZone zone) {
    }
}
