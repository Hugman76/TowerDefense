package com.hugman.tower_defense.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class TowerDefenseMapConfig {
    public static final Codec<TowerDefenseMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("spawn_block").forGetter(map -> map.spawnBlock)
    ).apply(instance, TowerDefenseMapConfig::new));

    public final BlockState spawnBlock;

    public TowerDefenseMapConfig(BlockState spawnBlock) {
        this.spawnBlock = spawnBlock;
    }
}
