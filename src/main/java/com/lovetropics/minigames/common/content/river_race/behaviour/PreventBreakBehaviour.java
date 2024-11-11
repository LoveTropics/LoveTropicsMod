package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;

public record PreventBreakBehaviour(Block[] blocks) implements IGameBehavior {
    public static final MapCodec<PreventBreakBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            MoreCodecs.arrayOrUnit(BuiltInRegistries.BLOCK.byNameCodec(), Block[]::new).fieldOf("blocks").forGetter(c -> c.blocks)
    ).apply(i, PreventBreakBehaviour::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GamePlayerEvents.BREAK_BLOCK, (player, pos, state, hand) -> {
            for (Block block : blocks) {
                if(state.is(block)){
                    return InteractionResult.FAIL;
                }
            }
            return InteractionResult.PASS;
        });
    }
}
