package com.lovetropics.minigames.common.util;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Objects;

public abstract class PartialUpdate<A> {
	protected final AbstractType<A> type;

	protected PartialUpdate(AbstractType<A> type) {
		this.type = type;
	}

	public abstract void applyTo(A apply);

	protected abstract void encode(FriendlyByteBuf buffer);

	public static final class Family<A> implements Iterable<AbstractType<A>> {
		private final AbstractType<A>[] idToType;
		private final Reference2IntMap<AbstractType<A>> typeToId = new Reference2IntOpenHashMap<>();

		private Family(AbstractType<A>[] idToType) {
			this.idToType = idToType;
			for (int i = 0; i < idToType.length; i++) {
				this.typeToId.put(idToType[i], i);
			}
		}

		@SafeVarargs
		public static <A> Family<A> of(AbstractType<A>... types) {
			return new Family<>(types);
		}

		@Override
		public Iterator<AbstractType<A>> iterator() {
			return Iterators.forArray(idToType);
		}

		@Nullable
		AbstractType<A> typeById(int id) {
			return id >= 0 && id < idToType.length ? idToType[id] : null;
		}

		int idByType(AbstractType<A> type) {
			return typeToId.getOrDefault(type, -1);
		}

		public int size() {
			return idToType.length;
		}
	}

	public interface AbstractType<A> {
		PartialUpdate<A> decode(FriendlyByteBuf buffer);
	}

	public static abstract class AbstractSet<A> implements Iterable<PartialUpdate<A>> {
		private final Family<A> family;
		private final PartialUpdate<A>[] updates;

		protected AbstractSet(Family<A> family) {
			this.family = family;
			this.updates = new PartialUpdate[family.size()];
		}

		protected void add(PartialUpdate<A> update) {
			updates[family.idByType(update.type)] = update;
		}

		public void applyTo(A apply) {
			for (PartialUpdate<A> update : this) {
				update.applyTo(apply);
			}
		}

		public void encode(FriendlyByteBuf buffer) {
			buffer.writeVarInt(encodeMask());

			for (PartialUpdate<A> update : updates) {
				if (update != null) {
					update.encode(buffer);
				}
			}
		}

		private int encodeMask() {
			int mask = 0;
			for (int id = 0; id < updates.length; id++) {
				if (updates[id] != null) {
					mask |= 1 << id;
				}
			}
			return mask;
		}

		protected void decodeSelf(FriendlyByteBuf buffer) {
			int mask = buffer.readVarInt();
			for (int id = 0; id < family.size(); id++) {
				if ((mask & 1 << id) == 0) continue;

				AbstractType<A> type = family.typeById(id);
				if (type != null) {
					add(type.decode(buffer));
				}
			}
		}

		@Override
		public Iterator<PartialUpdate<A>> iterator() {
			return Iterators.filter(Iterators.forArray(updates), Objects::nonNull);
		}
	}
}
