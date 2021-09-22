package com.lovetropics.minigames.common.core.game.behavior.configold;

import java.util.function.Consumer;
import java.util.function.Function;

public final class Configurable<T> {
	
	private final T defVal;
	private T cfgVal;
	private boolean baked;
	
	private final ConfigDisplay display;
	private final Function<ConfigDisplay, T> fromSettings;
	
	private final ConfigList children;

	private Configurable(T defVal, ConfigDisplay display, Function<ConfigDisplay, T> fromSettings) {
		this(defVal, display, fromSettings, ConfigList.empty());
	}
	
	private Configurable(T defVal, ConfigDisplay display, Function<ConfigDisplay, T> fromSettings, ConfigList children) {
		this.defVal = defVal;
		this.display = display;
		this.fromSettings = fromSettings;
		this.children = children;
	}

	public T getDefault() {
		return defVal;
	}
	
	public boolean isBaked() {
		return baked;
	}

	public T getUnchecked() {
		return cfgVal;
	}

	public T get() {
		if (!isBaked()) {
			throw new IllegalStateException("Cannot request a value from an unbaked configurable");
		}
		return getUnchecked();
	}
	
	public ConfigList getChildren() {
		return children;
	}
	
	public Configurable<T> withChildren(Consumer<ConfigList.Builder> children) {
		ConfigList.Builder builder = ConfigList.builder();
		children.accept(builder);
		return new Configurable<T>(this.defVal, this.display, this.fromSettings, builder.build());
	}

	public ConfigDisplay display() {
		return display;
	}
	
	public T fromSettings(ConfigDisplay settings) {
		return fromSettings.apply(settings);
	}

	void bake(T value) {
		this.cfgVal = value;
		this.baked = true;
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}

	public static class Builder<T> {
		
		private T defVal = null;
		private ConfigDisplay display = null;//ConfigDisplay.STRING;
		private Function<ConfigDisplay, T> fromSettings = $ -> { throw new UnsupportedOperationException(); };
		private ConfigList.Builder children = ConfigList.builder();
		
		public Builder<T> defaultValue(T defVal) {
			this.defVal = defVal;
			return this;
		}
		
		public Builder<T> display(ConfigDisplay display) {
			this.display = display;
			return this;
		}
		
		public Builder<T> deserializer(Function<ConfigDisplay, T> fromSettings) {
			this.fromSettings = fromSettings;
			return this;
		}
		
		public <C> Builder<T> child(Config<C> key, Consumer<Builder<C>> childCreator) {
			Builder<C> childBuilder = new Builder<>();
			childCreator.accept(childBuilder);
			this.children.with(key, childBuilder.build());
			return this;
		}
		
		public Configurable<T> build() {
			return new Configurable<>(defVal, display, fromSettings, children.build());
		}
	}
}
