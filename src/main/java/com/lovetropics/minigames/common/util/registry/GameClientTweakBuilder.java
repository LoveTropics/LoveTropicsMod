package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakType;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import net.minecraftforge.fml.RegistryObject;

public final class GameClientTweakBuilder<T extends GameClientTweak, P> extends AbstractBuilder<GameClientTweakType<?>, GameClientTweakType<T>, P, GameClientTweakBuilder<T, P>> {
	private final Codec<T> codec;

	public GameClientTweakBuilder(LoveTropicsRegistrate owner, P parent, String name, BuilderCallback callback, Codec<T> codec) {
		super(owner, parent, name, callback, GameClientTweakType.type());
		this.codec = codec;
	}

	@Override
	protected GameClientTweakType<T> createEntry() {
		return new GameClientTweakType<>(codec);
	}

	@Override
	protected GameClientTweakEntry<T> createEntryWrapper(RegistryObject<GameClientTweakType<T>> delegate) {
		return new GameClientTweakEntry<>(getOwner(), delegate);
	}

	@Override
	public GameClientTweakEntry<T> register() {
		return (GameClientTweakEntry<T>) super.register();
	}
}
