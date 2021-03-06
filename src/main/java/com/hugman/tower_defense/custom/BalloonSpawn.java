package com.hugman.tower_defense.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public class BalloonSpawn {
	public static final Codec<BalloonSpawn> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.LONG.fieldOf("spawn_time").forGetter(config -> config.spawnTime),
			Identifier.CODEC.fieldOf("name").forGetter(config -> BalloonRegistry.getId(config.balloon))
	).apply(instance, BalloonSpawn::new));

	public final long spawnTime;
	public final Balloon balloon;

	public BalloonSpawn(long spawnTime, Identifier balloon) {
		this.spawnTime = spawnTime;
		this.balloon = BalloonRegistry.get(balloon);
	}
}
