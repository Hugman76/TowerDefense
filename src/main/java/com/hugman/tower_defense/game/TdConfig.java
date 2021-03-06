package com.hugman.tower_defense.game;

import com.hugman.tower_defense.game.map.TdMapConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;
import xyz.nucleoid.plasmid.game.player.GameTeam;

import java.util.List;

public class TdConfig {
	public static final Codec<TdConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			TdMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
			PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
			GameTeam.CODEC.listOf().fieldOf("teams").forGetter(config -> config.teams)
	).apply(instance, TdConfig::new));

	public final PlayerConfig playerConfig;
	public final TdMapConfig mapConfig;
	public final List<GameTeam> teams;

	public TdConfig(TdMapConfig mapConfig, PlayerConfig players, List<GameTeam> teams) {
		this.playerConfig = players;
		this.mapConfig = mapConfig;
		this.teams = teams;
	}
}
