package com.lovetropics.minigames.common.util.registry;

import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public final class MobEffectBuilder<T extends MobEffect, P> extends AbstractBuilder<MobEffect, T, P, MobEffectBuilder<T, P>> {
	private final Supplier<T> effect;

	public MobEffectBuilder(final LoveTropicsRegistrate owner, final P parent, final String name, final BuilderCallback callback, final Supplier<T> effect) {
		super(owner, parent, name, callback, Registries.MOB_EFFECT);
		this.effect = effect;
	}

	public MobEffectBuilder<T, P> lang(final String name) {
		return super.lang(MobEffect::getDescriptionId, name);
	}

	@Override
	protected T createEntry() {
		return effect.get();
	}

	@Override
	protected RegistryEntry<T> createEntryWrapper(final RegistryObject<T> delegate) {
		return new RegistryEntry<>(getOwner(), delegate);
	}
}
