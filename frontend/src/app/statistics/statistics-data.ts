import { PlayerStatisticInput } from '../core/statistics.models';

export interface StatisticsSheet {
  matchId: string;
  setId: string;
  setNumber: number;
  players: PlayerStatisticInput[];
  error: boolean;
}

export const totalFields = [
  'points', 'balance', 'serveTotal', 'serveError', 'serveAces', 'opponentReceivedServeCount',
  'receptionTotal', 'receptionError', 'receptionNegativeCount', 'receptionNeutralCount',
  'receptionPositiveCount', 'receptionPerfectCount', 'attackTotal', 'attackError',
  'attackBlocked', 'attackPoints', 'opponentDefendedAttackCount', 'blockPoints',
  'blockTouches', 'defense', 'assists'
] as const;

export interface StatisticsSummary {
  totals: Record<typeof totalFields[number], number>;
  attackSuccess: number | null;
  attackEfficiency: number | null;
  receptionPositive: number | null;
  receptionPerfect: number | null;
  serveEfficiency: number | null;
}

const detailedFields = new Set<string>([
  'opponentReceivedServeCount', 'receptionNegativeCount', 'receptionNeutralCount',
  'receptionPositiveCount', 'receptionPerfectCount', 'opponentDefendedAttackCount'
]);

// Older sheets lack detailed action counts. Keep their stored totals and rates.
export function parseStatisticsPlayers(value: unknown): PlayerStatisticInput[] {
  if (!Array.isArray(value)) throw new Error('Nieprawidłowy format arkusza statystyk');
  const ids = new Set<string>();
  return value.map((row) => {
    if (!row || typeof row.playerExternalId !== 'string' || ids.has(row.playerExternalId)) {
      throw new Error('Nieprawidłowy zawodnik w arkuszu');
    }
    ids.add(row.playerExternalId);
    const normalized = { ...row };
    for (const field of [...totalFields, 'receptionPositivePercent', 'receptionPerfectPercent']) {
      if (detailedFields.has(field) && normalized[field] == null) normalized[field] = 0;
      if (typeof normalized[field] !== 'number' || !Number.isFinite(normalized[field])) {
        throw new Error('Nieprawidłowa wartość statystyki');
      }
    }
    return normalized as PlayerStatisticInput;
  });
}

export function percentage(value: number, total: number): number | null {
  return total ? Math.round(value / total * 1000) / 10 : null;
}

export function summarizeStatistics(rows: PlayerStatisticInput[]): StatisticsSummary {
  const totals = Object.fromEntries(totalFields.map((field) => [field, rows.reduce((sum, row) => sum + row[field], 0)])) as StatisticsSummary['totals'];
  const receptionCount = (row: PlayerStatisticInput, perfect: boolean): number => {
    const detailedTotal = row.receptionError + row.receptionNegativeCount + row.receptionNeutralCount
      + row.receptionPositiveCount + row.receptionPerfectCount;
    if (detailedTotal === row.receptionTotal) {
      return perfect ? row.receptionPerfectCount : row.receptionPositiveCount + row.receptionPerfectCount;
    }
    return row.receptionTotal * (perfect ? row.receptionPerfectPercent : row.receptionPositivePercent) / 100;
  };
  return {
    totals,
    attackSuccess: percentage(totals.attackPoints, totals.attackTotal),
    attackEfficiency: percentage(totals.attackPoints - totals.attackError - totals.attackBlocked, totals.attackTotal),
    receptionPositive: percentage(rows.reduce((sum, row) => sum + receptionCount(row, false), 0), totals.receptionTotal),
    receptionPerfect: percentage(rows.reduce((sum, row) => sum + receptionCount(row, true), 0), totals.receptionTotal),
    serveEfficiency: percentage(totals.serveAces - totals.serveError, totals.serveTotal)
  };
}

export function selectStatisticsSheets(sheets: StatisticsSheet[], matchId: string, playerId: string): StatisticsSheet[] {
  return sheets.filter((sheet) => !matchId || sheet.matchId === matchId).map((sheet) => ({
    ...sheet,
    players: sheet.players.filter((row) => !playerId || row.playerExternalId === playerId)
  }));
}
