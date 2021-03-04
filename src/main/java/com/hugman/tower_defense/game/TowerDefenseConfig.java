package com.hugman.tower_defense.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import com.hugman.tower_defense.game.map.TowerDefenseMapConfig;

public class TowerDefenseConfig {
    public static final Codec<TowerDefenseConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            TowerDefenseMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit").forGetter(config -> config.timeLimit)
    ).apply(instance, TowerDefenseConfig::new));

    public final PlayerConfig playerConfig;
    public final TowerDefenseMapConfig mapConfig;
    public final int timeLimit;

    public TowerDefenseConfig(PlayerConfig players, TowerDefenseMapConfig mapConfig, int timeLimit) {
        this.playerConfig = players;
        this.mapConfig = mapConfig;
        this.timeLimit = timeLimit;
    }
}
