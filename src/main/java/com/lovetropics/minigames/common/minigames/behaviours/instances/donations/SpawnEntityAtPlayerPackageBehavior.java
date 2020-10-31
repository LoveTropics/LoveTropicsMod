package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.lovetropics.minigames.common.Util;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;

import java.util.Optional;

public class SpawnEntityAtPlayerPackageBehavior extends DonationPackageBehavior
{
	private final ResourceLocation entityId;

	public SpawnEntityAtPlayerPackageBehavior(final String packageType, final ResourceLocation entityId, final ITextComponent messageForPlayer, final PlayerSelect playerSelect) {
		super(packageType, messageForPlayer, playerSelect);

		this.entityId = entityId;
	}

	public static <T> SpawnEntityAtPlayerPackageBehavior parse(Dynamic<T> root) {
		final String packageType = root.get("package_type").asString("");
		final ResourceLocation entity_id = new ResourceLocation(root.get("entity_id").asString(""));
		final ITextComponent messageForPlayer = Util.getText(root, "message_for_player");
		final PlayerSelect playerSelect = PlayerSelect.getFromType(root.get("player_select").asString(PlayerSelect.RANDOM.getType())).get();

		return new SpawnEntityAtPlayerPackageBehavior(packageType, entity_id, messageForPlayer, playerSelect);
	}

	@Override
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		Util.spawnEntity(entityId, player.getServerWorld(), player.getPosX(), player.getPosY(), player.getPosZ());
	}
}
