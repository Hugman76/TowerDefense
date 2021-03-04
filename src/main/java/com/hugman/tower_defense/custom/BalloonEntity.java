package com.hugman.tower_defense.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import xyz.nucleoid.plasmid.game.ManagedGameSpace;

public class BalloonEntity extends FallingBlockEntity {
	public BalloonEntity(World world, BlockPos pos, BlockState block) {
		super(world, pos.getX(), pos.getY(), pos.getX(), block);
		this.setNoGravity(true);
	}

	@Override
	public void tick() {
		this.timeFalling = 1;
		super.tick();

		if (this.world.isClient()) {
			return;
		}

		ManagedGameSpace game = ManagedGameSpace.forWorld(this.world);
		if (game == null) {
			this.remove();
			return;
		}
	}
}
