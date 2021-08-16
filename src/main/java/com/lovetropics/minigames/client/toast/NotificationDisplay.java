package com.lovetropics.minigames.client.toast;

import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.network.PacketBuffer;

public final class NotificationDisplay {
	public final NotificationIcon icon;
	public final Type type;
	public final long visibleTimeMs;

	public NotificationDisplay(NotificationIcon icon, Type type, long visibleTimeMs) {
		this.icon = icon;
		this.type = type;
		this.visibleTimeMs = visibleTimeMs;
	}

	public void encode(PacketBuffer buffer) {
		this.icon.encode(buffer);
		buffer.writeByte(this.type.ordinal() & 0xFF);
		buffer.writeVarLong(this.visibleTimeMs);
	}

	public static NotificationDisplay decode(PacketBuffer buffer) {
		NotificationIcon icon = NotificationIcon.decode(buffer);
		Type type = Type.VALUES[buffer.readUnsignedByte() % Type.VALUES.length];
		long timeMs = buffer.readVarLong();
		return new NotificationDisplay(icon, type, timeMs);
	}

	public enum Type {
		DARK("dark", 0),
		LIGHT("light", 32);

		public static final Type[] VALUES = values();
		public static final Codec<Type> CODEC = MoreCodecs.stringVariants(VALUES, t -> t.name);

		public final String name;
		public final int vOffset;

		Type(String name, int vOffset) {
			this.name = name;
			this.vOffset = vOffset;
		}
	}
}
