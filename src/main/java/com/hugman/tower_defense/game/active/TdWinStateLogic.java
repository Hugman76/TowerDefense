package com.hugman.tower_defense.game.active;

import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.plasmid.game.player.GameTeam;
import xyz.nucleoid.plasmid.game.player.PlayerSet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TdWinStateLogic {
	private final TdActive game;

	public TdWinStateLogic(TdActive game) {
		this.game = game;
	}

	@Nullable
	public WinResult checkWinResult() {
		this.checkEliminatedTeams();

		// if there's only one team, disable the win state
		if (this.game.getTeamCount() <= 1) {
			return null;
		}

		List<TdActive.TeamState> remainingTeams = this.game.teams()
				.filter(team -> !team.eliminated)
				.collect(Collectors.toList());

		if (remainingTeams.size() <= 1) {
			if (remainingTeams.size() == 1) {
				TdActive.TeamState winningTeam = remainingTeams.get(0);
				return WinResult.team(winningTeam.team);
			} else {
				return WinResult.draw();
			}
		}

		return null;
	}

	private void checkEliminatedTeams() {
		Stream<TdActive.TeamState> eliminatedTeams = this.game.teams()
				.filter(team -> !team.eliminated)
				.filter(team -> {
					long remainingCount = this.countRemainingPlayers(team.team);
					return remainingCount <= 0;
				});
		eliminatedTeams.forEach(this::eliminateTeam);
	}

	private long countRemainingPlayers(GameTeam team) {
		return this.game.participantsFor(team)
				.filter(TdParticipant::isPlaying)
				.count();
	}

	private void eliminateTeam(TdActive.TeamState teamState) {
		teamState.eliminated = true;

		this.game.participantsFor(teamState.team).forEach(participant -> {
			participant.eliminated = true;
		});

		broadcastTeamEliminated(teamState.team);
	}

	public void broadcastTeamEliminated(GameTeam team) {
		this.game.playersFor(team).sendMessage(
				new TranslatableText("text.tower_defense.team_eliminated", new LiteralText(team.getDisplay()).formatted(team.getFormatting())).formatted(Formatting.BOLD)
		);
	}

	public void broadcastWin(TdWinStateLogic.WinResult result) {
		GameTeam winningTeam = result.getTeam();
		if (winningTeam != null) {
			this.game.players().sendMessage(
					new TranslatableText("text.bedwars.team_win", winningTeam.getDisplay()).formatted(winningTeam.getFormatting(), Formatting.BOLD)
			);
		} else {
			this.game.players().sendMessage(new TranslatableText("text.bedwars.draw").formatted(Formatting.BOLD));
		}
	}

	public static class WinResult {
		private final GameTeam team;

		private WinResult(GameTeam team) {
			this.team = team;
		}

		public static WinResult team(GameTeam team) {
			return new WinResult(team);
		}

		public static WinResult draw() {
			return new WinResult(null);
		}

		@Nullable
		public GameTeam getTeam() {
			return this.team;
		}

		public boolean isDraw() {
			return this.team == null;
		}
	}
}
