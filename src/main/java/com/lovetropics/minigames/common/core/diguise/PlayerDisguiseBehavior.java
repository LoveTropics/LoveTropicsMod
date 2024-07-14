package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityEvent;

import javax.annotation.Nullable;
import java.util.Objects;

@EventBusSubscriber(modid = Constants.MODID)
public final class PlayerDisguiseBehavior {
	private static final ResourceLocation ATTRIBUTE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Constants.MODID, "disguise_modifier");

	@SubscribeEvent
	public static void onSetEntitySize(EntityEvent.Size event) {
		PlayerDisguise disguise = PlayerDisguise.getOrNull(event.getEntity());
		if (disguise == null || !disguise.isDisguised()) {
			return;
		}

		Entity entity = Objects.requireNonNullElse(disguise.entity(), event.getEntity());
		float scale = disguise.type().scale();

		Pose pose = event.getPose();
		EntityDimensions disguiseDimensions = entity.getDimensions(pose);
		EntityDimensions actualDimensions = disguise.type().changesSize() ? disguiseDimensions : event.getEntity().getDimensions(pose);
		event.setNewSize(actualDimensions.scale(scale));
	}

	public static void applyAttributes(LivingEntity entity, LivingEntity disguise) {
		AttributeMap playerAttributes = entity.getAttributes();
		AttributeMap disguiseAttributes = disguise.getAttributes();

		BuiltInRegistries.ATTRIBUTE.holders().forEach(attribute -> {
			if (!disguiseAttributes.hasAttribute(attribute) || !playerAttributes.hasAttribute(attribute)) {
				return;
			}

			AttributeInstance playerInstance = playerAttributes.getInstance(attribute);
			AttributeInstance disguiseInstance = disguiseAttributes.getInstance(attribute);

			AttributeModifier modifier = createModifier(playerInstance, disguiseInstance);
			if (modifier != null) {
				playerInstance.addTransientModifier(modifier);
			}
		});
	}

	public static void clearAttributes(LivingEntity entity) {
		AttributeMap attributes = entity.getAttributes();
		BuiltInRegistries.ATTRIBUTE.holders().forEach(attribute -> {
			AttributeInstance instance = attributes.getInstance(attribute);
			if (instance != null) {
				instance.removeModifier(ATTRIBUTE_MODIFIER_ID);
			}
		});
	}

	@Nullable
	private static AttributeModifier createModifier(AttributeInstance player, AttributeInstance disguise) {
		double playerValue = player.getBaseValue();
		double disguiseValue = disguise.getBaseValue();

		// non-player movement speeds get effectively squared
		if (disguise.getAttribute() == Attributes.MOVEMENT_SPEED) {
			disguiseValue *= disguiseValue;
		}

		double value = disguiseValue - playerValue;
		if (Math.abs(value) <= 0.001) {
			return null;
		}

		return new AttributeModifier(
				ATTRIBUTE_MODIFIER_ID,
				value,
				AttributeModifier.Operation.ADD_VALUE
		);
	}

	public static void copyWalkAnimation(WalkAnimationState from, WalkAnimationState to) {
		to.update(from.position() - to.position() - from.speed(), 1.0f);
		to.setSpeed(from.speed(0.0f));
		to.update(from.speed(), 1.0f);
	}
}
