package com.lovetropics.minigames.common.core.game.state;

import com.lovetropics.minigames.common.util.LinearSpline;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public class ProgressionSpline {
	public static final Codec<ProgressionSpline> CODEC = Entry.CODEC.listOf().xmap(ProgressionSpline::new, s -> s.entries);

	private final List<Entry> entries;

	public ProgressionSpline(List<Entry> entries) {
		this.entries = entries;
	}

	public LinearSpline resolve(GameProgressionState progression) {
		LinearSpline.Builder spline = LinearSpline.builder();
		for (Entry entry : entries) {
			int point = entry.point().resolve(progression);
			float value = entry.value();
			spline.point(point, value);
		}
		return spline.build();
	}

	public record Entry(ProgressionPoint point, float value) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
				ProgressionPoint.CODEC.fieldOf("point").forGetter(Entry::point),
				Codec.FLOAT.fieldOf("value").forGetter(Entry::value)
		).apply(i, Entry::new));
	}
}
