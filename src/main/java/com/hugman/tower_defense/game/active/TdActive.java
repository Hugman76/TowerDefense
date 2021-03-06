package com.hugman.tower_defense.game.active;

import com.google.common.collect.Multimap;
import com.hugman.tower_defense.game.*;
import com.hugman.tower_defense.game.map.TdMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.JoinResult;
import xyz.nucleoid.plasmid.game.player.MutablePlayerSet;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRule;
import xyz.nucleoid.plasmid.game.rule.RuleResult;
import xyz.nucleoid.plasmid.util.PlayerRef;
import xyz.nucleoid.plasmid.widget.GlobalWidgets;

import java.util.Map;
import java.util.stream.Stream;

public class TdActive {
	public static final long CLOSE_TICKS = 10 * 20;

	public final ServerWorld world;
	public final GameSpace gameSpace;

	public final TdConfig config;
	public final TdMap map;

	private final Object2ObjectMap<PlayerRef, TdParticipant> participants = new Object2ObjectOpenHashMap<>();
	private final Map<GameTeam, TeamState> teams = new Reference2ObjectOpenHashMap<>();

	public final TdStageManager stageManager;
	public final TdTeamLogic teamLogic;
	public final TdWinStateLogic winStateLogic;
	public final TdSpawnLogic spawnLogic;
	public final TdBar bar;

	private boolean opened;

	public long startTime;

	private long closeTime;

	private TdActive(GameSpace gameSpace, TdMap map, TdConfig config, GlobalWidgets widgets) {
		this.world = gameSpace.getWorld();
		this.gameSpace = gameSpace;

		this.map = map;
		this.config = config;

		this.stageManager = new TdStageManager(this);
		this.teamLogic = new TdTeamLogic(this);
		this.winStateLogic = new TdWinStateLogic(this);
		this.spawnLogic = new TdSpawnLogic(world, map);
		this.bar = new TdBar(widgets);
	}

	public static void open(GameSpace gameSpace, TdMap map, TdConfig config, Multimap<GameTeam, ServerPlayerEntity> players) {
		gameSpace.openGame(game -> {
			GlobalWidgets widgets = new GlobalWidgets(game);

			TdActive active = new TdActive(gameSpace, map, config, widgets);
			active.addPlayers(players);

			game.setRule(GameRule.CRAFTING, RuleResult.DENY);
			game.setRule(GameRule.PORTALS, RuleResult.DENY);
			game.setRule(GameRule.PVP, RuleResult.DENY);
			game.setRule(GameRule.HUNGER, RuleResult.DENY);
			game.setRule(GameRule.FALL_DAMAGE, RuleResult.DENY);
			game.setRule(GameRule.INTERACTION, RuleResult.DENY);
			game.setRule(GameRule.BLOCK_DROPS, RuleResult.DENY);
			game.setRule(GameRule.THROW_ITEMS, RuleResult.DENY);
			game.setRule(GameRule.UNSTABLE_TNT, RuleResult.DENY);

			game.on(GameOpenListener.EVENT, active::onOpen);

			game.on(OfferPlayerListener.EVENT, player -> JoinResult.ok());
			game.on(PlayerAddListener.EVENT, active::addPlayer);

			game.on(GameTickListener.EVENT, active::tick);

			game.on(PlayerDamageListener.EVENT, active::onPlayerDamage);
			game.on(PlayerDeathListener.EVENT, active::onPlayerDeath);
		});
	}

	private void onOpen() {
		this.stageManager.onOpen();

		this.startTime = this.world.getTime();
		this.opened = true;
	}

	private void addPlayer(ServerPlayerEntity player) {
		if (this.opened && this.isParticipant(player)) {
			this.rejoinPlayer(player);
		} else {
			this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
			this.spawnLogic.spawnAtCenter(player);
		}
	}

	private void rejoinPlayer(ServerPlayerEntity player) {
		TdParticipant participant = this.getParticipant(player);

		if (participant != null) {
			TdMap.TeamSpawn spawn = this.teamLogic.tryRespawn(participant);
			if (spawn != null) {
				this.spawnLogic.spawnPlayer(player, spawn);
			} else {
				this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
				this.spawnLogic.spawnAtCenter(player);
			}
		}
	}

	private void addPlayers(Multimap<GameTeam, ServerPlayerEntity> players) {
		MinecraftServer server = this.gameSpace.getServer();

		players.forEach((team, player) -> {
			TdParticipant participant = new TdParticipant(this, player, team);
			this.participants.put(participant.ref, participant);

			TeamState teamState = this.teams.computeIfAbsent(team, t -> new TeamState(server, t));
			teamState.players.add(player);
		});
	}

	private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
		this.spawnLogic.resetPlayer(player, GameMode.SURVIVAL);
		this.spawnLogic.spawnAtCenter(player);
		return ActionResult.FAIL;
	}

	private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
		this.spawnLogic.resetPlayer(player, GameMode.SURVIVAL);
		this.spawnLogic.spawnAtCenter(player);
		return ActionResult.FAIL;
	}

	private void tick() {
		ServerWorld world = this.gameSpace.getWorld();
		long time = world.getTime();

		TdStageManager.IdleTickResult result = this.stageManager.tick();

		switch (result) {
			case CONTINUE_TICK:
				break;
			case TICK_FINISHED:
				return;
			case GAME_FINISHED:
				this.winStateLogic.broadcastWin(this.winStateLogic.checkWinResult());
				return;
			case GAME_ENDED:
				this.gameSpace.close(GameCloseReason.FINISHED);
				return;
		}

		// TODO tick logic
	}

	@Nullable
	public TdParticipant getParticipant(PlayerEntity player) {
		return this.participants.get(PlayerRef.of(player));
	}

	@Nullable
	public TdParticipant getParticipant(PlayerRef player) {
		return this.participants.get(player);
	}

	@Nullable
	public GameTeam getTeam(PlayerRef player) {
		TdParticipant participant = this.participants.get(player);
		if (participant != null) {
			return participant.team;
		}
		return null;
	}

	public boolean isParticipant(PlayerEntity player) {
		return this.participants.containsKey(PlayerRef.of(player));
	}

	public Stream<TdParticipant> participantsFor(GameTeam team) {
		return this.participants.values().stream().filter(participant -> participant.team == team);
	}

	public PlayerSet playersFor(GameTeam team) {
		TeamState teamState = this.teams.get(team);
		return teamState != null ? teamState.players : PlayerSet.EMPTY;
	}

	public PlayerSet players() {
		return this.gameSpace.getPlayers();
	}

	public Stream<TdParticipant> participants() {
		return this.participants.values().stream();
	}

	public Stream<TeamState> teams() {
		return this.teams.values().stream();
	}

	public int getTeamCount() {
		return this.teams.size();
	}

	@Nullable
	public TeamState getTeam(GameTeam team) {
		return this.teams.get(team);
	}

	public static class TeamState {
		final MutablePlayerSet players;
		final GameTeam team;
		boolean eliminated;

		TeamState(MinecraftServer server, GameTeam team) {
			this.players = new MutablePlayerSet(server);
			this.team = team;
		}
	}
}
