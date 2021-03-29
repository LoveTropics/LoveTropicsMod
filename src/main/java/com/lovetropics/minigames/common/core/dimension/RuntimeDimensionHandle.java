package com.lovetropics.minigames.common.core.dimension;

import com.google.common.base.Preconditions;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.atomic.AtomicBoolean;

public final class RuntimeDimensionHandle {
	final RuntimeDimensions dimensions;
	final ServerWorld world;
	final AtomicBoolean deleted = new AtomicBoolean();

	RuntimeDimensionHandle(RuntimeDimensions dimensions, ServerWorld world) {
		this.dimensions = dimensions;
		this.world = world;
	}

	public RegistryKey<World> asKey() {
		return this.world.getDimensionKey();
	}

	public ServerWorld asWorld() {
		Preconditions.checkState(!this.deleted.get(), "dimension is queued for deletion!");
		return this.world;
	}

	public void delete() {
		if (this.deleted.compareAndSet(false, true)) {
			this.dimensions.enqueueDeletion(this.world);
		}
	}
}
