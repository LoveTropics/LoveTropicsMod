package com.lovetropics.minigames.common.util.registry;

import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import net.minecraftforge.registries.RegistryObject;

public final class GameBehaviorBuilder<T extends IGameBehavior, P> extends AbstractBuilder<GameBehaviorType<?>, GameBehaviorType<T>, P, GameBehaviorBuilder<T, P>> {
	private final Codec<T> codec;

	public GameBehaviorBuilder(LoveTropicsRegistrate owner, P parent, String name, BuilderCallback callback, Codec<T> codec) {
		super(owner, parent, name, callback, GameBehaviorType.type());
		this.codec = codec;
	}

	@Override
	protected GameBehaviorType<T> createEntry() {
		return new GameBehaviorType<>(codec);
	}

	@Override
	protected GameBehaviorEntry<T> createEntryWrapper(RegistryObject<GameBehaviorType<T>> delegate) {
		return new GameBehaviorEntry<>(getOwner(), delegate);
	}

	@Override
	public GameBehaviorEntry<T> register() {
		return (GameBehaviorEntry<T>) super.register();
	}
}
