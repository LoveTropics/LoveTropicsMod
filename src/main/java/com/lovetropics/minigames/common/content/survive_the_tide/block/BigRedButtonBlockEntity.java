package com.lovetropics.minigames.common.content.survive_the_tide.block;

import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTide;
import com.lovetropics.minigames.common.core.game.IGameManager;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import com.lovetropics.minigames.common.core.game.state.team.TeamState;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Set;
import java.util.UUID;

public class BigRedButtonBlockEntity extends BlockEntity {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final String TAG_PLAYERS = "players";
	private static final String TAG_REQUIREMENTS = "requirements";
	private static final String TAG_TRIGGER_POS = "trigger_pos";

	private final Set<UUID> playersPressed = new ObjectOpenHashSet<>();
	private int playersPressedCount;

	private Requirements requirements = new Requirements(0.0f, 1);
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
		int resolvedRequirement = entity.requirements.resolve(smallestTeamSize);
		entity.updateRequiredPlayers(resolvedRequirement);
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

	private void updateRequiredPlayers(int count) {
		if (count != playersRequiredCount) {
			playersRequiredCount = count;
			markUpdated();
		}
	}

	public boolean press(Player player) {
		// No game is active, just allow instant trigger
		if (IGameManager.get().getGamePhaseAt(level, getBlockPos()) == null) {
			applyTriggerEffects();
			return true;
		}
		if (playersPressed.add(player.getUUID())) {
			playersPressedCount = playersPressed.size();
			markUpdated();
		}
		if (playersPressedCount >= playersRequiredCount) {
			applyTriggerEffects();
			return true;
		}
		return false;
	}

	private void applyTriggerEffects() {
		if (triggerPos != null) {
			BlockState state = level.getBlockState(triggerPos);
			if (state.is(SurviveTheTide.LOOT_DISPENSER) && state.getValue(LootDispenserBlock.STATE) == LootDispenserBlock.State.INACTIVE) {
				level.setBlockAndUpdate(triggerPos, state.setValue(LootDispenserBlock.STATE, LootDispenserBlock.State.ACTIVE));
			}
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		ListTag playersList = new ListTag();
		for (UUID id : playersPressed) {
			playersList.add(NbtUtils.createUUID(id));
		}
		tag.put(TAG_PLAYERS, playersList);
		tag.put(TAG_REQUIREMENTS, Requirements.CODEC.encodeStart(NbtOps.INSTANCE, requirements).getOrThrow());
		if (triggerPos != null) {
			tag.put(TAG_TRIGGER_POS, NbtUtils.writeBlockPos(triggerPos));
		}
	}

	@Override
	public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		playersPressed.clear();
		for (Tag id : tag.getList(TAG_PLAYERS, Tag.TAG_LIST)) {
			playersPressed.add(NbtUtils.loadUUID(id));
		}
		playersPressedCount = playersPressed.size();
		Requirements.CODEC.parse(NbtOps.INSTANCE, tag.get(TAG_REQUIREMENTS))
				.resultOrPartial(LOGGER::error)
				.ifPresent(r -> requirements = r);
		triggerPos = NbtUtils.readBlockPos(tag, TAG_TRIGGER_POS).orElse(null);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putInt(TAG_PLAYERS, playersPressed.size());
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
		playersPressedCount = tag.getInt(TAG_PLAYERS);
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

	public int getPlayersPressedCount() {
		return playersPressedCount;
	}

	public int getPlayersRequiredCount() {
		return playersRequiredCount;
	}

	private record Requirements(float percent, int count) {
		public static final Codec<Requirements> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.FLOAT.optionalFieldOf("percent", 0.0f).forGetter(Requirements::percent),
				Codec.INT.optionalFieldOf("count", 1).forGetter(Requirements::count)
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
