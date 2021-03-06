package com.hugman.tower_defense.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Balloon {
	public static final Codec<Balloon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockState.CODEC.fieldOf("block_state").forGetter(balloon -> balloon.blockState),
			Codec.DOUBLE.fieldOf("speed").forGetter(balloon -> balloon.speed)
	).apply(instance, Balloon::new));

	public final BlockState blockState;
	public final double speed;

	public Balloon(BlockState blockState, double speed) {
		this.blockState = blockState;
		this.speed = speed;
	}

	public BalloonEntity spawn(World world, BlockPos pos) {
		BalloonEntity entity = new BalloonEntity(world, pos, this);
		world.spawnEntity(entity);
		return entity;
	}
}
