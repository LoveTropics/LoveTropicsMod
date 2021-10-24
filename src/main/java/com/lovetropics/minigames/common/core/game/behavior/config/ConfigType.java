package com.lovetropics.minigames.common.core.game.behavior.config;

import java.util.Collection;

public enum ConfigType {
	
	NONE(Void.class, true),
	STRING(String.class, false),
	NUMBER(Number.class, false),
	BOOLEAN(Boolean.class, false),
	ENUM(Enum.class, false),
	LIST(Collection.class, true),
	COMPOSITE(ConfigData.class, true),
	;
	
	private final Class<?> requiredType;
	private final boolean isComplex;

	private ConfigType(Class<?> requiredType, boolean isComplex) {
		this.requiredType = requiredType;
		this.isComplex = isComplex;
	}
	
	public boolean isValidValue(Object val) {
		return requiredType.isInstance(val);
	}

	public boolean isComplex() {
		return isComplex;
	}
}
