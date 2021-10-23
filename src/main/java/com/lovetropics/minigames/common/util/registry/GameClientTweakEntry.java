package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakType;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraftforge.fml.RegistryObject;

public final class GameClientTweakEntry<T extends GameClientTweak> extends RegistryEntry<GameClientTweakType<T>> {
	public GameClientTweakEntry(AbstractRegistrate<?> owner, RegistryObject<GameClientTweakType<T>> delegate) {
		super(owner, delegate);
	}

	public Codec<T> getCodec() {
		return get().getCodec();
	}
}
