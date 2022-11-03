package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import net.minecraftforge.registries.RegistryObject;

public final class GameClientTweakBuilder<T extends GameClientState, P> extends AbstractBuilder<GameClientStateType<?>, GameClientStateType<T>, P, GameClientTweakBuilder<T, P>> {
	private final Codec<T> codec;

	public GameClientTweakBuilder(LoveTropicsRegistrate owner, P parent, String name, BuilderCallback callback, Codec<T> codec) {
		super(owner, parent, name, callback, GameClientStateTypes.REGISTRY_KEY);
		this.codec = codec;
	}

	@Override
	protected GameClientStateType<T> createEntry() {
		return new GameClientStateType<>(codec);
	}

	@Override
	protected GameClientTweakEntry<T> createEntryWrapper(RegistryObject<GameClientStateType<T>> delegate) {
		return new GameClientTweakEntry<>(getOwner(), delegate);
	}

	@Override
	public GameClientTweakEntry<T> register() {
		return (GameClientTweakEntry<T>) super.register();
	}
}
