package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.client.toast.NotificationStyle;
import com.lovetropics.minigames.client.toast.ShowNotificationToastMessage;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.network.PacketDistributor;

public record NotificationToastAction(Component text, NotificationStyle style) implements IGameBehavior {
	public static final MapCodec<NotificationToastAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ExtraCodecs.COMPONENT.fieldOf("text").forGetter(NotificationToastAction::text),
			NotificationStyle.MAP_CODEC.forGetter(NotificationToastAction::style)
	).apply(i, NotificationToastAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		ShowNotificationToastMessage message = new ShowNotificationToastMessage(text, style);
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> target), message);
			return true;
		});
	}
}
