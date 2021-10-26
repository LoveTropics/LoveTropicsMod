package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

public enum PlantFamily {
    CROP("crop", true),
    PLANT("plant", true),
    SAPLING("sapling", false),
    TREE("tree", true);

    public static final ImmutableSet<PlantFamily> BIODIVERSITY_VALUES = ImmutableSet.copyOf(Arrays.stream(values())
    		.filter(f -> f.countsForBiodiversity)
    		.collect(Collectors.toList()));

    private final String name;
    private final boolean countsForBiodiversity;

    PlantFamily(String name, boolean countsForBiodiversity) {
        this.name = name;
        this.countsForBiodiversity = countsForBiodiversity;
    }

    public String friendlyName() {
        return name;
    }
}
