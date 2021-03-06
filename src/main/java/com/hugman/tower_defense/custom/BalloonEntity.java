package com.hugman.tower_defense.custom;

import com.hugman.tower_defense.TowerDefense;
import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;

public class BalloonEntity extends FallingBlockEntity {
	private Vec3d targetPos;
	private double speed = 0.05;

	public BalloonEntity(World world, BlockPos pos, BlockState block) {
		super(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getX() + 0.5, block);
		this.setNoGravity(true);
	}

	@Override
	public void tick() {
		this.timeFalling = 1;

		if (this.world.isClient()) {
			return;
		}

		ManagedGameSpace game = ManagedGameSpace.forWorld(this.world);
		if (game == null) {
			this.remove();
			return;
		}
		if(hasTarget()) {
			Vec3d vecToTarget = this.getPos().reverseSubtract(this.targetPos);
			double distance = vecToTarget.length();
			Vec3d posOffset = vecToTarget.multiply(speed / distance);
			if(posOffset.length() < distance) {
				this.setPos(this.getX() + posOffset.x, this.getY() + posOffset.y, this.getZ() + posOffset.z);
			}
			else {
				this.setPos(targetPos.x, targetPos.y, targetPos.z);
				removeTarget();
			}
			TowerDefense.LOGGER.info(this.getPos().toString());
			this.move(MovementType.SELF, this.getVelocity());
		}
	}

	public boolean hasTarget() {
		return this.targetPos != null;
	}

	public void setTarget(BlockPos targetPos) {
		this.targetPos = new Vec3d(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
	}

	public void removeTarget() {
		this.targetPos = null;
	}

	/**
	 * @param speed in blocks per second
	 */
	public void setSpeed(double speed) {
		this.speed = speed / 20.0D;
	}
}
