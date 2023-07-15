package com.lovetropics.minigames.common.core.integration;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;

public enum Crud implements StringRepresentable {
	CREATE("create"),
	READ("read"),
	UPDATE("update"),
	DELETE("delete"),
	;

	public static final Codec<Crud> CODEC = StringRepresentable.fromEnum(Crud::values);

	private final String name;

	Crud(String name) {
		this.name = name;
	}

	@Nullable
	public static Crud parse(JsonElement json) {
		return CODEC.parse(JsonOps.INSTANCE, json).result().orElse(null);
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
