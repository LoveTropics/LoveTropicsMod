package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;
import java.util.Optional;

public record FogClientState(float red, float green, float blue, Optional<FogType> fogType, Optional<FogShape> fogShape) implements GameClientState {
    public static final Codec<FogClientState> CODEC = RecordCodecBuilder.create(in -> in.group(
            Codec.floatRange(0.0f, 1.0f).fieldOf("red").forGetter(FogClientState::red),
            Codec.floatRange(0.0f, 1.0f).fieldOf("green").forGetter(FogClientState::green),
            Codec.floatRange(0.0f, 1.0f).fieldOf("blue").forGetter(FogClientState::blue),
            StringRepresentable.fromEnum(FogType::values).optionalFieldOf("fog_type").forGetter(FogClientState::fogType),
            StringRepresentable.fromEnum(FogShape::values).optionalFieldOf("fog_shape").forGetter(FogClientState::fogShape)
    ).apply(in, FogClientState::new));

    @Override
    public GameClientStateType<?> getType() {
        return GameClientStateTypes.FOG.get();
    }

    public enum FogType implements StringRepresentable {
        TERRAIN,
        SKY;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum FogShape implements StringRepresentable {
        SPHERE,
        CYLINDER;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
