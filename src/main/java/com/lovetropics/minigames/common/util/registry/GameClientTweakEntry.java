package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraftforge.fml.RegistryObject;

public final class GameClientTweakEntry<T extends GameClientState> extends RegistryEntry<GameClientStateType<T>> {
	public GameClientTweakEntry(AbstractRegistrate<?> owner, RegistryObject<GameClientStateType<T>> delegate) {
		super(owner, delegate);
	}

	public Codec<T> getCodec() {
		return get().getCodec();
	}
}
