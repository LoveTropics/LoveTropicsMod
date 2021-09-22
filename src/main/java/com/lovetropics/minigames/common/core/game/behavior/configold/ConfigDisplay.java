package com.lovetropics.minigames.common.core.game.behavior.configold;

import java.util.Collections;
import java.util.List;

public interface ConfigDisplay {
	
	enum FieldType {
		
		BOOLEAN,
		NUMBER,
		STRING,
		STATE,
		COMPOSITE,
		;

		private final Consumer<PacketBuffer> write;
	}

	class CompositeDisplay implements ConfigDisplay {
		private final List<ConfigDisplay> composition;
		
		public CompositeDisplay(List<ConfigDisplay> composition) {
			this.composition = composition;
		}
		
		@Override
		public List<ConfigDisplay> getChildren() {
			return composition;
		}
	}

	default List<ConfigDisplay> getChildren() {
		return Collections.emptyList();
	}

	FieldType getType();
}
