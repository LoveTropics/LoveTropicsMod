package com.lovetropics.minigames.common.content.survive_the_tide.block;

import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTide;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class BigRedButtonBlock extends ButtonBlock implements EntityBlock {
	public static final BooleanProperty TRIGGERED = BooleanProperty.create("triggered");

	public static final int PRESSED_DEPTH = 2;
	public static final int UNPRESSED_DEPTH = 3;
	public static final int HALF_SIZE = 5;
	private static final VoxelShape CEILING_AABB_X = Block.box(8.0 - HALF_SIZE, 16.0 - UNPRESSED_DEPTH, 8.0 - HALF_SIZE, 8.0 + HALF_SIZE, 16.0, 8.0 + HALF_SIZE);
	private static final VoxelShape CEILING_AABB_Z = Block.box(8.0 - HALF_SIZE, 16.0 - UNPRESSED_DEPTH, 8.0 - HALF_SIZE, 8.0 + HALF_SIZE, 16.0, 8.0 + HALF_SIZE);
	private static final VoxelShape FLOOR_AABB_X = Block.box(8.0 - HALF_SIZE, 0.0, 8.0 - HALF_SIZE, 8.0 + HALF_SIZE, UNPRESSED_DEPTH, 8.0 + HALF_SIZE);
	private static final VoxelShape FLOOR_AABB_Z = Block.box(8.0 - HALF_SIZE, 0.0, 8.0 - HALF_SIZE, 8.0 + HALF_SIZE, UNPRESSED_DEPTH, 8.0 + HALF_SIZE);
	private static final VoxelShape NORTH_AABB = Block.box(8.0 - HALF_SIZE, 8.0 - HALF_SIZE, 16.0 - UNPRESSED_DEPTH, 8.0 + HALF_SIZE, 8.0 + HALF_SIZE, 16.0);
	private static final VoxelShape SOUTH_AABB = Block.box(8.0 - HALF_SIZE, 8.0 - HALF_SIZE, 0.0, 8.0 + HALF_SIZE, 8.0 + HALF_SIZE, UNPRESSED_DEPTH);
	private static final VoxelShape WEST_AABB = Block.box(16.0 - UNPRESSED_DEPTH, 8.0 - HALF_SIZE, 8.0 - HALF_SIZE, 16.0, 8.0 + HALF_SIZE, 8.0 + HALF_SIZE);
	private static final VoxelShape EAST_AABB = Block.box(0.0, 8.0 - HALF_SIZE, 8.0 - HALF_SIZE, UNPRESSED_DEPTH, 8.0 + HALF_SIZE, 8.0 + HALF_SIZE);
	private static final VoxelShape PRESSED_CEILING_AABB_X = Block.box(8.0 - HALF_SIZE, 16.0 - PRESSED_DEPTH, 8.0 - HALF_SIZE, 8.0 + HALF_SIZE, 16.0, 8.0 + HALF_SIZE);
	private static final VoxelShape PRESSED_CEILING_AABB_Z = Block.box(8.0 - HALF_SIZE, 16.0 - PRESSED_DEPTH, 8.0 - HALF_SIZE, 8.0 + HALF_SIZE, 16.0, 8.0 + HALF_SIZE);
	private static final VoxelShape PRESSED_FLOOR_AABB_X = Block.box(8.0 - HALF_SIZE, 0.0, 8.0 - HALF_SIZE, 8.0 + HALF_SIZE, PRESSED_DEPTH, 8.0 + HALF_SIZE);
	private static final VoxelShape PRESSED_FLOOR_AABB_Z = Block.box(8.0 - HALF_SIZE, 0.0, 8.0 - HALF_SIZE, 8.0 + HALF_SIZE, PRESSED_DEPTH, 8.0 + HALF_SIZE);
	private static final VoxelShape PRESSED_NORTH_AABB = Block.box(8.0 - HALF_SIZE, 8.0 - HALF_SIZE, 16.0 - PRESSED_DEPTH, 8.0 + HALF_SIZE, 8.0 + HALF_SIZE, 16.0);
	private static final VoxelShape PRESSED_SOUTH_AABB = Block.box(8.0 - HALF_SIZE, 8.0 - HALF_SIZE, 0.0, 8.0 + HALF_SIZE, 8.0 + HALF_SIZE, PRESSED_DEPTH);
	private static final VoxelShape PRESSED_WEST_AABB = Block.box(16.0 - PRESSED_DEPTH, 8.0 - HALF_SIZE, 8.0 - HALF_SIZE, 16.0, 8.0 + HALF_SIZE, 8.0 + HALF_SIZE);
	private static final VoxelShape PRESSED_EAST_AABB = Block.box(0.0, 8.0 - HALF_SIZE, 8.0 - HALF_SIZE, PRESSED_DEPTH, 8.0 + HALF_SIZE, 8.0 + HALF_SIZE);

	public BigRedButtonBlock(Properties properties) {
		super(properties, BlockSetType.STONE, SharedConstants.TICKS_PER_SECOND * 3, false);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, Boolean.FALSE).setValue(FACE, AttachFace.WALL).setValue(TRIGGERED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		Direction direction = state.getValue(FACING);
		boolean pressed = state.getValue(POWERED);
		return switch (state.getValue(FACE)) {
			case FLOOR -> switch (direction.getAxis()) {
				case X -> pressed ? PRESSED_FLOOR_AABB_X : FLOOR_AABB_X;
				default -> pressed ? PRESSED_FLOOR_AABB_Z : FLOOR_AABB_Z;
			};
			case WALL -> switch (direction) {
				case EAST -> pressed ? PRESSED_EAST_AABB : EAST_AABB;
				case WEST -> pressed ? PRESSED_WEST_AABB : WEST_AABB;
				case SOUTH -> pressed ? PRESSED_SOUTH_AABB : SOUTH_AABB;
				case NORTH, UP, DOWN -> pressed ? PRESSED_NORTH_AABB : NORTH_AABB;
			};
			default -> switch (direction.getAxis()) {
				case X -> pressed ? PRESSED_CEILING_AABB_X : CEILING_AABB_X;
				default -> pressed ? PRESSED_CEILING_AABB_Z : CEILING_AABB_Z;
			};
		};
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof BigRedButtonBlockEntity entity) {
			if (entity.press(player)) {
				trigger(state, level, pos, player);
				return InteractionResult.SUCCESS;
			}
		}
		return super.use(state, level, pos, player, hand, hit);
	}

	private void trigger(BlockState state, Level level, BlockPos pos, Player player) {
		level.setBlock(pos, state.setValue(POWERED, true).setValue(TRIGGERED, true), Block.UPDATE_ALL);
		level.updateNeighborsAt(pos, this);
		level.updateNeighborsAt(pos.relative(getConnectedDirection(state).getOpposite()), this);

		level.playSound(player, pos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS);
		level.gameEvent(player, GameEvent.BLOCK_ACTIVATE, pos);
	}

	@Override
	protected void checkPressed(BlockState state, Level level, BlockPos pos) {
		if (!state.getValue(TRIGGERED)) {
			super.checkPressed(state, level, pos);
		}
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return state.getValue(TRIGGERED) ? 15 : 0;
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction side) {
		return state.getValue(TRIGGERED) && getConnectedDirection(state) == side ? 15 : 0;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(TRIGGERED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BigRedButtonBlockEntity(SurviveTheTide.BIG_RED_BUTTON_ENTITY.get(), pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide()) {
			return null;
		}
		return createTickerHelper(type, SurviveTheTide.BIG_RED_BUTTON_ENTITY.get(), BigRedButtonBlockEntity::tick);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> actualType, BlockEntityType<E> type, BlockEntityTicker<? super E> ticker) {
		return type == actualType ? (BlockEntityTicker<A>) ticker : null;
	}
}
