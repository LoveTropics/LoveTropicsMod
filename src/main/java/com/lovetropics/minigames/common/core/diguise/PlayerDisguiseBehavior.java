package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class PlayerDisguiseBehavior {
	private static final UUID ATTRIBUTE_MODIFIER_UUID = UUID.randomUUID();

	@SubscribeEvent
	public static void onSetEntitySize(EntityEvent.Size event) {
		Entity entity = event.getEntity();

		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			Entity disguise = PlayerDisguise.getDisguiseEntity(player);

			if (disguise != null) {
				Pose pose = event.getPose();
				EntitySize size = disguise.getSize(pose);

				event.setNewSize(size);
				event.setNewEyeHeight(disguise.getEyeHeightAccess(pose, size));
			}
		}
	}

	public static void applyAttributes(PlayerEntity player, LivingEntity disguise) {
		AttributeModifierManager playerAttributes = player.getAttributeManager();
		AttributeModifierManager disguiseAttributes = disguise.getAttributeManager();

		for (Attribute attribute : Registry.ATTRIBUTE) {
			if (!disguiseAttributes.hasAttributeInstance(attribute) || !playerAttributes.hasAttributeInstance(attribute)) {
				continue;
			}

			ModifiableAttributeInstance playerInstance = playerAttributes.createInstanceIfAbsent(attribute);
			ModifiableAttributeInstance disguiseInstance = disguiseAttributes.createInstanceIfAbsent(attribute);

			AttributeModifier modifier = createModifier(playerInstance, disguiseInstance);
			if (modifier != null) {
				playerInstance.applyNonPersistentModifier(modifier);
			}
		}
	}

	public static void clearAttributes(PlayerEntity player) {
		AttributeModifierManager attributes = player.getAttributeManager();
		for (Attribute attribute : Registry.ATTRIBUTE) {
			ModifiableAttributeInstance instance = attributes.createInstanceIfAbsent(attribute);
			if (instance != null) {
				instance.removeModifier(ATTRIBUTE_MODIFIER_UUID);
			}
		}
	}

	@Nullable
	private static AttributeModifier createModifier(ModifiableAttributeInstance player, ModifiableAttributeInstance disguise) {
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
				ATTRIBUTE_MODIFIER_UUID, "disguise",
				value,
				AttributeModifier.Operation.ADDITION
		);
	}
}
