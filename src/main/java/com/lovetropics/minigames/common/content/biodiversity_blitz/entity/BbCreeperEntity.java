package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbGroundNavigator;
import com.lovetropics.minigames.common.content.biodiversity_blitz.explosion.FilteredExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.KaboomCropGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.explosion.PlantAffectingExplosion;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SExplosionPacket;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BbCreeperEntity extends CreeperEntity implements BbMobEntity {
    private final BbMobBrain mobBrain;
    private final Plot plot;
    
    public BbCreeperEntity(EntityType<? extends CreeperEntity> type, World world, Plot plot) {
        super(type, world);
        this.mobBrain = new BbMobBrain(plot.walls);
        this.plot = plot;

        // Ignore sweet berry bushes and water
        this.setPathPriority(PathNodeType.DANGER_OTHER, 0.0F);
        this.setPathPriority(PathNodeType.DAMAGE_OTHER, 0.0F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);

        this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(8);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SwimGoal(this));
        this.goalSelector.addGoal(2, new KaboomCropGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookRandomlyGoal(this));
    }

    @Override
    public void explode() {
        if (!this.world.isRemote) {
            double x = this.getPosX();
            double y = this.getPosY();
            double z = this.getPosZ();

            Explosion explosion = new PlantAffectingExplosion(this.world, null, null, null, x, y, z, 3.0f, false, Explosion.Mode.BREAK, e -> true, this.plot);
            explosion.doExplosionA();
            explosion.doExplosionB(false);

            for (ServerPlayerEntity player : ((ServerWorld) this.world).getPlayers()) {
                if (player.getDistanceSq(x, y, z) < 4096.0) {
                    player.connection.sendPacket(new SExplosionPacket(x, y, z, 3.0f, explosion.getAffectedBlockPositions(), explosion.getPlayerKnockbackMap().get(player)));
                }
            }

            this.remove();
        }
    }

    @Override
    protected Vector3d maybeBackOffFromEdge(Vector3d offset, MoverType mover) {
        return mobBrain.getPlotWalls().collide(this.getBoundingBox(), offset);
    }

    @Override
    protected PathNavigator createNavigator(World world) {
        return new BbGroundNavigator(this);
    }

    @Override
    public BbMobBrain getMobBrain() {
        return this.mobBrain;
    }

    @Override
    public MobEntity asMob() {
        return this;
    }

    @Override
    public Plot getPlot() {
        return this.plot;
    }
}
