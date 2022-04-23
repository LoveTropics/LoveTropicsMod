package com.lovetropics.minigames.common.content.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

import java.util.Locale;
import java.util.Random;

public class TrashBlock extends Block implements IWaterLoggable {

	public enum Attachment implements IStringSerializable {
		FLOOR,
		WALL,
		CEILING,
		;

		private static final Attachment[] VALUES = values();

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}

		public static Attachment random(Random rand) {
			return VALUES[rand.nextInt(VALUES.length)];
		}
	}

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Attachment> ATTACHMENT = EnumProperty.create("attachment", Attachment.class);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final TrashType type;

    public TrashBlock(TrashType type, Properties properties) {
        super(properties);
        this.type = type;
        registerDefaultState(this.stateDefinition.any().setValue(ATTACHMENT, Attachment.FLOOR).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ATTACHMENT, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context)
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(WATERLOGGED, fluid.is(FluidTags.WATER) && fluid.getAmount() == 8);
    }

    @Override
    @Deprecated
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @Deprecated
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
    	Vector3d offset = state.getOffset(worldIn, pos);
    	return type.getShape(state.getValue(FACING), state.getValue(ATTACHMENT)).move(offset.x, offset.y, offset.z);
    }

    @Override
    public Block.OffsetType getOffsetType() {
        return Block.OffsetType.XZ;
    }
}
