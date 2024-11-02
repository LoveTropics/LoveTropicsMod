package com.lovetropics.minigames.common.content.survive_the_tide.block;

import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTide;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class BigRedButtonBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final String TAG_PRESSED = "pressed";
	private static final String TAG_PRESENT = "present";
	private static final String TAG_REQUIREMENTS = "requirements";
	private static final String TAG_TRIGGER_POS = "trigger_pos";

	private boolean pressed;

	private Requirements requirements = new Requirements(0.0f, 1, Requirements.DEFAULT_DISTANCE);
	private int playersPresentCount;
	private int playersRequiredCount;

	@Nullable
	private BlockPos triggerPos;

	public BigRedButtonBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, BigRedButtonBlockEntity entity) {
		if (state.getValue(BigRedButtonBlock.TRIGGERED)) {
			return;
		}

		IGamePhase game = IGameManager.get().getGamePhaseAt(level, pos);
		int smallestTeamSize = game != null ? getSmallestTeamSize(game) : 1;
		int requiredCount = entity.requirements.resolve(smallestTeamSize);
		int presentCount = game != null && entity.pressed ? entity.countPlayersPresent(game, pos) : 0;
		entity.updatePlayerCount(presentCount, requiredCount);

		if (entity.pressed && presentCount >= requiredCount) {
			entity.trigger();
			entity.pressed = false;
		}
	}

	private int countPlayersPresent(IGamePhase game, BlockPos pos) {
		int count = 0;
		for (ServerPlayer player : game.participants()) {
			if (pos.closerToCenterThan(player.position(), requirements.distance)) {
				count++;
			}
		}
		return count;
	}

	private static int getSmallestTeamSize(IGamePhase game) {
		TeamState teams = game.instanceState().getOrNull(TeamState.KEY);
		if (teams == null) {
			return 1;
		}
		int smallestTeamSize = game.participants().size();
		for (GameTeam team : teams) {
			int teamSize = teams.getParticipantsForTeam(game, team.key()).size();
			if (teamSize > 0 && teamSize < smallestTeamSize) {
				smallestTeamSize = teamSize;
			}
		}
		return smallestTeamSize;
	}

	private void updatePlayerCount(int presentCount, int requiredCount) {
		if (presentCount == playersPresentCount && requiredCount == playersRequiredCount) {
			return;
		}
		playersPresentCount = presentCount;
		playersRequiredCount = requiredCount;
		markUpdated();
	}

	public void press() {
		// No game is active, just allow instant trigger
		if (IGameManager.get().getGamePhaseAt(level, getBlockPos()) == null) {
			trigger();
			return;
		}
		pressed = true;
	}

	private void trigger() {
		BigRedButtonBlock.trigger(getBlockState(), level, getBlockPos());
		if (triggerPos != null) {
			BlockState triggerState = level.getBlockState(triggerPos);
			if (triggerState.is(SurviveTheTide.LOOT_DISPENSER) && triggerState.getValue(LootDispenserBlock.STATE) == LootDispenserBlock.State.INACTIVE) {
				level.setBlockAndUpdate(triggerPos, triggerState.setValue(LootDispenserBlock.STATE, LootDispenserBlock.State.ACTIVE));
			}
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		tag.putBoolean(TAG_PRESSED, pressed);
		tag.put(TAG_REQUIREMENTS, Requirements.CODEC.encodeStart(NbtOps.INSTANCE, requirements).getOrThrow());
		if (triggerPos != null) {
			tag.put(TAG_TRIGGER_POS, NbtUtils.writeBlockPos(triggerPos));
		}
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		pressed = tag.getBoolean(TAG_PRESSED);
		Requirements.CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_REQUIREMENTS))
				.resultOrPartial(LOGGER::error)
				.ifPresent(r -> requirements = r);
		triggerPos = NbtUtils.readBlockPos(tag, TAG_TRIGGER_POS).orElse(null);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putInt(TAG_PRESENT, playersPresentCount);
		tag.putInt(TAG_REQUIREMENTS, playersRequiredCount);
		return tag;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		if (pkt.getTag() != null) {
			handleUpdateTag(pkt.getTag(), registries);
		}
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		playersPresentCount = tag.getInt(TAG_PRESENT);
		playersRequiredCount = tag.getInt(TAG_REQUIREMENTS);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	private void markUpdated() {
		setChanged();
		level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
	}

	public int getPlayersPresentCount() {
		return playersPresentCount;
	}

	public int getPlayersRequiredCount() {
		return playersRequiredCount;
	}

	private record Requirements(float percent, int count, float distance) {
		public static final float DEFAULT_DISTANCE = 4.0f;
		public static final Codec<Requirements> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.FLOAT.optionalFieldOf("percent", 0.0f).forGetter(Requirements::percent),
				Codec.INT.optionalFieldOf("count", 1).forGetter(Requirements::count),
				Codec.FLOAT.optionalFieldOf("distance", DEFAULT_DISTANCE).forGetter(Requirements::distance)
		).apply(i, Requirements::new));

		public int resolve(int smallestTeamSize) {
			int preferredCount = Math.max(
					Mth.floor(percent * smallestTeamSize),
					count
			);
			return Math.min(preferredCount, smallestTeamSize);
		}
	}
}
