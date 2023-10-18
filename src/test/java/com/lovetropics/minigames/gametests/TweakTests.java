package com.lovetropics.minigames.gametests;

import com.lovetropics.lib.permission.role.RoleOverrideType;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.CancelPlayerDamageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.ScalePlayerDamageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.SetMaxHealthBehavior;
import com.lovetropics.minigames.common.core.game.datagen.BehaviorFactory;
import com.lovetropics.minigames.common.core.game.datagen.GameProvider;
import com.lovetropics.minigames.common.core.game.map.InlineMapProvider;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.gametests.api.LTGameTestHelper;
import com.lovetropics.minigames.gametests.api.MinigameTest;
import com.lovetropics.minigames.gametests.api.RegisterMinigameTest;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

import java.util.Map;

@RegisterMinigameTest
public class TweakTests implements MinigameTest {
    public static final RoleOverrideType<Boolean> IS_TEST_PLAYER = RoleOverrideType.register("test_player", Codec.BOOL);


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

        generator.builder(gameId("scale_damage"))
                .withPlayingPhase(new InlineMapProvider(Level.OVERWORLD), phaseBuilder -> phaseBuilder
                        .withBehavior(new ScalePlayerDamageBehavior(1f, Map.of(IS_TEST_PLAYER, new ScalePlayerDamageBehavior.RoleOverrideEntry<>(IS_TEST_PLAYER, true, 2f)))));
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
                .thenIdle(60) // Wait for invulnerability to end
                .thenExecute(() -> player.attack(target))
                .thenExecute(() -> helper.assertEntityHealth(target, target.getMaxHealth()))
                .thenSucceed();
    }

    @GameTest
    public void testScaleDamage(final LTGameTestHelper helper) {
        final var player = helper.createFakePlayer();
        final var lobby = helper.createGame(player, PlayerRole.PARTICIPANT);
        lobby.enqueue(gameId("scale_damage"));

        final var target = helper.playerBuilder()
                .gameMode(GameType.SURVIVAL)
                .isInvulnerableTo(source -> !source.is(DamageTypes.PLAYER_ATTACK))
                .canBeHarmedBy(p -> p == player)
                .shouldRegenerateNaturally(false)
                .build();

        target.getAbilities().invulnerable = false;
        lobby.getPlayers().join(target, PlayerRole.PARTICIPANT);

        helper.startSequence()
                .thenExecute(helper.startGame(lobby))
                .thenIdle(60) // Wait for spawn invulnerabulity to end
                .thenExecute(() -> target.hurt(target.damageSources().playerAttack(player), 2))
                .thenIdle(5)
                .thenExecute(() -> helper.assertEntityHealth(target, 18))
                .thenIdle(15) // Wait for invulnerable time to end
                .thenExecute(() -> helper.getRoles(target).addRole("setTest", Map.of(IS_TEST_PLAYER, true)))
                .thenExecute(() -> target.hurt(player.damageSources().playerAttack(player), 2))
                .thenExecute(() -> helper.assertEntityHealth(target, 14))
                .thenSucceed();
    }

    @Override
    public ResourceLocation id() {
        return new ResourceLocation("lttest:tweak_tests");
    }
}
