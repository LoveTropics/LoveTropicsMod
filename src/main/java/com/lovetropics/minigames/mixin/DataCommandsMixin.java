package com.lovetropics.minigames.mixin;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.util.world.gamedata.GameDataAccessor;
import net.minecraft.server.commands.data.BlockDataAccessor;
import net.minecraft.server.commands.data.DataCommands;
import net.minecraft.server.commands.data.EntityDataAccessor;
import net.minecraft.server.commands.data.StorageDataAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Function;

@Mixin(DataCommands.class)
public class DataCommandsMixin {
    @Shadow
    public static final List<Function<String, DataCommands.DataProvider>> ALL_PROVIDERS = ImmutableList.of(
            EntityDataAccessor.PROVIDER, BlockDataAccessor.PROVIDER, StorageDataAccessor.PROVIDER, GameDataAccessor.PROVIDER
    );
    @Shadow
    public static final List<DataCommands.DataProvider> TARGET_PROVIDERS = ALL_PROVIDERS.stream()
            .map(func -> func.apply("target"))
            .collect(ImmutableList.toImmutableList());
    @Shadow
    public static final List<DataCommands.DataProvider> SOURCE_PROVIDERS = ALL_PROVIDERS.stream()
            .map(func -> func.apply("source"))
            .collect(ImmutableList.toImmutableList());
}
