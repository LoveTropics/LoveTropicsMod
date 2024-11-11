package com.lovetropics.minigames.common.content.connect4;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWorldEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.lovetropics.minigames.common.util.SequentialList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ConnectFourBehavior implements IGameBehavior {
    public static final MapCodec<ConnectFourBehavior> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            Codec.unboundedMap(GameTeamKey.CODEC, GameBlock.CODEC.codec()).fieldOf("team_blocks").forGetter(c -> c.teamBlocks),
            Codec.STRING.fieldOf("placing_region").forGetter(c -> c.placingRegionKey),
            MoreCodecs.BLOCK_STATE.fieldOf("separator").forGetter(c -> c.separator),
            MoreCodecs.BLOCK_STATE.fieldOf("blocker").forGetter(c -> c.blocker),
            Codec.INT.fieldOf("grid_width").forGetter(c -> c.width),
            Codec.INT.fieldOf("grid_height").forGetter(c -> c.height),
            Codec.INT.optionalFieldOf("connect", 4).forGetter(c -> c.connectAmount)
    ).apply(in, ConnectFourBehavior::new));

    private final Map<GameTeamKey, GameBlock> teamBlocks;
    private final String placingRegionKey;
    private final BlockState separator;
    private final BlockState blocker;

    private final int width, height, connectAmount;

    public ConnectFourBehavior(Map<GameTeamKey, GameBlock> teamBlocks, String placingRegionKey, BlockState separator, BlockState blocker, int width, int height, int connectAmount) {
        this.teamBlocks = teamBlocks;
        this.placingRegionKey = placingRegionKey;
        this.separator = separator;
        this.blocker = blocker;
        this.width = width;
        this.height = height;
        this.connectAmount = connectAmount;
    }

    private IGamePhase game;

    @Nullable
    private PendingGate pendingGate;

    private SequentialList<PlayingTeam> playingTeams;

    private TeamState teams;
    private BlockBox placingRegion;

    private GameTeamKey[][] pieces;
    private int placedPieces;

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        placingRegion = game.mapRegions().getOrThrow(placingRegionKey);
        teams = game.instanceState().getOrThrow(TeamState.KEY);

        pieces = new GameTeamKey[width][height];
        placedPieces = 0;

        events.listen(GamePhaseEvents.START, this::onStart);

        events.listen(GamePlayerEvents.PLACE_BLOCK, this::onPlaceBlock);
        events.listen(GamePlayerEvents.BREAK_BLOCK, (player, pos, state, hand) -> player.isCreative() ? InteractionResult.PASS : InteractionResult.FAIL);

        events.listen(GameWorldEvents.BLOCK_LANDED, this::onBlockLanded);
    }

    private void onStart() {
        var teams = new ArrayList<PlayingTeam>(this.teams.getTeamKeys().size());
        this.teams.getTeamKeys().forEach(key -> {
            var players = this.teams.getPlayersForTeam(key).stream().map(PlayerKey::from).toList();
            if (!players.isEmpty()) {
                teams.add(new PlayingTeam(key, new SequentialList<>(players, -1)));
            }
        });
        Collections.shuffle(teams);
        playingTeams = new SequentialList<>(teams, -1);

        nextPlayer();
    }

    private InteractionResult onPlaceBlock(ServerPlayer player, BlockPos pos, BlockState placed, BlockState placedOn, ItemStack placedItemStack) {
        if (player.isCreative()) return InteractionResult.PASS;

        if (!Objects.equals(playingTeams.current().players.current(), PlayerKey.from(player)) || !placingRegion.contains(pos))
            return InteractionResult.FAIL;

        var expected = teamBlocks.get(playingTeams.current().key).powder;
        if (expected != placed.getBlock()) return InteractionResult.FAIL;

        var below = pos.below();
        pendingGate = new PendingGate(below, player.level().getBlockState(below));
        player.level().setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);

        return InteractionResult.PASS;
    }

    private void onBlockLanded(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;

        var expected = teamBlocks.get(playingTeams.current().key);

        if (!state.is(expected.powder)) return;

        level.setBlock(pos, expected.solid.defaultBlockState(), Block.UPDATE_ALL);

        if (pendingGate != null) {
            level.setBlock(pendingGate.gatePosition(), pendingGate.gate(), Block.UPDATE_ALL);
            pendingGate = null;
        }

        level.setBlock(pos.above(), separator, Block.UPDATE_ALL);
        // Separator above, and gate above that
        if (placingRegion.contains(pos.getX(), pos.getY() + 3, pos.getZ())) {
            level.setBlock(pos.above(3), blocker, Block.UPDATE_ALL);
        }

        int x = (pos.getX() - Math.min(placingRegion.min().getX(), placingRegion.max().getX())) / 2;
        if (placingRegion.min().getX() == placingRegion.max().getX()) {
            x = (pos.getZ() - Math.min(placingRegion.min().getZ(), placingRegion.max().getZ())) / 2;
        }
        var column = pieces[x];
        int y;
        for (y = 0; y < column.length; y++) {
            if (column[y] == null) {
                column[y] = playingTeams.current().key();
                break;
            }
        }
        placedPieces++;

        var team = playingTeams.current().key();

        game.allPlayers().getPlayerBy(playingTeams.current().players().current()).setGlowingTag(false);

        if (checkWin(x, y, team)) {
            game.statistics().global().set(StatisticKey.WINNING_TEAM, team);
            GameTeam gameTeam = teams.getTeamByKey(team);
            game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(gameTeam);
            game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

            game.allPlayers().forEach(ServerPlayer::closeContainer);

            game.scheduler().runAfterSeconds(1.5f, () -> game.allPlayers().sendMessage(MinigameTexts.TEAM_WON.apply(gameTeam.config().styledName()).withStyle(ChatFormatting.GREEN), true));
            game.scheduler().runAfterSeconds(5, () -> game.requestStop(GameStopReason.finished()));
        } else {
            if (placedPieces == width * height) {
                game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

                game.scheduler().runAfterSeconds(1.5f, () -> game.allPlayers().sendMessage(MinigameTexts.NOBODY_WON, true));
                game.scheduler().runAfterSeconds(5, () -> game.requestStop(GameStopReason.finished()));
            } else {
                nextPlayer();
            }
        }
    }

    private void nextPlayer() {
        var nextTeam = playingTeams.next();
        var nextPlayer = nextTeam.players().next();

        game.allPlayers().sendMessage(ConnectFourTexts.TEAM_GOES_NEXT.apply(teams.getTeamByKey(nextTeam.key).config().styledName()), false);

        var player = game.allPlayers().getPlayerBy(nextPlayer);
        player.addItem(teamBlocks.get(nextTeam.key).powder.asItem().getDefaultInstance());

        player.setGlowingTag(true);
        player.displayClientMessage(ConnectFourTexts.IT_IS_YOUR_TURN.copy().withStyle(ChatFormatting.GOLD), true);
    }

    private boolean checkWin(int x, int y, GameTeamKey team) {
        if (checkLine(x, y, 0, -1, team)) { // vertical
            return true;
        }

        for (int offset = 0; offset < connectAmount; offset++) {
            if (checkLine(x - (connectAmount - 1) + offset, y, 1, 0, team)) { // horizontal
                return true;
            }

            if (checkLine(x - (connectAmount - 1) + offset, y + (connectAmount - 1) - offset, 1, -1, team)) { // leading diagonal
                return true;
            }

            if (checkLine(x - (connectAmount - 1) + offset, y - (connectAmount - 1) + offset, 1, 1, team)) { // trailing diagonal
                return true;
            }
        }

        return false;
    }


    private boolean checkLine(int xs, int ys, int dx, int dy, GameTeamKey team) {
        for (int i = 0; i < connectAmount; i++) {
            int x = xs + (dx * i);
            int y = ys + (dy * i);

            if (x < 0 || x > pieces.length - 1) {
                return false;
            }

            if (y < 0 || y > pieces[x].length - 1) {
                return false;
            }

            if (team != pieces[x][y]) {
                return false;
            }
        }

        return true;
    }

    private record GameBlock(Block powder, Block solid) {
        public static final MapCodec<GameBlock> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("powder").forGetter(GameBlock::powder),
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("solid").forGetter(GameBlock::solid)
        ).apply(in, GameBlock::new));
    }

    private record PendingGate(BlockPos gatePosition, BlockState gate) {
    }

    private record PlayingTeam(GameTeamKey key, SequentialList<PlayerKey> players) {

    }
}
