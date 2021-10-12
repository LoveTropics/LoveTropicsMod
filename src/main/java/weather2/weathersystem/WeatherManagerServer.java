package weather2.weathersystem;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
import weather2.PacketNBTFromServer;
import weather2.Weather;
import weather2.WeatherNetworking;
import weather2.util.CachedNBTTagCompound;
import weather2.util.WeatherUtilBlock;
import weather2.weathersystem.storm.WeatherObject;
import weather2.weathersystem.storm.WeatherObjectSandstorm;

import javax.annotation.Nullable;
import java.util.Iterator;

public class WeatherManagerServer extends WeatherManager {
	private final ServerWorld world;

	//TEMP
	public WeatherObjectSandstorm sandstorm = null;

	public WeatherManagerServer(ServerWorld world) {
		super(world.getDimensionKey());
		this.world = world;
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public void tick() {
		super.tick();
	}

	public WeatherObjectSandstorm spawnSandStorm(BlockPos pos) {
		WeatherObjectSandstorm sandstorm = new WeatherObjectSandstorm(this);

		sandstorm.initFirstTime();
		BlockPos posSpawn = new BlockPos(WeatherUtilBlock.getPrecipitationHeightSafe(world, pos)).add(0, 1, 0);
		sandstorm.initSandstormSpawn(new Vector3d(posSpawn.getX(), posSpawn.getY(), posSpawn.getZ()));
		addStormObject(sandstorm);
		syncStormNew(sandstorm, null);
		return sandstorm;
	}

	public void syncStormNew(WeatherObject parStorm, @Nullable ServerPlayerEntity entP) {
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormNew");

		CachedNBTTagCompound cache = parStorm.getNbtCache();
		cache.setUpdateForced(true);
		parStorm.nbtSyncForClient();
		cache.setUpdateForced(false);
		data.put("data", cache.getNewNBT());

		if (entP == null) {
			//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getMinecartType().getId());
			WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimensionKey()), new PacketNBTFromServer(data));
		} else {
			//Weather.eventChannel.sendTo(PacketHelper.getNBTPacket(data, Weather.eventChannelName), entP);
			//WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimension().getType()), new PacketNBTFromServer(data));
			WeatherNetworking.HANDLER.sendTo(new PacketNBTFromServer(data), entP.connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
		}
		//PacketDispatcher.sendPacketToAllAround(parStorm.pos.xCoord, parStorm.pos.yCoord, parStorm.pos.zCoord, syncRange, getWorld().provider.dimensionId, WeatherPacketHelper.createPacketForServerToClientSerialization("WeatherData", data));
	}

	public void syncStormUpdate(WeatherObject parStorm) {
		//packets
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormUpdate");
		parStorm.getNbtCache().setNewNBT(new CompoundNBT());
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());
		boolean testNetworkData = false;
		if (testNetworkData) {
			System.out.println("sending to client: " + parStorm.getNbtCache().getNewNBT().keySet().size());
			/*if (parStorm instanceof StormObject) {
				System.out.println("Real: " + ((StormObject) parStorm).levelCurIntensityStage);
				if (parStorm.getNbtCache().getNewNBT().contains("levelCurIntensityStage")) {
					System.out.println(" vs " + parStorm.getNbtCache().getNewNBT().getInt("levelCurIntensityStage"));
				} else {
					System.out.println("no key!");
				}
			}*/

			Iterator iterator = parStorm.getNbtCache().getNewNBT().keySet().iterator();
			String keys = "";
			while (iterator.hasNext()) {
				keys = keys.concat((String) iterator.next() + "; ");
			}
			System.out.println("sending    " + keys);
		}
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimensionKey()), new PacketNBTFromServer(data));
	}

	public void syncStormRemove(WeatherObject parStorm) {
		//packets
		CompoundNBT data = new CompoundNBT();
		data.putString("packetCommand", "WeatherData");
		data.putString("command", "syncStormRemove");
		parStorm.nbtSyncForClient();
		data.put("data", parStorm.getNbtCache().getNewNBT());
		//data.put("data", parStorm.nbtSyncForClient(new NBTTagCompound()));
		//fix for client having broken states
		data.getCompound("data").putBoolean("removed", true);
		//Weather.eventChannel.sendToDimension(PacketHelper.getNBTPacket(data, Weather.eventChannelName), getWorld().getDimension().getType().getId());
		WeatherNetworking.HANDLER.send(PacketDistributor.DIMENSION.with(() -> getWorld().getDimensionKey()), new PacketNBTFromServer(data));
	}

	public void playerJoinedWorldSyncFull(ServerPlayerEntity entP) {
		Weather.dbg("Weather2: playerJoinedWorldSyncFull for dim: " + world.getDimensionKey());
		World world = getWorld();
		if (world != null) {
			Weather.dbg("Weather2: playerJoinedWorldSyncFull, sending " + getStormObjects().size() + " weather objects to: " + entP.getName() + ", dim: " + world.getDimensionKey());
			//sync storms
			for (int i = 0; i < getStormObjects().size(); i++) {
				syncStormNew(getStormObjects().get(i), entP);
			}
		}
	}
}
