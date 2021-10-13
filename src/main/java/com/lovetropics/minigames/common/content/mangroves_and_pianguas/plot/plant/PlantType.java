package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.state.JackOLantern;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.state.Pumpkin;
import net.minecraft.util.Unit;

public final class PlantType<S> {
	public static final PlantType<JackOLantern> JACK_O_LANTERN = create();
	public static final PlantType<Pumpkin> PUMPKIN = create();
	public static final PlantType<Unit> BIRCH_TREE = create();
	public static final PlantType<Unit> MELON = create();

	private PlantType() {
	}

	public static <S> PlantType<S> create() {
		return new PlantType<>();
	}
}
