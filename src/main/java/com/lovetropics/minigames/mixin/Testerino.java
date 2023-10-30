package com.lovetropics.minigames.mixin;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensions;
import com.mojang.brigadier.context.CommandContextBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class Testerino {

    @SubscribeEvent
    public static void teleporterino(final CommandEvent evt) {
        if (!evt.getParseResults().getReader().getString().startsWith("tpa ")) {
            return;
        }

        final CommandContextBuilder<CommandSourceStack> ctx = evt.getParseResults().getContext();
        final ResourceKey<Level> playerDimension = ctx.getSource().getLevel().dimension();
        final RuntimeDimensions runtimeDimensions = RuntimeDimensions.get(ctx.getSource().getServer());

        if (runtimeDimensions.isTemporaryDimension(playerDimension)) {
            ctx.getSource().sendSuccess(() -> Component.translatable("command.tpa.illegal"), false);
            evt.setCanceled(true);
        }
    }
}
