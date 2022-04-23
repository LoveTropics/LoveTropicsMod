package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.GameResult;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

public final class LobbyControls {
	private static final LobbyControls EMPTY = new LobbyControls();

	private final Map<Type, Action> actions = new EnumMap<>(Type.class);

	public static LobbyControls empty() {
		return EMPTY;
	}

	public LobbyControls add(Type type, Action action) {
		this.actions.put(type, action);
		return this;
	}

	@Nullable
	public Action get(Type type) {
		return this.actions.get(type);
	}

	public boolean isEnabled(Type type) {
		return this.actions.containsKey(type);
	}

	public State asState() {
		State state = State.disabled();
		for (Type type : actions.keySet()) {
			state.enable(type);
		}
		return state;
	}

	public interface Action {
		GameResult<Unit> run();
	}

	public enum Type {
		PLAY,
		SKIP;

		public int mask() {
			return 1 << this.ordinal();
		}
	}

	public static final class State {
		private int bits;

		private State(int bits) {
			this.bits = bits;
		}

		public static State disabled() {
			return new State(0);
		}

		public void enable(Type type) {
			this.bits |= type.mask();
		}

		public void disable(Type type) {
			this.bits &= ~type.mask();
		}

		public boolean enabled(Type type) {
			return (bits & type.mask()) != 0;
		}

		public void encode(FriendlyByteBuf buffer) {
			buffer.writeByte(this.bits);
		}

		public static State decode(FriendlyByteBuf buffer) {
			return new State(buffer.readUnsignedByte());
		}
	}
}
