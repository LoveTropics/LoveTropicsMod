package com.lovetropics.minigames.common.content.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;

import java.util.Locale;
import java.util.Random;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class TrashBlock extends Block implements SimpleWaterloggedBlock {

	public enum Attachment implements StringRepresentable {
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
    public BlockState getStateForPlacement(BlockPlaceContext context) {
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
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
        }

        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
    	Vec3 offset = state.getOffset(worldIn, pos);
    	return type.getShape(state.getValue(FACING), state.getValue(ATTACHMENT)).move(offset.x, offset.y, offset.z);
    }

    @Override
    public Block.OffsetType getOffsetType() {
        return Block.OffsetType.XZ;
    }
}
