package com.lovetropics.minigames.common.core.dimension;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.concurrent.atomic.AtomicBoolean;

public final class RuntimeDimensionHandle {
	final RuntimeDimensions dimensions;
	final ServerLevel world;
	final AtomicBoolean deleted = new AtomicBoolean();

	RuntimeDimensionHandle(RuntimeDimensions dimensions, ServerLevel world) {
		this.dimensions = dimensions;
		this.world = world;
	}

	public ResourceKey<Level> asKey() {
		return this.world.dimension();
	}

	public ServerLevel asWorld() {
		Preconditions.checkState(!this.deleted.get(), "dimension is queued for deletion!");
		return this.world;
	}

	public void delete() {
		if (this.deleted.compareAndSet(false, true)) {
			this.dimensions.enqueueDeletion(this.world);
		}
	}
}
