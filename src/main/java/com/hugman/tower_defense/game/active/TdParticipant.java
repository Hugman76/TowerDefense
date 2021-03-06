package com.hugman.tower_defense.game.active;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.util.PlayerRef;

public class TdParticipant {
	private final ServerWorld world;
	public final PlayerRef ref;
	public final GameTeam team;

	public boolean eliminated;

	public TdParticipant(TdActive game, ServerPlayerEntity player, GameTeam team) {
		this.world = player.getServerWorld();
		this.ref = PlayerRef.of(player);
		this.team = team;
	}

	@Nullable
	public ServerPlayerEntity player() {
		return this.ref.getEntity(this.world);
	}

	public boolean isPlaying() {
		return !this.eliminated && this.isOnline();
	}

	public boolean isOnline() {
		return this.ref.isOnline(this.world);
	}
}
