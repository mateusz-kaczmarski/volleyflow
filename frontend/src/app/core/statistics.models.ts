export interface PlayerStatistic {
  id: string;
  playerExternalId: string;
  position: string;
  player: string;
  points: number;
  balance: number;
  serveTotal: number;
  serveError: number;
  serveAces: number;
  opponentReceivedServeCount: number;
  receptionTotal: number;
  receptionError: number;
  receptionNegativeCount: number;
  receptionNeutralCount: number;
  receptionPositiveCount: number;
  receptionPerfectCount: number;
  receptionPositivePercent: number;
  receptionPerfectPercent: number;
  attackTotal: number;
  attackError: number;
  attackBlocked: number;
  attackPoints: number;
  opponentDefendedAttackCount: number;
  blockPoints: number;
  blockTouches: number;
  defense: number;
  assists: number;
}

// Names and positions come from the club roster; only the stable profile ID and
// entered values are exchanged with the statistics API.
export type PlayerStatisticInput = Omit<PlayerStatistic, 'id' | 'position' | 'player'>;
export interface SetStatistics { players: PlayerStatisticInput[]; }

export interface PlayerCareerStatisticsResponse {
  matchesPlayed: number;
  setsPlayed: number;
  points: number;
  attack: { total: number; points: number; successPercent: number; efficiencyPercent: number };
  reception: { positivePercent: number; perfectPercent: number };
  serve: { aces: number; efficiencyPercent: number };
  block: { points: number; touches: number };
  defense: number;
  assists: number;
  matches: PlayerMatchStatisticsResponse[];
}

export interface PlayerMatchStatisticsResponse {
  matchExternalId: string;
  scheduledAt: string;
  opponentName: string;
  result: string;
  setsPlayed: number;
  points: number;
  attackSuccessPercent: number;
  attackEfficiencyPercent: number;
  receptionPositivePercent: number;
  receptionPerfectPercent: number;
  aces: number;
  serveEfficiencyPercent: number;
  blockPoints: number;
  blockTouches: number;
  defense: number;
}
