package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public record AddAttributeModifierAction(Attribute attribute, AttributeModifier modifier) implements IGameBehavior {
	private static final Codec<AttributeModifier.Operation> MODIFIER_OPERATION_CODEC = MoreCodecs.stringVariants(AttributeModifier.Operation.values(), operation -> switch (operation) {
		case ADDITION -> "addition";
		case MULTIPLY_BASE -> "multiply_base";
		case MULTIPLY_TOTAL -> "multiply_total";
	});
	private static final Codec<AttributeModifier> MODIFIER_CODEC = RecordCodecBuilder.create(i -> i.group(
			UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(AttributeModifier::getId),
			Codec.STRING.fieldOf("name").forGetter(AttributeModifier::getName),
			Codec.DOUBLE.fieldOf("amount").forGetter(AttributeModifier::getAmount),
			MODIFIER_OPERATION_CODEC.fieldOf("operation").forGetter(AttributeModifier::getOperation)
	).apply(i, AttributeModifier::new));

	public static final MapCodec<AddAttributeModifierAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(AddAttributeModifierAction::attribute),
			MODIFIER_CODEC.fieldOf("modifier").forGetter(AddAttributeModifierAction::modifier)
	).apply(i, AddAttributeModifierAction::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			final AttributeInstance attribute = player.getAttribute(this.attribute);
			if (attribute != null) {
				if (!attribute.hasModifier(modifier)) {
					attribute.addTransientModifier(modifier);
				}
				return true;
			}
			return false;
		});
	}
}
