package com.lovetropics.minigames.gametests.api;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.function.Predicate;

public class FakePlayerBuilder {
    private final LTGameTestHelper helper;

    public FakePlayerBuilder(LTGameTestHelper helper) {
        this.helper = helper;
    }

    Predicate<Packet<?>> packetPredicate = p -> false;
    Predicate<DamageSource> isInvulnerableTo = s -> true;
    GameType gameMode = GameType.CREATIVE;
    Predicate<Player> canBeHarmedBy = player -> false;
    boolean shouldRegenerateNaturally = true;

    public FakePlayerBuilder packetFilter(Predicate<Packet<?>> packetPredicate) {
        this.packetPredicate = packetPredicate;
        return this;
    }

    public FakePlayerBuilder isInvulnerableTo(Predicate<DamageSource> isInvulnerableTo) {
        this.isInvulnerableTo = isInvulnerableTo;
        return this;
    }

    public FakePlayerBuilder gameMode(GameType gameMode) {
        this.gameMode = gameMode;
        return this;
    }

    public FakePlayerBuilder canBeHarmedBy(Predicate<Player> canBeHarmedBy) {
        this.canBeHarmedBy = canBeHarmedBy;
        return this;
    }

    public FakePlayerBuilder shouldRegenerateNaturally(boolean shouldRegenerateNaturally) {
        this.shouldRegenerateNaturally = shouldRegenerateNaturally;
        return this;
    }

    public LTFakePlayer build() {
        final var player = new LTFakePlayer(helper.getLevel(), this);
        helper.info.addListener(new GameTestListener() {
            @Override
            public void testStructureLoaded(GameTestInfo pTestInfo) {

            }

            @Override
            public void testPassed(GameTestInfo pTestInfo) {
                player.exitWorld();
            }

            @Override
            public void testFailed(GameTestInfo pTestInfo) {
                player.exitWorld();
            }
        });
        return player;
    }
}
