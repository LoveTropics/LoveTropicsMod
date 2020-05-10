package CoroUtil.util;

import java.util.Random;

import net.minecraft.util.math.Vec3d;


public class CoroUtilParticle {

    public static Vec3d[] rainPositions;
    public static int maxRainDrops = 2000;
    
    public static Random rand = new Random();
    
    static {
    	rainPositions = new Vec3d[maxRainDrops];
        
        float range = 10F;
        
        for (int i = 0; i < maxRainDrops; i++) {
        	rainPositions[i] = new Vec3d((rand.nextFloat() * range) - (range/2), (rand.nextFloat() * range/1) - (range/2), (rand.nextFloat() * range) - (range/2));
        }
    }
	
}
