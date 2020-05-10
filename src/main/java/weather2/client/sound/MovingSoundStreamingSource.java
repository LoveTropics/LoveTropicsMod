package weather2.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import weather2.weathersystem.storm.StormObject;

public class MovingSoundStreamingSource extends TickableSound {

	private StormObject storm = null;
	public float cutOffRange = 128;
	public Vec3d realSource = null;
    public boolean lockToPlayer = false;

    public MovingSoundStreamingSource(Vec3d parPos, SoundEvent event, SoundCategory category, float parVolume, float parPitch, boolean lockToPlayer) {
        super(event, category);
        this.repeat = false;
        this.volume = parVolume;
        this.pitch = parPitch;
        this.realSource = parPos;

        this.lockToPlayer = lockToPlayer;

        tick();
    }

	//constructor for non moving sounds
    public MovingSoundStreamingSource(Vec3d parPos, SoundEvent event, SoundCategory category, float parVolume, float parPitch, float parCutOffRange)
    {
        super(event, category);
        this.repeat = false;
        this.volume = parVolume;
        this.pitch = parPitch;
        cutOffRange = parCutOffRange;
        realSource = parPos;
        
        //sync position
        tick();
    }
    
    //constructor for moving sounds
    public MovingSoundStreamingSource(StormObject parStorm, SoundEvent event, SoundCategory category, float parVolume, float parPitch, float parCutOffRange)
    {
        super(event, category);
        this.storm = parStorm;
        this.repeat = false;
        this.volume = parVolume;
        this.pitch = parPitch;
        cutOffRange = parCutOffRange;
        
        //sync position
        tick();
    }

    public void tick()
    {
    	PlayerEntity entP = Minecraft.getInstance().player;
    	
    	if (entP != null) {
    		this.x = (float) entP.getPosX();
    		this.y = (float) entP.getPosY();
    		this.z = (float) entP.getPosZ();
    	}
    	
    	if (storm != null) {
    		realSource = this.storm.posGround;
    	}

    	//if locked to player, don't dynamically adjust volume
    	if (!lockToPlayer) {
            float var3 = (float)((cutOffRange - (double)MathHelper.sqrt(getDistanceFrom(realSource, entP.getPositionVec()))) / cutOffRange);

            if (var3 < 0.0F)
            {
                var3 = 0.0F;
            }

            volume = var3;
        }

    }
    
    public double getDistanceFrom(Vec3d source, Vec3d targ)
    {
    	return source.subtract(targ).length();
    }
}
