package com.hugman.tower_defense.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;

public class TdMapConfig {
	public static final Codec<TdMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockState.CODEC.fieldOf("spawn_block").forGetter(map -> map.spawnBlock)
	).apply(instance, TdMapConfig::new));

	public final BlockState spawnBlock;

	public TdMapConfig(BlockState spawnBlock) {
		this.spawnBlock = spawnBlock;
	}
}
