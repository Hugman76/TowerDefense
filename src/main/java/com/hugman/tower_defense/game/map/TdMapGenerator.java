package com.hugman.tower_defense.game.map;

import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.map.template.MapTemplate;

public class TdMapGenerator {

	private final TdMapConfig config;

	public TdMapGenerator(TdMapConfig config) {
		this.config = config;
	}

	public TdMap build() {
		MapTemplate template = MapTemplate.createEmpty();
		TdMap map = new TdMap(template, this.config);

		this.buildSpawn(template);
		map.setCenterSpawn(new BlockPos(0, 65, 0));

		return map;
	}

	private void buildSpawn(MapTemplate builder) {
		BlockPos min = new BlockPos(-5, 64, -5);
		BlockPos max = new BlockPos(5, 64, 5);

		for(BlockPos pos : BlockPos.iterate(min, max)) {
			builder.setBlockState(pos, this.config.spawnBlock);
		}
	}
}
