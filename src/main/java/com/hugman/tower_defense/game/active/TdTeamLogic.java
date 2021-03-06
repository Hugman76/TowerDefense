package com.hugman.tower_defense.game.active;

import com.hugman.tower_defense.game.map.TdMap;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;

public class TdTeamLogic {
	private final TdActive game;

	TdTeamLogic(TdActive game) {
		this.game = game;
	}

	public void tick() {
		ServerWorld world = this.game.world;
		// TODO: moving balloons
	}

	@Nullable
	public TdMap.TeamSpawn tryRespawn(TdParticipant participant) {
		TdActive.TeamState teamState = this.game.getTeam(participant.team);
		if (teamState != null && !teamState.eliminated) {
			return this.game.map.getTeamSpawn(participant.team);
		}

		return null;
	}
}
