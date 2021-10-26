package com.lovetropics.minigames.common.core.game.behavior.config;

import java.util.Collection;

public enum ConfigType {
	
	NONE(Void.class),
	STRING(String.class),
	NUMBER(Number.class),
	BOOLEAN(Boolean.class),
	ENUM(Enum.class),
	LIST(Collection.class),
	COMPOSITE(ConfigData.class),
	;
	
	private final Class<?> requiredType;
	
	private ConfigType() {
		this(Object.class);
	}

	private ConfigType(Class<?> requiredType) {
		this.requiredType = requiredType;
	}
	
	public boolean isValidValue(Object val) {
		return requiredType.isInstance(val);
	}
}
