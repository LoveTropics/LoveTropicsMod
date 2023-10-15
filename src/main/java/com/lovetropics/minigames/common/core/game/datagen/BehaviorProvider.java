package com.lovetropics.minigames.common.core.game.datagen;

import com.google.gson.JsonElement;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import org.slf4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BehaviorProvider implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final PackOutput output;
    protected final BehaviorFactory behaviors;
    protected final CompletableFuture<HolderLookup.Provider> registries;

    public BehaviorProvider(PackOutput output, BehaviorFactory behaviors, CompletableFuture<HolderLookup.Provider> registries) {
        this.output = output;
        this.behaviors = behaviors;
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput) {
        return registries.thenCompose(regs -> {
            final var behProv = this.output.createPathProvider(PackOutput.Target.DATA_PACK, "behaviors");
            final var regOps = RegistryOps.create(JsonOps.INSTANCE, regs);
            return CompletableFuture.allOf(behaviors.stream()
                    .map(entry -> {
                        final var built = entry.getValue();
                        final var path = behProv.json(entry.getKey());

                        Optional<JsonElement> optional = IGameBehavior.CODEC.encodeStart(regOps, built).resultOrPartial((p_255999_) -> {
                            LOGGER.error("Couldn't serialize behavior {}: {}", path, p_255999_);
                        });
                        return optional.isPresent() ? DataProvider.saveStable(pOutput, optional.get(), path) : CompletableFuture.completedFuture(null);
                    })
                    .toArray(CompletableFuture[]::new));
        });
    }

    @Override
    public String getName() {
        return "Behaviors";
    }
}
