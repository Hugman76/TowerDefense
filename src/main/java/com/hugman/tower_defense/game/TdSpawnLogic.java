package com.hugman.tower_defense.game;

import com.hugman.tower_defense.TowerDefense;
import com.hugman.tower_defense.game.active.TdActive;
import com.hugman.tower_defense.game.map.TdMap;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.chunk.ChunkStatus;
import xyz.nucleoid.plasmid.game.GameSpace;

public class TdSpawnLogic {
	private final ServerWorld world;
	private final TdMap map;

	public TdSpawnLogic(ServerWorld world, TdMap map) {
		this.world = world;
		this.map = map;
	}

	public void spawnPlayer(ServerPlayerEntity player, TdMap.TeamSpawn spawn) {
		resetPlayer(player, GameMode.ADVENTURE);
		spawn.placePlayer(player, this.world);
	}

	public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
		player.clearStatusEffects();
		player.setHealth(20.0F);
		player.getHungerManager().setFoodLevel(20);
		player.setVelocity(Vec3d.ZERO);
		player.fallDistance = 0.0f;
		player.setFireTicks(0);
		player.setGameMode(gameMode);
		if(gameMode == GameMode.SURVIVAL) {
			player.abilities.allowFlying = true;
			player.abilities.flying = true;
			player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.abilities));
		}
	}

	public void spawnAtCenter(ServerPlayerEntity player) {
		BlockPos pos = this.map.getCenterSpawn();

		ChunkPos chunkPos = new ChunkPos(pos);
		this.world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, player.getEntityId());
		this.world.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL);

		player.teleport(this.world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.0F, 0.0F);
		player.networkHandler.syncWithPlayerPosition();
	}
}
