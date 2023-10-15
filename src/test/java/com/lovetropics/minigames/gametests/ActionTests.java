package com.lovetropics.minigames.gametests;

import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.behavior.action.NoneActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.PlaySoundAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.RunCommandsAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SendMessageAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.StartGameTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.StopGameTrigger;
import com.lovetropics.minigames.common.core.game.datagen.BehaviorFactory;
import com.lovetropics.minigames.common.core.game.datagen.GameProvider;
import com.lovetropics.minigames.common.core.game.map.InlineMapProvider;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.lovetropics.minigames.gametests.api.LTGameTestHelper;
import com.lovetropics.minigames.gametests.api.MinigameTest;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class ActionTests implements MinigameTest {
    @Override
    public void generateGame(GameProvider.GameGenerator generator, BehaviorFactory behaviors, HolderLookup.Provider registries) {
        generator.builder(id().withSuffix("/start_trigger"))
                .withPlayingPhase(new InlineMapProvider(Level.OVERWORLD), phaseBuilder -> phaseBuilder
                        .withBehavior(new StartGameTrigger(behaviors.applyToAllPlayers(
                                NoneActionTarget.INSTANCE,
                                new SendMessageAction(new TemplatedText(Component.literal("hello world!"))),
                                new PlaySoundAction(SoundEvents.ALLAY_HURT, 0.5f, 0.5f)
                        ))));

        generator.builder(id().withSuffix("/stop_trigger"))
                .withPlayingPhase(new InlineMapProvider(Level.OVERWORLD), phaseBuilder -> phaseBuilder
                        .withBehavior(new StopGameTrigger(behaviors.applyToAllPlayers(
                                NoneActionTarget.INSTANCE,
                                new RunCommandsAction(List.of(), List.of("give @s minecraft:oak_planks 13"))
                        ), Optional.empty(), Optional.empty())));
    }

    @GameTest
    public void testStartTrigger(final LTGameTestHelper helper) {
        final var player = helper.createFakePlayer(packet -> packet instanceof ClientboundSystemChatPacket || packet instanceof ClientboundSoundPacket);
        final var lobby = helper.createGame(player, PlayerRole.PARTICIPANT);
        lobby.enqueue(id().withSuffix("/start_trigger"));

        helper.startSequence()
            .thenExecute(helper.startGame(lobby))
            .thenIdle(20)
            .thenExecute(() -> helper.assertReceivedPacket(player, 0, ClientboundSystemChatPacket.class, it -> it.content().equals(Component.literal("hello world!"))))
            .thenExecute(() -> helper.assertReceivedPacket(player, 1, ClientboundSoundPacket.class, it -> it.getSound().get() == SoundEvents.ALLAY_HURT && it.getVolume() == 0.5f && it.getPitch() == 0.5f))
            .thenSucceed();
    }

    @GameTest
    public void testStopTrigger(final LTGameTestHelper helper) {
        final var player = helper.createFakePlayer(packet -> false);
        final var lobby = helper.createGame(player, PlayerRole.PARTICIPANT);
        lobby.enqueue(id().withSuffix("/stop_trigger"));

        helper.startSequence()
            .thenExecute(helper.startGame(lobby))
            .thenIdle(20)
            .thenExecute(() -> lobby.getCurrentPhase().requestStop(GameStopReason.finished()))
            .thenExecute(() -> helper.assertPlayerInventoryContainsAt(player, 0, new ItemStack(Items.OAK_PLANKS, 13)))
            .thenSucceed();
    }

    @Override
    public ResourceLocation id() {
        return new ResourceLocation("lttest:action_test");
    }
}
