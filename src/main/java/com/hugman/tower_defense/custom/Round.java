package com.hugman.tower_defense.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class Round {
	public static final Codec<Round> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BalloonSpawn.CODEC.listOf().fieldOf("spawns").forGetter(config -> config.balloons)
	).apply(instance, Round::new));

	public final List<BalloonSpawn> balloons;

	public Round(List<BalloonSpawn> balloons) {
		this.balloons = balloons;
	}

	public long getLength() {
		long length = 0;
		for(BalloonSpawn spawn : balloons) {
			length = Math.max(spawn.spawnTime, length);
		}
		return length;
	}
}
