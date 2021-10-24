package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant;

public enum PlantFamily {
    CROP("crop"),
    PLANT("plant"),
    TREE("tree");

    private final String name;

    PlantFamily(String name) {
        this.name = name;
    }

    public String friendlyName() {
        return name;
    }
}
