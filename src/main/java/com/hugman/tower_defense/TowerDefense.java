package com.hugman.tower_defense;

import com.hugman.tower_defense.custom.BalloonRegistry;
import com.hugman.tower_defense.game.TdConfig;
import com.hugman.tower_defense.game.TdWaiting;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.nucleoid.plasmid.game.GameType;

public class TowerDefense implements ModInitializer {

	public static final String ID = "tower_defense";
	public static final Logger LOGGER = LogManager.getLogger(ID);

	public static final GameType<TdConfig> TYPE = GameType.register(
			new Identifier(ID, "survival"),
			TdWaiting::open,
			TdConfig.CODEC
	);

	public static Identifier id(String value) {
		return new Identifier(ID, value);
	}

	@Override
	public void onInitialize() {
		BalloonRegistry.init();
	}
}
