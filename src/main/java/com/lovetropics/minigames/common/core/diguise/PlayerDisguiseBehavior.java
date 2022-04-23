package com.lovetropics.minigames.common.core.diguise;

import com.lovetropics.minigames.Constants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Registry;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.UUID;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class PlayerDisguiseBehavior {
	private static final UUID ATTRIBUTE_MODIFIER_UUID = UUID.randomUUID();

	@SubscribeEvent
	public static void onSetEntitySize(EntityEvent.Size event) {
		Entity entity = event.getEntity();

		if (entity instanceof Player) {
			Player player = (Player) entity;
			Entity disguise = PlayerDisguise.getDisguiseEntity(player);

			if (disguise != null) {
				Pose pose = event.getPose();
				EntityDimensions size = disguise.getDimensions(pose);

				event.setNewSize(size);
				event.setNewEyeHeight(disguise.getEyeHeightAccess(pose, size));
			}
		}
	}

	public static void applyAttributes(Player player, LivingEntity disguise) {
		AttributeMap playerAttributes = player.getAttributes();
		AttributeMap disguiseAttributes = disguise.getAttributes();

		for (Attribute attribute : Registry.ATTRIBUTE) {
			if (!disguiseAttributes.hasAttribute(attribute) || !playerAttributes.hasAttribute(attribute)) {
				continue;
			}

			AttributeInstance playerInstance = playerAttributes.getInstance(attribute);
			AttributeInstance disguiseInstance = disguiseAttributes.getInstance(attribute);

			AttributeModifier modifier = createModifier(playerInstance, disguiseInstance);
			if (modifier != null) {
				playerInstance.addTransientModifier(modifier);
			}
		}
	}

	public static void clearAttributes(Player player) {
		AttributeMap attributes = player.getAttributes();
		for (Attribute attribute : Registry.ATTRIBUTE) {
			AttributeInstance instance = attributes.getInstance(attribute);
			if (instance != null) {
				instance.removeModifier(ATTRIBUTE_MODIFIER_UUID);
			}
		}
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
				ATTRIBUTE_MODIFIER_UUID, "disguise",
				value,
				AttributeModifier.Operation.ADDITION
		);
	}
}
