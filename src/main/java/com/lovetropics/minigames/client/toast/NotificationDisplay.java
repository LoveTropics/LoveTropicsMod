package com.lovetropics.minigames.client.toast;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;

public final class NotificationDisplay {
	public final NotificationIcon icon;
	public final Sentiment sentiment;
	public final Color color;
	public final long visibleTimeMs;

	public NotificationDisplay(NotificationIcon icon, Sentiment sentiment, Color color, long visibleTimeMs) {
		this.icon = icon;
		this.sentiment = sentiment;
		this.color = color;
		this.visibleTimeMs = visibleTimeMs;
	}

	public void encode(FriendlyByteBuf buffer) {
		this.icon.encode(buffer);
		buffer.writeByte(this.sentiment.ordinal() & 0xFF);
		buffer.writeByte(this.color.ordinal() & 0xFF);
		buffer.writeVarLong(this.visibleTimeMs);
	}

	public static NotificationDisplay decode(FriendlyByteBuf buffer) {
		NotificationIcon icon = NotificationIcon.decode(buffer);
		Sentiment sentiment = Sentiment.VALUES[buffer.readUnsignedByte() % Sentiment.VALUES.length];
		Color color = Color.VALUES[buffer.readUnsignedByte() % Color.VALUES.length];
		long timeMs = buffer.readVarLong();
		return new NotificationDisplay(icon, sentiment, color, timeMs);
	}

	public int getTextureOffset() {
		return this.sentiment.offset + this.color.offset;
	}

	public enum Color {
		DARK(0),
		LIGHT(32);

		public static final Color[] VALUES = values();

		public final int offset;

		Color(int offset) {
			this.offset = offset;
		}
	}

	public enum Sentiment {
		NEUTRAL("neutral", 0),
		POSITIVE("positive", 64),
		NEGATIVE("negative", 128);

		public static final Sentiment[] VALUES = values();
		public static final Codec<Sentiment> CODEC = MoreCodecs.stringVariants(VALUES, s -> s.name);

		public final String name;
		public final int offset;

		Sentiment(String name, int offset) {
			this.name = name;
			this.offset = offset;
		}
	}
}
