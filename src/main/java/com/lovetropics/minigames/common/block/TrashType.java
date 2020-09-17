package com.lovetropics.minigames.common.block;

import java.util.Locale;

import javax.annotation.Nullable;

import com.lovetropics.minigames.common.Util;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import net.minecraft.block.Block;
import net.minecraft.util.math.shapes.VoxelShape;

public enum TrashType implements NonNullSupplier<Block> {

    CAN(9, 7),
    CHIP_BAG(16, 1),
    COLA_STANDING(5, 9),
    COLA(9, 5),
    PLASTIC_BAG(16, 1),
    PLASTIC_BOTTLE(12, 5),
    PLASTIC_RINGS(16, 1),
    STRAW(8, 1),
    ;
    
    private final String name;
    private final int w, h;
    private final VoxelShape shape;

    private TrashType() {
        this(7);
    }

    private TrashType(int w) {
        this(w, 15);
    }

    private TrashType(int w, int h) {
        this(null, w, h);
    }
    
    private TrashType(@Nullable String name, int w, int h) {
        this.name = name == null ? Util.toEnglishName(name()) : name;
        this.w = w;
        this.h = h;
        float halfW = w / 2f;
        this.shape = Block.makeCuboidShape(8 - halfW, 0, 8 - halfW, 8 + halfW, h, 8 + halfW);
    }
    
    public int getModelYOffset() {
        return Math.max(16 - h - 4, 0) / 2;
    }
    
    public float getModelScale(float base) {
        return ((16 - Math.max(w, h)) / 16f) * base + base;
    }

    public VoxelShape getShape() {
        return shape;
    }
    
    public String getId() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public Block get() {
        return LoveTropicsBlocks.TRASH.get(this).get();
    }

    public String getEnglishName() {
        return name;
    }
}
