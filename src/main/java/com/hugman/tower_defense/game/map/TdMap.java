package com.hugman.tower_defense.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateMetadata;
import xyz.nucleoid.plasmid.map.template.TemplateChunkGenerator;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.util.HashMap;
import java.util.Map;

public class TdMap {
	private final MapTemplate template;
	private final TdMapConfig config;
	private final Map<GameTeam, TeamSpawn> teamSpawns = new HashMap<>();
	private final Map<GameTeam, TeamRegions> teamRegions = new HashMap<>();
	private BlockPos centerSpawn = BlockPos.ORIGIN;

	public TdMap(MapTemplate template, TdMapConfig config) {
		this.template = template;
		this.config = config;
	}

	public ChunkGenerator asGenerator(MinecraftServer server) {
		return new TemplateChunkGenerator(server, this.template);
	}

	@Nullable
	public TeamSpawn getTeamSpawn(GameTeam team) {
		return this.teamSpawns.get(team);
	}

	public BlockPos getCenterSpawn() {
		return this.centerSpawn;
	}

	public void setCenterSpawn(BlockPos centerSpawn) {
		this.centerSpawn = centerSpawn;
	}

	public static class TeamSpawn {
		private final BlockBounds region;

		TeamSpawn(BlockBounds region) {
			this.region = region;
		}

		public void placePlayer(ServerPlayerEntity player, ServerWorld world) {
			player.fallDistance = 0.0F;

			Vec3d center = this.region.getCenter();
			player.teleport(world, center.x, center.y + 0.5, center.z, 0.0F, 0.0F);
		}
	}

	public static class TeamRegions {
		public static final TeamRegions EMPTY = new TeamRegions(null, null, null, null);

		public final BlockBounds base;
		public final BlockBounds spawn;
		public final BlockBounds balloonSpawn;
		public final BlockBounds balloonEnd;

		public TeamRegions(BlockBounds base, BlockBounds spawn, BlockBounds balloonSpawn, BlockBounds balloonEnd) {
			this.spawn = spawn;
			this.base = base;
			this.balloonSpawn = balloonSpawn;
			this.balloonEnd = balloonEnd;
		}

		public static TeamRegions fromTemplate(GameTeam team, MapTemplateMetadata metadata) {
			String teamKey = team.getKey();

			BlockBounds base = metadata.getFirstRegionBounds(teamKey + "_base");
			BlockBounds spawn = metadata.getFirstRegionBounds(teamKey + "_spawn");
			BlockBounds balloonSpawn = metadata.getFirstRegionBounds(teamKey + "_balloon_spawn");
			BlockBounds balloonEnd = metadata.getFirstRegionBounds(teamKey + "_balloon_end");

			return new TeamRegions(base, spawn, balloonSpawn, balloonEnd);
		}
	}
}
