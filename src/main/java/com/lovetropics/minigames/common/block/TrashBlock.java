package com.lovetropics.minigames.common.block;

import java.util.Locale;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;

public class TrashBlock extends Block implements IWaterLoggable {

	public enum Attachment implements IStringSerializable {
		FLOOR,
		WALL,
		CEILING,
		;

		private static Attachment[] VALUES = values();

		@Override
		public String getName() {
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
        setDefaultState(this.stateContainer.getBaseState().with(ATTACHMENT, Attachment.FLOOR).with(WATERLOGGED, false));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING, ATTACHMENT, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
        return super.getStateForPlacement(context)
                .with(FACING, context.getPlacementHorizontalFacing())
                .with(WATERLOGGED, Boolean.valueOf(ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8));
    }

    @Override
    @Deprecated
    public IFluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    @Deprecated
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.get(WATERLOGGED)) {
            worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
        }

        return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
    	Vec3d offset = state.getOffset(worldIn, pos);
    	return type.getShape(state.get(FACING), state.get(ATTACHMENT)).withOffset(offset.x, offset.y, offset.z);
    }

    @Override
    public Block.OffsetType getOffsetType() {
        return Block.OffsetType.XZ;
    }
}
