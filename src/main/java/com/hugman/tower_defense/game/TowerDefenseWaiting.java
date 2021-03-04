package com.hugman.tower_defense.game;

import com.hugman.tower_defense.custom.BalloonEntity;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.event.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import com.hugman.tower_defense.game.map.TowerDefenseMap;
import com.hugman.tower_defense.game.map.TowerDefenseMapGenerator;
import xyz.nucleoid.fantasy.BubbleWorldConfig;

public class TowerDefenseWaiting {
    private final GameSpace gameSpace;
    private final TowerDefenseMap map;
    private final TowerDefenseConfig config;
    private final TowerDefenseSpawnLogic spawnLogic;

    private TowerDefenseWaiting(GameSpace gameSpace, TowerDefenseMap map, TowerDefenseConfig config) {
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new TowerDefenseSpawnLogic(gameSpace, map);
    }

    public static GameOpenProcedure open(GameOpenContext<TowerDefenseConfig> context) {
        TowerDefenseConfig config = context.getConfig();
        TowerDefenseMapGenerator generator = new TowerDefenseMapGenerator(config.mapConfig);
        TowerDefenseMap map = generator.build();

        BubbleWorldConfig worldConfig = new BubbleWorldConfig()
                .setGenerator(map.asGenerator(context.getServer()))
                .setDefaultGameMode(GameMode.SPECTATOR);

        return context.createOpenProcedure(worldConfig, game -> {
            TowerDefenseWaiting waiting = new TowerDefenseWaiting(game.getSpace(), map, context.getConfig());

            GameWaitingLobby.applyTo(game, config.playerConfig);

            game.on(RequestStartListener.EVENT, waiting::requestStart);
            game.on(PlayerAddListener.EVENT, waiting::addPlayer);
            game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
        });
    }

    private StartResult requestStart() {
        TowerDefenseActive.open(this.gameSpace, this.map, this.config);
        return StartResult.OK;
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }
}
