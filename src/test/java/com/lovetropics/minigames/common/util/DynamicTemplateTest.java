package com.lovetropics.minigames.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DynamicTemplateTest {
	private static final JsonOps OPS = JsonOps.INSTANCE;

	private static <T> void assertSubstitutionAndExtraction(final DynamicTemplate template, final Dynamic<T> parameters, final Dynamic<T> result) {
		assertEquals(result, template.substitute(parameters));
		assertEquals(parameters, template.extract(result));
	}

	@Test
	public void substitutionInMap() {
		final DynamicTemplate template = DynamicTemplate.parse(map()
				.set("foo", string("$1"))
				.set("bar", map().set("baz", string("$2")))
		);
		assertSubstitutionAndExtraction(template,
				map()
						.set("1", string("hello"))
						.set("2", string("there")),
				map()
						.set("foo", string("hello"))
						.set("bar", map().set("baz", string("there")))
		);
	}

	@Test
	public void missingParameterInMap() {
		final DynamicTemplate template = DynamicTemplate.parse(map()
				.set("foo", string("$1"))
				.set("bar", string("$2"))
		);
		assertSubstitutionAndExtraction(template,
				map().set("1", string("hello")),
				map().set("foo", string("hello"))
		);
	}

	private static Dynamic<JsonElement> string(final String string) {
		return new Dynamic<>(OPS, new JsonPrimitive(string));
	}

	private static Dynamic<JsonElement> map() {
		return new Dynamic<>(OPS, new JsonObject());
	}
}