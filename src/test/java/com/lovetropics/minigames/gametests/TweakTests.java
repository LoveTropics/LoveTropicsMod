package com.lovetropics.minigames.gametests;

import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.CancelPlayerDamageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.SetMaxHealthBehavior;
import com.lovetropics.minigames.common.core.game.datagen.BehaviorFactory;
import com.lovetropics.minigames.common.core.game.datagen.GameProvider;
import com.lovetropics.minigames.common.core.game.map.InlineMapProvider;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.gametests.api.LTGameTestHelper;
import com.lovetropics.minigames.gametests.api.MinigameTest;
import com.lovetropics.minigames.gametests.api.RegisterMinigameTest;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

@RegisterMinigameTest
public class TweakTests implements MinigameTest {

    @Override
    public void generateGame(GameProvider.GameGenerator generator, BehaviorFactory behaviors, HolderLookup.Provider registries) {
        generator.builder(gameId("max_health"))
                .withPlayingPhase(new InlineMapProvider(Level.OVERWORLD), phaseBuilder -> phaseBuilder
                        .withBehavior(behaviors.applyToAllPlayersBehavior(
                                new SetMaxHealthBehavior(30d, Object2DoubleMaps.emptyMap())
                        )));

        generator.builder(gameId("cancel_damage"))
                .withPlayingPhase(new InlineMapProvider(Level.OVERWORLD), phaseBuilder -> phaseBuilder
                        .withBehavior(new CancelPlayerDamageBehavior(false)));
    }

    @GameTest
    public void testMaxHealth(final LTGameTestHelper helper) {
        final var player = helper.createFakePlayer();
        final var lobby = helper.createGame(player, PlayerRole.PARTICIPANT);
        lobby.enqueue(gameId("max_health"));

        helper.startSequence()
                .thenExecute(helper.startGame(lobby))
                .thenIdle(5)
                .thenExecute(() -> lobby.getCurrentPhase().setPlayerRole(player, PlayerRole.PARTICIPANT))
                .thenIdle(5)
                .thenExecute(() -> helper.assertEntityMaxHealth(player, 30f))
                .thenSucceed();
    }

    @GameTest
    public void testCancelDamage(final LTGameTestHelper helper) {
        final var player = helper.createFakePlayer();
        final var lobby = helper.createGame(player, PlayerRole.PARTICIPANT);
        lobby.enqueue(gameId("cancel_damage"));

        final var target = helper.makeMockSurvivalPlayer();
        helper.startSequence()
                .thenExecute(helper.startGame(lobby))
                .thenIdle(5)
                .thenExecute(() -> player.attack(target))
                .thenIdle(5)
                .thenExecute(() -> helper.assertEntityHealth(target, target.getMaxHealth()))
                .thenSucceed();
    }

    @Override
    public ResourceLocation id() {
        return new ResourceLocation("lttest:tweak_tests");
    }
}
