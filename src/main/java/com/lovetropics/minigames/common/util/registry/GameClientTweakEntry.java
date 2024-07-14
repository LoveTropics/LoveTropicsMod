package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.MapCodec;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class GameClientTweakEntry<T extends GameClientState> extends RegistryEntry<GameClientStateType<?>, GameClientStateType<T>> {
	public GameClientTweakEntry(AbstractRegistrate<?> owner, DeferredHolder<GameClientStateType<?>, GameClientStateType<T>> delegate) {
		super(owner, delegate);
	}

	public MapCodec<T> getCodec() {
		return get().codec();
	}
}
