package com.hugman.tower_defense.game;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hugman.tower_defense.game.active.TdActive;
import com.hugman.tower_defense.game.map.TdMap;
import com.hugman.tower_defense.game.map.TdMapGenerator;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import xyz.nucleoid.fantasy.BubbleWorldConfig;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.event.PlayerAddListener;
import xyz.nucleoid.plasmid.game.event.PlayerDamageListener;
import xyz.nucleoid.plasmid.game.event.PlayerDeathListener;
import xyz.nucleoid.plasmid.game.event.RequestStartListener;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;

public class TdWaiting {
	private final GameSpace gameSpace;
	private final TdMap map;
	private final TdConfig config;

	private final TeamSelectionLobby teamSelection;

	private final TdSpawnLogic spawnLogic;

	private TdWaiting(GameSpace gameSpace, TdMap map, TdConfig config, TeamSelectionLobby teamSelection) {
		this.gameSpace = gameSpace;
		this.map = map;
		this.config = config;

		this.teamSelection = teamSelection;

		this.spawnLogic = new TdSpawnLogic(gameSpace.getWorld(), map);
	}

	public static GameOpenProcedure open(GameOpenContext<TdConfig> context) {
		TdConfig config = context.getConfig();
		TdMapGenerator generator = new TdMapGenerator(config.mapConfig);
		TdMap map = generator.build();

		BubbleWorldConfig worldConfig = new BubbleWorldConfig()
				.setGenerator(map.asGenerator(context.getServer()))
				.setDefaultGameMode(GameMode.SPECTATOR);

		return context.createOpenProcedure(worldConfig, game -> {
			GameWaitingLobby.applyTo(game, config.playerConfig);

			TeamSelectionLobby teamSelection = TeamSelectionLobby.applyTo(game, config.teams);
			TdWaiting waiting = new TdWaiting(game.getSpace(), map, config, teamSelection);

			game.setRule(GameRule.INTERACTION, RuleResult.ALLOW);

			game.on(RequestStartListener.EVENT, waiting::requestStart);

			game.on(PlayerAddListener.EVENT, waiting::addPlayer);
			game.on(PlayerDeathListener.EVENT, waiting::onPlayerDeath);
			game.on(PlayerDamageListener.EVENT, waiting::onPlayerDamage);
		});
	}

	private StartResult requestStart() {
		Multimap<GameTeam, ServerPlayerEntity> players = HashMultimap.create();
		this.teamSelection.allocate(players::put);

		TdActive.open(this.gameSpace, this.map, this.config, players);

		return StartResult.OK;
	}

	private void addPlayer(ServerPlayerEntity player) {
		this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
		this.spawnLogic.spawnAtCenter(player);
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
		this.spawnLogic.spawnAtCenter(player);
		return ActionResult.FAIL;
	}
}
