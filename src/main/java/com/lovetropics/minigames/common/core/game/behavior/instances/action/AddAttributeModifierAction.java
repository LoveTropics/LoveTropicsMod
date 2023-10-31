package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.util.Codecs;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.registries.ForgeRegistries;

public record AddAttributeModifierAction(Attribute attribute, AttributeModifier modifier) implements IGameBehavior {
	public static final MapCodec<AddAttributeModifierAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(AddAttributeModifierAction::attribute),
			Codecs.ATTRIBUTE_MODIFIER.fieldOf("modifier").forGetter(AddAttributeModifierAction::modifier)
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
