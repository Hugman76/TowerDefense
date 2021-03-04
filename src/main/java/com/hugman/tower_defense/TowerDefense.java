package com.hugman.tower_defense;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.hugman.tower_defense.game.TowerDefenseConfig;
import com.hugman.tower_defense.game.TowerDefenseWaiting;

public class TowerDefense implements ModInitializer {

    public static final String ID = "tower_defense";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<TowerDefenseConfig> TYPE = GameType.register(
            new Identifier(ID, "survival"),
            TowerDefenseWaiting::open,
            TowerDefenseConfig.CODEC
    );

    @Override
    public void onInitialize() {}
}
