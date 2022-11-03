package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundClearTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;

import java.util.Optional;

public record ShowTitleAction(Optional<Component> title, Optional<Component> subtitle, Optional<Component> actionBar, int fadeIn, int stay, int fadeOut) implements IGameBehavior {
	public static final Codec<ShowTitleAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.TEXT.optionalFieldOf("title").forGetter(ShowTitleAction::title),
			MoreCodecs.TEXT.optionalFieldOf("subtitle").forGetter(ShowTitleAction::subtitle),
			MoreCodecs.TEXT.optionalFieldOf("action_bar").forGetter(ShowTitleAction::actionBar),
			Codec.INT.optionalFieldOf("fade_in", SharedConstants.TICKS_PER_SECOND / 4).forGetter(ShowTitleAction::fadeIn),
			Codec.INT.optionalFieldOf("stay", SharedConstants.TICKS_PER_SECOND * 2).forGetter(ShowTitleAction::stay),
			Codec.INT.optionalFieldOf("fade_out", SharedConstants.TICKS_PER_SECOND / 4).forGetter(ShowTitleAction::fadeOut)
	).apply(i, ShowTitleAction::new));

	private static final Component EMPTY_TITLE = new TextComponent(" ");

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			target.connection.send(new ClientboundClearTitlesPacket(true));
			target.connection.send(new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
			target.connection.send(new ClientboundSetTitleTextPacket(title.orElse(EMPTY_TITLE)));
			subtitle.ifPresent(subtitle -> target.connection.send(new ClientboundSetSubtitleTextPacket(subtitle)));
			actionBar.ifPresent(actionBar -> target.connection.send(new ClientboundSetActionBarTextPacket(actionBar)));
			return true;
		});
	}
}
