package com.lovetropics.minigames.common.core.game.behavior.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class ConfigData {
	
	private DisplayHint display = DisplayHint.NONE;
	
	ConfigData() {}
	
	public final DisplayHint display() {
		return display;
	}

	public abstract Object value();
	
	public final void setValue(Object value) {
		if (this.type().isValidValue(value)) {
			this.setValueInternal(value);
		} else {
			throw new IllegalArgumentException("Value is not valid for config type " + this.type().name() + ": " + value);
		}
	}
	
	protected abstract void setValueInternal(Object value);
	
	public abstract ConfigType type();
	
	public static class SimpleConfigData extends ConfigData {
		
		private final ConfigType type;
		private Object value;
		
		public SimpleConfigData(ConfigType type) {
			this.type = type;
		}

		public SimpleConfigData(ConfigType type, Object value) {
			this.type = type;
			this.value = value;
		}

		@Override
		public Object value() {
			return value;
		}
		
		@Override
		protected void setValueInternal(Object value) {
			this.value = value;
		}

		@Override
		public ConfigType type() {
			return type;
		}
		
		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("type", type())
					.append("value", value)
					.build();
		}
	}
	
	public static class ListConfigData extends ConfigData implements Iterable<Object> {
		
		public static final ListConfigData EMPTY = new ListConfigData(ConfigType.NONE);
		
		private final ConfigType type;
		private final List<Object> values = new ArrayList<>();

		public ListConfigData(ConfigType type) {
			this.type = type;
		}
		
		public void add(Object value) {
			if (componentType().isValidValue(value)) {
				this.values.add(value);
			} else {
				throw new IllegalArgumentException("Invalid list value for type " + componentType() + ": " + value);
			}
		}

		public void addAll(Collection<?> value) {
			value.forEach(this::add);
		}

		@Override
		public List<Object> value() {
			return values;
		}
		
		@Override
		protected void setValueInternal(Object value) {
			if (value instanceof Collection<?>) {
				Collection<?> coll = (Collection<?>) value;
				if (!coll.isEmpty()) {
					if (coll.stream().allMatch(this.componentType()::isValidValue)) {
						this.values.clear();
						this.values.addAll(coll);
					} else {
						throw new IllegalArgumentException("List contains invalid values for component type " + this.componentType() + ": " + value);
					}
				} else {
					this.values.clear();
				}
			} else if (value instanceof Object[]) {
				Object[] arr = (Object[]) value;
				if (arr.length > 0) {
					if (Arrays.stream(arr).allMatch(this.componentType()::isValidValue)) {
						this.values.clear();
						this.values.addAll(Arrays.asList(arr));
					} else {
						throw new IllegalArgumentException("Array contains invalid values for component type " + this.componentType() + ": " + Arrays.toString(arr));
					}
				} else {
					this.values.clear();
				}
			} else {
				throw new IllegalArgumentException("Value is not valid for list type " + this.componentType() + ": " + value);
			}
		}

		@Override
		public ConfigType type() {
			return ConfigType.LIST;
		}
		
		public ConfigType componentType() {
			return type;
		}
		
		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("componentType", componentType())
					.append("values", values)
					.build();
		}

		@Override
		public Iterator<Object> iterator() {
			return values.iterator();
		}
	}

	public static class CompositeConfigData extends ConfigData {
		private final Map<String, ConfigData> values = new HashMap<>();

		public final void addChild(String name, ConfigData config) {
			this.values.put(name, config);
		}

		@Override
		public Map<String, ConfigData> value() {
			return Collections.unmodifiableMap(values);
		}

		public ConfigData value(String name) {
			return values.get(name);
		}
		
		@Override
		protected void setValueInternal(Object value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ConfigType type() {
			return ConfigType.COMPOSITE;
		}
		
		@Override
		public String toString() {
			return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
					.append("values", values)
					.build();
		}
	}
}
