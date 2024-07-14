package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.MapCodec;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class GameClientTweakBuilder<T extends GameClientState, P> extends AbstractBuilder<GameClientStateType<?>, GameClientStateType<T>, P, GameClientTweakBuilder<T, P>> {
	private final MapCodec<T> codec;

	public GameClientTweakBuilder(LoveTropicsRegistrate owner, P parent, String name, BuilderCallback callback, MapCodec<T> codec) {
		super(owner, parent, name, callback, GameClientStateTypes.REGISTRY_KEY);
		this.codec = codec;
	}

	@Override
	protected GameClientStateType<T> createEntry() {
		return new GameClientStateType<>(codec);
	}

	@Override
	protected GameClientTweakEntry<T> createEntryWrapper(DeferredHolder<GameClientStateType<?>, GameClientStateType<T>> delegate) {
		return new GameClientTweakEntry<>(getOwner(), delegate);
	}

	@Override
	public GameClientTweakEntry<T> register() {
		return (GameClientTweakEntry<T>) super.register();
	}
}
