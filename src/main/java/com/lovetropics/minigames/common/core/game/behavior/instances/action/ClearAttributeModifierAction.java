package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public record ClearAttributeModifierAction(Attribute attribute, UUID id) implements IGameBehavior {
	public static final MapCodec<ClearAttributeModifierAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(ClearAttributeModifierAction::attribute),
			UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(ClearAttributeModifierAction::id)
	).apply(i, ClearAttributeModifierAction::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			final AttributeInstance attribute = player.getAttribute(this.attribute);
			if (attribute != null) {
				attribute.removeModifier(id);
				return true;
			}
			return false;
		});
	}
}
