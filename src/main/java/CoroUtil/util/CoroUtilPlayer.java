package CoroUtil.util;

import java.util.WeakHashMap;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public class CoroUtilPlayer {

	public static WeakHashMap<PlayerEntity, Vec3d> lookupPlayerToLastPos = new WeakHashMap<>();
	public static WeakHashMap<PlayerEntity, Vec3d> lookupPlayerToLastSpeed = new WeakHashMap<>();

	/**
	 * Currently used for tracking player SERVER SIDE speed
	 *
	 * @param player
	 */
	public static void trackPlayerForSpeed(PlayerEntity player) {
		
		//TODO: edge cases like teleporting and respawning, reconnecting, etc
		//cap max speed, we dont need too much accuracy for now
		
		Vec3d vecPos = player.getPositionVec();
		if (!lookupPlayerToLastPos.containsKey(player)) {
			lookupPlayerToLastPos.put(player, vecPos);
			return;
		} else {
			Vec3d vecLastPos = lookupPlayerToLastPos.get(player);
			
			Vec3d vecDiff = vecPos.subtract(vecLastPos);
			lookupPlayerToLastSpeed.put(player, vecDiff);
			
			lookupPlayerToLastPos.put(player, vecPos);
		}
	}
}
