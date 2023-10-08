package com.lovetropics.minigames.common.content.spleef;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerIterable;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.game.util.GameScheduler;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.util.world.BlockPlacer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class SpleefBehavior implements IGameBehavior {

    private IGamePhase game;

    public static final MapCodec<SpleefBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.INT.optionalFieldOf("forced_progression_seconds", 17).forGetter(c -> c.forcedProgressionSeconds),
            Codec.INT.optionalFieldOf("floors", 0).forGetter(c -> c.floors),
            Codec.INT.optionalFieldOf("break_interval", 40).forGetter(c -> c.breakInterval),
            Codec.INT.optionalFieldOf("break_count", 6).forGetter(c -> c.breakCount),
            ForgeRegistries.BLOCKS.getCodec().optionalFieldOf("floor_material", Blocks.OBSIDIAN).forGetter(c -> c.floorMaterial),
            ForgeRegistries.BLOCKS.getCodec().optionalFieldOf("floor_breaking_material", Blocks.PURPLE_CONCRETE).forGetter(c -> c.floorBreakingMaterial),
            Codec.STRING.optionalFieldOf("flavor_text", "volcano").forGetter(c -> c.flavourText)
    ).apply(i, SpleefBehavior::new));

    private final int forcedProgressionSeconds;
    private final int breakInterval;
    private final int breakCount;
    private final int floors;
    private final String flavourText;
    private GlobalGameWidgets widgets;

    private GameBossBar bossBar;

    private BlockBox[] floorRegions;

    // Eliminate players if they enter this area.
    private BlockBox deathRegion;

    private final Block floorMaterial;
    private final Block floorBreakingMaterial;


    /**
     * To make it easier for mapmakers, the levels will start at 1 but in code it'll be 0.
     */
    private int currentFloor = 0;

    private int progressionTimer = 0;

    private GameScheduler scheduler = new GameScheduler();

    private final Style DARK_YELLOW = Style.EMPTY.withColor(TextColor.parseColor("#77A12F"));

    // If people jump into the lava now, don't show the eliminated screen
    private boolean gameOver = false;

    // Handy for checking who won if multiple people were eliminated at the same time.
    private List<ServerPlayer> lastTickPlayers;

    private boolean blockSinglePlayerWin = true;

    public SpleefBehavior(int forcedProgressionSeconds, int floors, int breakInterval, int breakCount, Block floorMaterial, Block floorBreakingMaterial, String flavourText) {
        this.forcedProgressionSeconds = forcedProgressionSeconds;
        this.floors = floors;
        this.floorRegions = new BlockBox[floors];
        this.breakInterval = breakInterval;
        this.breakCount = breakCount;
        this.floorMaterial = floorMaterial;
        this.floorBreakingMaterial = floorBreakingMaterial;
        this.flavourText = flavourText;
        this.gameOver = false;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        this.widgets = GlobalGameWidgets.registerTo(game, events);

        for (int floor = 0; floor < this.floors; floor++) {
            this.floorRegions[floor] = game.getMapRegions().getOrThrow("floor" + (floor + 1));
        }

        this.deathRegion = game.getMapRegions().getOrThrow("death");

        Style style = Style.EMPTY.withColor(TextColor.parseColor("#ACC12F")).withBold(true);
        this.bossBar = this.widgets.openBossBar(MinigameTexts.SPLEEF_TITLE_PREPARE.copy().withStyle(style), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);

        for (var floor : this.floorRegions) {
            BlockPlacer.replace(game.getWorld(), floor, this.floorMaterial, BlockPlacer.Mode.REPLACE, Blocks.WHITE_STAINED_GLASS);
        }

        events.listen(GamePhaseEvents.START, () -> {

            // Shouldn't affect a typical game but has a bug in dev with 1 player.
            this.lastTickPlayers = this.game.getParticipants().stream().toList();

            if(this.game.getParticipants().size() == 1) {
                this.blockSinglePlayerWin = true;
            }

            int tickDelay = 5 * 20;
            countdownMessage(10, "#176D85", 0.25f, tickDelay);

            tickDelay += 5 * 20;
            countdownMessage(5, "#176D85", 0.5f, tickDelay);

            tickDelay += 20;
            countdownMessage(4, "#307B6F", 0.75f, tickDelay);

            tickDelay += 20;
            countdownMessage(3, "#48885B", 1f, tickDelay);

            tickDelay += 20;
            countdownMessage(2, "#5F9446", 1.25f, tickDelay);

            tickDelay += 20;
            countdownMessage(1, "#77A12F", 1.5f, tickDelay);

            tickDelay += 20;
            this.scheduler.delayedTickEvent("start_game", this::startGame, tickDelay);
        });

        events.listen(GamePhaseEvents.TICK, scheduler::tick);
        events.listen(GamePhaseEvents.TICK, this::checkPlayerDeath);
        events.listen(GamePhaseEvents.TICK, this::checkForWinner);

        events.listen(GamePlayerEvents.DEATH, this::onPlayerDeath);
    }


    private InteractionResult onPlayerDeath(ServerPlayer player, DamageSource damageSource) {
        this.killPlayer(player);

        return InteractionResult.FAIL;
    }

    /**
     * This is after the countdown has finished.
     */
    private void startGame() {
        this.progressionTimer = this.forcedProgressionSeconds;
        this.scheduler.intervalTickEvent("forced_progression", this::updateForcedProgression, 0, 20);

        this.game.getAllPlayers().playSound(SoundEvents.ANVIL_PLACE, SoundSource.MASTER, Integer.MAX_VALUE, 1);
        this.game.getAllPlayers().playSound(SoundEvents.BASALT_BREAK, SoundSource.MASTER, Integer.MAX_VALUE, 1);
    }

    private void checkForWinner() {
        if(this.gameOver) {
            return;
        }
        var participants = this.game.getParticipants();

        if(participants.size() <= 1) {

            if (this.game.getParticipants().size() == 1 && !blockSinglePlayerWin) {
                this.winTitle(participants.stream().toList());
                var displayName = participants.iterator().next().getDisplayName();
                var style = displayName.getStyle();
                if(style.getColor() == null) {
                    style = style.withColor(ChatFormatting.WHITE);
                }
                this.game.getAllPlayers().playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, Integer.MAX_VALUE, 0.8f);
                this.announceWinner(Component.translatable(getFlavourTextKey("win"), displayName.copy().withStyle(style)));
            } else if(this.game.getParticipants().isEmpty()) {
                // Join the winners with a comma from the last tick though add an & for the last join.
                var winners = this.lastTickPlayers.stream().toList();
                MutableComponent winnerText = Component.literal("");

                for (int i = 0; i < winners.size(); i++) {
                    this.winTitle(winners);
                    var displayName = winners.get(i).getDisplayName();
                    var style = displayName.getStyle();
                    if(style.getColor() == null) {
                        style = style.withColor(ChatFormatting.WHITE);
                    }
                    winnerText.append(winners.get(i).getDisplayName().copy().withStyle(style));
                    if (i < winners.size() - 2) {
                        winnerText.append(", ");
                    } else if (i == winners.size() - 2) {
                        winnerText.append(" & ");
                    }
                }

                this.game.getAllPlayers().playSound(SoundEvents.RAID_HORN.get(), SoundSource.MASTER, Integer.MAX_VALUE, 0.75f);
                this.announceWinner(Component.translatable(getFlavourTextKey("winners"), winnerText));
            }
        } else {
            this.lastTickPlayers = participants.stream().toList();
        }
    }

    private void winTitle(List<ServerPlayer> players) {
        for(ServerPlayer player : players) {
            title(player, MinigameTexts.WINNER_TITLE.copy().withStyle(ChatFormatting.GREEN),
                    MinigameTexts.WINNER_SUBTITLE.copy(),
                    20, 20 * 3, 20);
        }
    }

    private void checkPlayerDeath() {
        for (ServerPlayer player : this.game.getParticipants()) {
            if (this.deathRegion.intersects(player.getBoundingBox())) {
                this.killPlayer(player);
            }
        }
    }

    /**
     * Trigger the end game timer as well as show the winner.
     */
    private void announceWinner(MutableComponent winner) {
        this.gameOver = true;

        this.scheduler.clearAllEvents();

        specialMessage(Component.literal("★").withStyle(ChatFormatting.GOLD),
                winner.withStyle(Style.EMPTY.withColor(TextColor.parseColor("#ACC12F"))));

        this.bossBar.close();

        this.scheduler.delayedTickEvent("end_game", () -> {
            this.game.requestStop(GameStopReason.finished());
        }, 20 * 8);
    }

    private void killPlayer(ServerPlayer player) {
        if(!gameOver) {
            this.specialMessage(Component.literal("☠").withStyle(ChatFormatting.RED), player.getDisplayName().copy().withStyle(ChatFormatting.DARK_GRAY));
            title(player, MinigameTexts.SPLEEF_ELIMINATED.copy().withStyle(ChatFormatting.RED),
                    Component.translatable(getFlavourTextKey("eliminated")).withStyle(ChatFormatting.YELLOW),
                    20, 20 * 3, 20);
            player.playNotifySound(SoundEvents.ANVIL_PLACE, SoundSource.MASTER, Integer.MAX_VALUE, 1);
        }
        this.game.setPlayerRole(player, PlayerRole.SPECTATOR);
    }

    private void specialMessage(Component sender, Component message) {
        this.game.getAllPlayers().sendMessage(Component.literal("[").withStyle(ChatFormatting.GRAY)
                .append(sender).append("]: ")
                .append(message), false);
    }

    private String getFlavourTextKey(String key) {
        return "ltminigames.minigame.spleef.flavor." + this.flavourText + "." + key;
    }

    private void startFloor() {
        this.progressionTimer = this.forcedProgressionSeconds;
    }

    private void updateForcedProgression() {
        if(this.currentFloor >= this.floors) {
            return;
        }
        if(this.progressionTimer <= 0) {
            this.progressionTimer = this.forcedProgressionSeconds;
            this.spleefMessage(Component.translatable(getFlavourTextKey("forced_progression"), Component.translatable("ltminigames.minigame.position." + (this.currentFloor + 1))).withStyle(ChatFormatting.YELLOW));
            BlockPlacer.replace(game.getWorld(), this.floorRegions[this.currentFloor], this.floorBreakingMaterial, BlockPlacer.Mode.REPLACE, this.floorMaterial, this.scheduler,
                    (pos) -> (game.getWorld().random.nextInt(this.breakCount) * this.breakInterval),
                    (pos) -> {
                        scheduler.delayedTickEvent("delayed_break", () -> {
                            game.getWorld().destroyBlock(pos, false);
                            scheduler.notifyBlockChange(pos, game.getWorld(), Blocks.AIR);
                        }, 15 + game.getWorld().random.nextInt(10));
                    });
            currentFloor++;
        }

        switch (this.progressionTimer) {
            case 10 -> {
                this.spleefMessage(Component.translatable(getFlavourTextKey("layer_countdown"), Component.literal(Integer.toString(this.progressionTimer)).withStyle(DARK_YELLOW)).withStyle(ChatFormatting.YELLOW));
            }
            case 5, 4, 3, 2, 1 -> {
                this.spleefMessage(Component.literal(Integer.toString(this.progressionTimer)).withStyle(DARK_YELLOW), false);
                this.game.getAllPlayers().playSound(SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.MASTER, Integer.MAX_VALUE, 1.75f - (this.progressionTimer * 0.25f));
            }
        }

        this.updateForcedProgressionBossbar();
        this.progressionTimer--;
    }

    private void updateForcedProgressionBossbar() {
        Style style = Style.EMPTY.withColor(TextColor.parseColor("#ACC12F")).withBold(true);
        this.bossBar.setTitle(MinigameTexts.SPLEEF_TITLE_FORCED_PROGRESSION.copy().withStyle(style)
                .append(Component.literal(Integer.toString(this.progressionTimer)).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withBold(false))));
    }

    private void countdownMessage(int seconds, String hexColor, float soundPitch, int delayTicks) {
        this.scheduler.delayedTickEvent("countdown", () -> {
            title(MinigameTexts.SPLEEF_COUNTDOWN_TITLE.apply(
                                    Component.literal(Integer.toString(seconds)).withStyle(Style.EMPTY.withColor(TextColor.parseColor(hexColor))))
                            .withStyle(ChatFormatting.GRAY),
                    MinigameTexts.SPLEEF_COUNTDOWN_SUBTITLE.copy().withStyle(ChatFormatting.YELLOW),
                    5, 20, seconds == 1 ? 0 : 5);
            this.game.getAllPlayers().playSound(SoundEvents.NOTE_BLOCK_BIT.get(), SoundSource.MASTER, Integer.MAX_VALUE, soundPitch);
        }, delayTicks);
    }

    private void spleefMessage(Component message) {
        this.spleefMessage(message, true);
    }

    private void spleefMessage(Component message, boolean showGameName) {
        var fullMessage = Component.literal("").append(Component.literal(">>> ").withStyle(ChatFormatting.GRAY));
        if(showGameName) {
            fullMessage = fullMessage.append(MinigameTexts.SPLEEF).append(Component.literal(" ❯ "));
        }
        fullMessage = fullMessage.append(message);
        this.game.getAllPlayers().sendMessage(fullMessage.withStyle(ChatFormatting.YELLOW), false);
    }


    private void title(Component title, Component subtitle, int pFadeIn, int pStay, int pFadeOut) {
        PlayerSet players = this.game.getAllPlayers();
        title(players, title, subtitle, pFadeIn, pStay, pFadeOut);
    }

    private void title(PlayerIterable players, Component title, Component subtitle, int pFadeIn, int pStay, int pFadeOut) {
        players.sendPacket(new ClientboundSetTitlesAnimationPacket(pFadeIn, pStay, pFadeOut));
        players.sendPacket(new ClientboundSetTitleTextPacket(title));
        players.sendPacket(new ClientboundSetSubtitleTextPacket(subtitle));
    }

    private void title(ServerPlayer player, Component title, Component subtitle, int pFadeIn, int pStay, int pFadeOut) {
        PlayerSet players = this.game.getAllPlayers();
        player.connection.send(new ClientboundSetTitlesAnimationPacket(pFadeIn, pStay, pFadeOut));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
    }
    
}
