package com.lovetropics.minigames.common.core.game.util;

import com.google.common.collect.ImmutableList;
import com.lovetropics.lib.BlockBox;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;

public record ColliderSet(
		List<BlockBox> boxes,
		List<VoxelShape> shapes,
		@Nullable
		AABB extent
) {
	public static final ColliderSet EMPTY = new ColliderSet(List.of());

	public static final MapCodec<ColliderSet> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			BlockBox.CODEC.listOf().fieldOf("boxes").forGetter(ColliderSet::boxes)
	).apply(i, ColliderSet::new));
	public static final StreamCodec<ByteBuf, ColliderSet> STREAM_CODEC = StreamCodec.composite(
			BlockBox.STREAM_CODEC.apply(ByteBufCodecs.list()), ColliderSet::boxes,
			ColliderSet::new
	);

	public ColliderSet(List<BlockBox> boxes) {
		this(boxes, toShapes(boxes), resolveExtent(boxes));
	}

	private static List<VoxelShape> toShapes(List<BlockBox> boxes) {
		return boxes.stream().map(BlockBox::asShape).toList();
	}

	@Nullable
	private static AABB resolveExtent(List<BlockBox> boxes) {
		return boxes.stream().reduce(BlockBox::encompassing).map(BlockBox::asAabb).orElse(null);
	}

	public void addTo(AABB boundingBox, ImmutableList.Builder<VoxelShape> output) {
		if (extent == null || !boundingBox.intersects(extent)) {
			return;
		}
		for (int i = 0; i < boxes.size(); i++) {
			if (boxes.get(i).intersects(boundingBox)) {
				output.add(shapes.get(i));
			}
		}
	}
}
