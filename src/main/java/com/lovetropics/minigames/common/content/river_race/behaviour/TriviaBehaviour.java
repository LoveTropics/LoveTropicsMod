package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.content.river_race.RiverRaceTexts;
import com.lovetropics.minigames.common.content.river_race.TriviaEvents;
import com.lovetropics.minigames.common.content.river_race.block.HasTrivia;
import com.lovetropics.minigames.common.content.river_race.block.TriviaChestBlockEntity;
import com.lovetropics.minigames.common.content.river_race.block.TriviaDifficulty;
import com.lovetropics.minigames.common.content.river_race.block.TriviaType;
import com.lovetropics.minigames.common.content.river_race.event.RiverRaceEvents;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.core.game.util.GameScheduler;
import com.lovetropics.minigames.common.core.network.trivia.ShowTriviaMessage;
import com.lovetropics.minigames.common.core.network.trivia.TriviaAnswerResponseMessage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public final class TriviaBehaviour implements IGameBehavior {

    public static final MapCodec<TriviaBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.nonEmptyList(TriviaZone.CODEC.listOf()).fieldOf("zones").forGetter(b -> b.zones),
            Codec.INT.optionalFieldOf("question_lockout", 30).forGetter(b -> b.questionLockout),
            Direction.CODEC.fieldOf("forward_direction").forGetter(b -> b.forwardDirection)
    ).apply(i, TriviaBehaviour::new));

    private static final TagKey<Block> STAINED_GLASS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("lt", "all_stained_glass"));

    private final List<TriviaZone> zones;
    private final int questionLockout;
    private final Direction forwardDirection;
    private final Map<Long, BlockPos> lockedOutTriviaBlocks = new ConcurrentHashMap<>();

    private final Set<TriviaQuestion> usedQuestions = new ObjectOpenHashSet<>();
    private final Long2ObjectMap<TriviaQuestion> questionsByZoneLocalPos = new Long2ObjectOpenHashMap<>();

    private final List<ZoneRegion> zoneRegions = new ArrayList<>();

    public TriviaBehaviour(List<TriviaZone> zones, int questionLockout, Direction forwardDirection) {
        this.zones = zones;
        this.questionLockout = questionLockout;
        this.forwardDirection = forwardDirection;
    }

    @Nullable
    private ZoneRegion getZoneByPos(BlockPos pos) {
        for (ZoneRegion region : zoneRegions) {
            if (region.box.contains(pos)) {
                return region;
            }
        }
        return null;
    }

    // Mirrored for the opposite team side so that equivalent trivia blocks can reuse the same question
    private long getTriviaQuestionKey(ZoneRegion zone, BlockPos pos) {
        pos = pos.subtract(zone.box.min());
        BlockPos size = zone.box.size();
        if (forwardDirection.getAxis() == Direction.Axis.X) {
			if (pos.getZ() >= size.getZ() / 2) {
                pos = new BlockPos(pos.getX(), pos.getY(), size.getZ() - 1 - pos.getZ());
            }
        } else {
            if (pos.getX() >= size.getX() / 2) {
                pos = new BlockPos(size.getX() - 1 - pos.getX(), pos.getY(), pos.getZ());
            }
        }
        return pos.asLong();
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        for (TriviaZone zone : zones) {
            Collection<BlockBox> regions = game.mapRegions().get(zone.regionKey());
            for (BlockBox region : regions) {
                zoneRegions.add(new ZoneRegion(region, zone));
            }
        }

        events.listen(GamePhaseEvents.TICK, () -> {
            Set<Long> longs = lockedOutTriviaBlocks.keySet();
            for (Long l : longs) {
                if (game.level().getGameTime() >= l) {
                    BlockPos blockPos = lockedOutTriviaBlocks.get(l);
                    BlockEntity blockEntity = game.level().getBlockEntity(blockPos);
                    if (blockEntity instanceof HasTrivia triviaBlockEntity) {
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
        events.listen(TriviaEvents.ANSWER_TRIVIA_BLOCK_QUESTION, (player, pos, triviaBlockEntity, question, answer) -> {
            if (triviaBlockEntity.getState().lockedOut()) {
                return false;
            }
            if (answer.correct()) {
                player.sendSystemMessage(RiverRaceTexts.CORRECT_ANSWER);
                if (triviaBlockEntity.markAsCorrect()) {
                    game.invoker(RiverRaceEvents.QUESTION_COMPLETED).onAnswer(player, triviaBlockEntity.getTriviaType(), pos);
                }
                PacketDistributor.sendToPlayer(player, new TriviaAnswerResponseMessage(pos, triviaBlockEntity.getState()));
            } else {
                player.sendSystemMessage(RiverRaceTexts.INCORRECT_ANSWER.apply(questionLockout));
                lockedOutTriviaBlocks.put(triviaBlockEntity.lockout(questionLockout), pos);
                PacketDistributor.sendToPlayer(player, new TriviaAnswerResponseMessage(pos, triviaBlockEntity.getState()));
            }
            return answer.correct();
        });
        events.listen(RiverRaceEvents.QUESTION_COMPLETED, (player, triviaType, triviaPos) -> {
            switch (triviaType) {
                case GATE -> {
                    game.level().destroyBlock(triviaPos, false);
                    findNeighboursOfTypeAndDestroy(game.scheduler(), game.level(), triviaPos, null);
                }
                case COLLECTABLE -> giveCollectableFromBlock(game, player, triviaPos);
                case REWARD -> {
					if (game.level().getBlockEntity(triviaPos) instanceof TriviaChestBlockEntity chest) {
                        player.openMenu(chest);
                    }
                }
            }
        });
    }

    private InteractionResult useTriviaBlock(IGamePhase game, ServerPlayer player, BlockPos pos, HasTrivia hasTrivia) {
        if (!game.participants().contains(player)) {
            return InteractionResult.FAIL;
        }
		if (hasTrivia.getState().isAnswered()) {
            return useUnlockedTriviaBlock(game, player, pos, hasTrivia);
        } else {
            return useLockedTriviaBlock(game, player, pos, hasTrivia);
        }
    }

    private InteractionResult useUnlockedTriviaBlock(IGamePhase game, ServerPlayer player, BlockPos pos, HasTrivia hasTrivia) {
        return switch (hasTrivia.getTriviaType()) {
            case GATE, VICTORY -> {
                player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
                player.sendSystemMessage(RiverRaceTexts.TRIVIA_BLOCK_ALREADY_USED, true);
                yield InteractionResult.FAIL;
            }
            case COLLECTABLE ->
					giveCollectableFromBlock(game, player, pos) ? InteractionResult.SUCCESS_NO_ITEM_USED : InteractionResult.FAIL;
            // Let the player open the chest
            case REWARD -> InteractionResult.PASS;
        };
    }

    private InteractionResult useLockedTriviaBlock(IGamePhase game, ServerPlayer player, BlockPos pos, HasTrivia hasTrivia) {
        TriviaQuestion question = hasTrivia.getQuestion();
        if (question == null) {
            TriviaQuestion pickedQuestion = pickTriviaForPos(game, pos, hasTrivia.getTriviaType());
            if (pickedQuestion != null) {
                usedQuestions.add(pickedQuestion);
                hasTrivia.setQuestion(pickedQuestion);
                question = pickedQuestion;
            } else {
                player.sendSystemMessage(Component.literal("Failed to pick a question from the question pool for this trivia block").withStyle(ChatFormatting.RED));
                return InteractionResult.FAIL;
            }
        }
        PacketDistributor.sendToPlayer(player, new ShowTriviaMessage(pos, question, hasTrivia.getState()));
        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }

    @Nullable
    private TriviaQuestion pickTriviaForPos(IGamePhase game, BlockPos pos, TriviaType triviaType) {
        ZoneRegion region = getZoneByPos(pos);
		if (region == null) {
			return null;
		}
        return questionsByZoneLocalPos.computeIfAbsent(getTriviaQuestionKey(region, pos), k -> {
            List<TriviaQuestion> questionPool = region.zone.questionsByDifficulty(triviaType.difficulty())
                    .filter(question -> !usedQuestions.contains(question))
                    .toList();
            return Util.getRandomSafe(questionPool, game.random()).orElse(null);
        });
	}

    private boolean giveCollectableFromBlock(IGamePhase game, ServerPlayer player, BlockPos pos) {
        CollectablesBehaviour.Collectable collectable = getCollectableFromPos(game, pos);
        if (collectable == null) {
			return false;
		}
		ServerPlayer playerWithCollectable = getPlayerWithCollectable(game, player, collectable);
		if (playerWithCollectable != null) {
			player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
			if (playerWithCollectable == player) {
				player.sendSystemMessage(RiverRaceTexts.YOU_HAVE_COLLECTABLE, true);
			} else {
				player.sendSystemMessage(RiverRaceTexts.PLAYER_HAS_COLLECTABLE.apply(playerWithCollectable.getDisplayName()), true);
			}
			return false;
		}
		if (!player.addItem(collectable.collectable().copy())) {
            player.playNotifySound(SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
            player.sendSystemMessage(MinigameTexts.INVENTORY_FULL, true);
            return false;
        }
        player.displayClientMessage(RiverRaceTexts.COLLECTABLE_GIVEN, false);
        return true;
	}

    @Nullable
    private CollectablesBehaviour.Collectable getCollectableFromPos(IGamePhase game, BlockPos pos) {
        ZoneRegion inZone = getZoneByPos(pos);
        CollectablesBehaviour collectables = game.state().getOrNull(CollectablesBehaviour.COLLECTABLES);
		if (inZone != null && collectables != null) {
			return collectables.getCollectableForZone(inZone.zone.regionKey());
		}
		return null;
	}

    @Nullable
    private ServerPlayer getPlayerWithCollectable(IGamePhase game, ServerPlayer player, CollectablesBehaviour.Collectable collectable) {
        TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
        if (teams == null) {
            return null;
        }
        for (ServerPlayer otherPlayer : teams.getPlayersOnSameTeam(player)) {
			if (otherPlayer.getInventory().contains(collectable.collectable())) {
				return otherPlayer;
			}
        }
        return null;
    }

    private static void findNeighboursOfTypeAndDestroy(GameScheduler scheduler, ServerLevel world, BlockPos pos, @Nullable Block blockType) {
        for (Direction direction : Direction.values()) {
            BlockPos relative = pos.relative(direction);
            BlockState blockState = world.getBlockState(relative);
            if (!blockState.isAir()) {
                if (blockType == null) {
                    blockType = blockState.getBlock();
                }
                if (blockState.is(blockType)) {
                    world.destroyBlock(relative, false);
                    Block finalBlockType = blockType;
                    scheduler.runAfterSeconds(0.5f, () -> {
                        findNeighboursOfTypeAndDestroy(scheduler, world, relative, finalBlockType);
                    });
                }
            }
        }
    }

    public record TriviaZone(String regionKey, List<TriviaQuestion> questionPool) {
        public static final Codec<TriviaZone> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("zone_region").forGetter(TriviaZone::regionKey),
                ExtraCodecs.nonEmptyList(TriviaQuestion.CODEC.listOf()).fieldOf("questions").forGetter(TriviaZone::questionPool)
        ).apply(i, TriviaZone::new));

        private Stream<TriviaQuestion> questionsByDifficulty(TriviaDifficulty difficulty) {
            return questionPool.stream().filter(question -> question.difficulty() == difficulty);
        }
    }

    public record TriviaQuestion(String question, List<TriviaQuestionAnswer> answers, TriviaDifficulty difficulty) {
        private static final Codec<List<TriviaQuestionAnswer>> SHUFFLED_ANSWERS_CODEC = ExtraCodecs.nonEmptyList(TriviaQuestionAnswer.CODEC.listOf())
                .xmap(TriviaQuestion::shuffle, TriviaQuestion::shuffle);

        public static final Codec<TriviaQuestion> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.STRING.fieldOf("question").forGetter(TriviaQuestion::question),
                SHUFFLED_ANSWERS_CODEC.fieldOf("answers").forGetter(TriviaQuestion::answers),
                TriviaDifficulty.CODEC.fieldOf("difficulty").forGetter(TriviaQuestion::difficulty)
        ).apply(i, TriviaQuestion::new));

        public static final StreamCodec<ByteBuf, TriviaQuestion> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, TriviaQuestion::question,
                TriviaQuestionAnswer.STREAM_CODEC.apply(ByteBufCodecs.list()), TriviaQuestion::answers,
                TriviaDifficulty.STREAM_CODEC, TriviaQuestion::difficulty,
                TriviaQuestion::new
        );

        private static <T> List<T> shuffle(List<T> values) {
            List<T> result = new ArrayList<>(values);
            Util.shuffle(result, RandomSource.create());
            return result;
        }

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
