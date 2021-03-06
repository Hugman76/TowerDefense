package com.hugman.tower_defense.game.active;

import com.hugman.tower_defense.TowerDefense;
import com.hugman.tower_defense.custom.*;
import com.hugman.tower_defense.game.map.TdMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class TdStageManager {
	private final TdActive game;
	public boolean isPaused;
	public int round;
	public long stageSwapTime = -1;
	public long stageEndTime = -1;
	private long finishTime = -1;
	private boolean setSpectator = false;

	public TdStageManager(TdActive game) {
		this.game = game;
	}

	public void onOpen() {
		this.game.participants().forEach(participant -> {
			ServerPlayerEntity player = participant.player();
			if (player == null) {
				return;
			}

			this.game.spawnLogic.resetPlayer(player, GameMode.SURVIVAL);

			TdMap.TeamSpawn spawn = this.game.teamLogic.tryRespawn(participant);
			if (spawn != null) {
				this.game.spawnLogic.spawnPlayer(player, spawn);
			} else {
				TowerDefense.LOGGER.warn("No spawn for player {}", participant.ref);
				this.game.spawnLogic.spawnAtCenter(player);
			}
		});

		// First stage is 30 seconds long
		this.stageEndTime = (30 * 20L);
		this.stageSwapTime = this.game.startTime + stageEndTime;
		this.isPaused = true;
		this.round = 1;

		Balloon redBalloon = BalloonRegistry.get(TowerDefense.id("red"));
		if(redBalloon != null) {
			BalloonEntity entity = redBalloon.spawn(this.game.world, new BlockPos(0, 68, 0));
			entity.setTarget(new BlockPos(5, 68, 5));
		}
	}

	public IdleTickResult tick() {
		long time = this.game.world.getTime();
		// Game has finished. Wait a few seconds before finally closing the game.
		if(this.finishTime > 0) {
			if(time >= this.finishTime) {
				return IdleTickResult.GAME_FINISHED;
			}
			return IdleTickResult.TICK_FINISHED;
		}

		// Stage has just ended, going to the next phase.
		if(time > this.stageSwapTime) {
			this.round++;
			this.isPaused = !this.isPaused;
			if(!this.isPaused) {
				Round round = RoundRegistry.get(this.round);
				if(round != null) {
					this.stageEndTime = round.getLength();
				}
			}
			this.stageEndTime = (20 * 20L);
			this.stageSwapTime = time + stageEndTime;
		}

		this.game.bar.update(stageSwapTime - time, stageEndTime);

		// Game has just ended. Transition to the waiting-before-close state.
		if(this.game.winStateLogic.checkWinResult() != null) {
			if(!this.setSpectator) {
				this.setSpectator = true;
				for(ServerPlayerEntity player : this.game.gameSpace.getPlayers()) {
					player.setGameMode(GameMode.SPECTATOR);
				}
			}

			this.finishTime = time + (5 * 20L);

			return IdleTickResult.GAME_ENDED;
		}

		return IdleTickResult.CONTINUE_TICK;

	}

	public enum IdleTickResult {
		CONTINUE_TICK,
		TICK_FINISHED,
		GAME_ENDED,
		GAME_FINISHED,
	}
}
