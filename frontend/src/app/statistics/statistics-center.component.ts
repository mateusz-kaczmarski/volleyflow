import { ChangeDetectionStrategy, Component, ElementRef, ViewChild, computed, effect, inject, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { catchError, forkJoin, map, of } from 'rxjs';
import { ApiService } from '../core/api.service';
import { Club, Match, Membership } from '../core/models';
import { StatisticsSheet, StatisticsSummary, parseStatisticsPlayers, selectStatisticsSheets, summarizeStatistics } from './statistics-data';
import { PercentageGaugeComponent } from './percentage-gauge.component';

type TrendMetric = 'all' | 'attackSuccess' | 'attackEfficiency' | 'receptionPositive';
interface TrendPoint {
  id: string;
  label: string;
  description: string;
  summary: StatisticsSummary | null;
  incomplete: boolean;
}

@Component({
  selector: 'vf-statistics-center',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, PercentageGaugeComponent],
  templateUrl: './statistics-center.component.html',
  styleUrl: './statistics-center.component.css'
})
export class StatisticsCenterComponent {
  private readonly api = inject(ApiService);
  readonly club = input.required<Club>();
  readonly players = input<Membership[]>([]);
  readonly matches = input<Match[]>([]);
  readonly playersLoading = input(false);
  readonly playersError = input(false);
  readonly matchesLoading = input(false);
  readonly matchesError = input(false);
  readonly selectedPlayerId = signal('');
  readonly selectedMatchId = signal('');
  readonly picker = signal<'player' | 'match'>('player');
  readonly search = signal('');
  readonly loading = signal(false);
  readonly sheets = signal<StatisticsSheet[]>([]);
  readonly trendMetric = signal<TrendMetric>('all');
  readonly activePointId = signal('');
  private readonly refresh = signal(0);
  @ViewChild('filterDialog', { static: true }) private filterDialog!: ElementRef<HTMLDialogElement>;

  readonly finishedMatches = computed(() => this.matches().filter((match) => match.status === 'FINISHED')
    .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime()));
  readonly selectedMatch = computed(() => this.finishedMatches().find((match) => match.externalId === this.selectedMatchId()));
  readonly playerOptions = computed(() => {
    const options = new Map(this.players().map((player) => [player.personProfileExternalId, this.memberName(player)]));
    this.sheets().forEach((sheet) => sheet.players.forEach((player) => {
      if (!options.has(player.playerExternalId)) options.set(player.playerExternalId, `Zawodnik spoza składu · ${player.playerExternalId.slice(0, 8)}`);
    }));
    return [...options].map(([id, name]) => ({ id, name })).sort((a, b) => a.name.localeCompare(b.name, 'pl'));
  });
  readonly playerLabel = computed(() => this.playerOptions().find((player) => player.id === this.selectedPlayerId())?.name || 'Cały zespół');
  readonly matchLabel = computed(() => this.selectedMatch() ? this.opponentName(this.selectedMatch()!) : 'Wszystkie mecze');
  readonly filteredPlayers = computed(() => this.playerOptions().filter((player) => player.name.toLocaleLowerCase('pl').includes(this.search().toLocaleLowerCase('pl'))));
  readonly filteredMatches = computed(() => [...this.finishedMatches()].reverse().filter((match) =>
    `${this.opponentName(match)} ${new Date(match.scheduledAt).toLocaleDateString('pl-PL')}`.toLocaleLowerCase('pl').includes(this.search().toLocaleLowerCase('pl'))));
  readonly selectedSheets = computed(() => selectStatisticsSheets(this.sheets(), this.selectedMatchId(), this.selectedPlayerId()));
  readonly rows = computed(() => this.selectedSheets().flatMap((sheet) => sheet.players));
  readonly summary = computed(() => this.rows().length ? summarizeStatistics(this.rows()) : null);
  readonly failedSheets = computed(() => this.selectedSheets().filter((sheet) => sheet.error).length);
  readonly recordedSets = computed(() => this.selectedSheets().filter((sheet) => sheet.players.length).length);
  readonly recordedMatches = computed(() => new Set(this.selectedSheets().filter((sheet) => sheet.players.length).map((sheet) => sheet.matchId)).size);
  readonly averagePoints = computed(() => this.recordedSets() ? Math.round((this.summary()?.totals.points || 0) / this.recordedSets() * 10) / 10 : 0);
  readonly balance = computed(() => {
    const matches = this.finishedMatches().filter((match) => !this.selectedMatchId() || match.externalId === this.selectedMatchId());
    return { won: matches.filter((match) => this.outcome(match) === 'won').length, lost: matches.filter((match) => this.outcome(match) === 'lost').length };
  });
  readonly playerTable = computed(() => {
    const ids = [...new Set(this.rows().map((row) => row.playerExternalId))];
    return ids.map((id) => {
      const rows = this.rows().filter((row) => row.playerExternalId === id);
      const sets = this.selectedSheets().filter((sheet) => sheet.players.some((row) => row.playerExternalId === id)).length;
      const summary = summarizeStatistics(rows);
      return { id, name: this.playerOptions().find((player) => player.id === id)?.name || id, sets, summary,
        pointsPerSet: Math.round(summary.totals.points / sets * 10) / 10 };
    }).sort((a, b) => b.summary.totals.points - a.summary.totals.points);
  });
  readonly matchTable = computed(() => this.finishedMatches().filter((match) => !this.selectedMatchId() || match.externalId === this.selectedMatchId()).map((match) => {
    const sheets = this.selectedSheets().filter((sheet) => sheet.matchId === match.externalId);
    const rows = sheets.flatMap((sheet) => sheet.players);
    return { match, summary: rows.length ? summarizeStatistics(rows) : null, incomplete: sheets.some((sheet) => sheet.error) };
  }).filter((entry) => !this.selectedPlayerId() || entry.summary !== null).reverse());
  readonly trend = computed<TrendPoint[]>(() => {
    const match = this.selectedMatch();
    if (match) return [...(match.sets || [])].sort((a, b) => a.setNumber - b.setNumber).map((set) => {
      const sheet = this.selectedSheets().find((sheet) => sheet.setId === set.externalId);
      return { id: set.externalId, label: `Set ${set.setNumber}`, description: `${set.homePoints} : ${set.awayPoints}`,
        summary: sheet?.players.length ? summarizeStatistics(sheet.players) : null, incomplete: sheet?.error || false };
    }).filter((point) => !this.selectedPlayerId() || point.summary !== null);
    return [...this.matchTable()].reverse().map(({ match, summary, incomplete }) => ({
      id: match.externalId, label: new Date(match.scheduledAt).toLocaleDateString('pl-PL', { day: '2-digit', month: '2-digit' }),
      description: this.opponentName(match), summary, incomplete
    }));
  });
  readonly activePoint = computed(() => this.trend().find((point) => point.id === this.activePointId()));
  readonly maxPoints = computed(() => Math.max(1, ...this.trend().map((point) => point.summary?.totals.points || 0)));
  readonly axisMin = computed(() => this.trend().some((point) => (point.summary?.attackEfficiency ?? 0) < 0) ? -100 : 0);
  readonly axisTicks = computed(() => Array.from({ length: 5 }, (_, index) => 100 - index * (100 - this.axisMin()) / 4));
  readonly metrics = [
    { key: 'attackSuccess' as const, label: 'Skuteczność ataku', color: '#ef4444' },
    { key: 'attackEfficiency' as const, label: 'Efektywność ataku', color: '#f97316' },
    { key: 'receptionPositive' as const, label: 'Przyjęcie pozytywne', color: '#eab308' }
  ];

  constructor() {
    effect((onCleanup) => {
      const clubId = this.club().externalId;
      const matches = this.finishedMatches();
      this.refresh();
      this.sheets.set([]);
      this.loading.set(true);
      const jobs = matches.flatMap((match) => (match.sets || []).map((set) =>
        this.api.setStatistics(clubId, match.externalId, set.externalId).pipe(
          map((response): StatisticsSheet => ({ matchId: match.externalId, setId: set.externalId,
            setNumber: set.setNumber, players: parseStatisticsPlayers(response?.players), error: false })),
          catchError(() => of<StatisticsSheet>({ matchId: match.externalId, setId: set.externalId,
            setNumber: set.setNumber, players: [], error: true }))
        )));
      if (!jobs.length) { this.loading.set(false); return; }
      const subscription = forkJoin(jobs).subscribe((sheets) => { this.sheets.set(sheets); this.loading.set(false); });
      onCleanup(() => subscription.unsubscribe());
    });
    effect(() => {
      this.club().externalId;
      this.selectedPlayerId.set('');
      this.selectedMatchId.set('');
      this.activePointId.set('');
    });
  }

  retry(): void { this.refresh.update((value) => value + 1); }
  memberName(player: Membership): string { return player.displayName || `${player.firstName} ${player.lastName}`; }
  opponentName(match: Match): string {
    return (match.homeTeam?.club.externalId === this.club().externalId ? match.awayTeam?.club.name : match.homeTeam?.club.name) || 'Przeciwnik nieprzypisany';
  }
  score(match: Match): string {
    const home = (match.sets || []).filter((set) => set.homePoints > set.awayPoints).length;
    const away = (match.sets || []).filter((set) => set.awayPoints > set.homePoints).length;
    return match.awayTeam?.club.externalId === this.club().externalId ? `${away} : ${home}` : `${home} : ${away}`;
  }
  outcome(match: Match): 'won' | 'lost' | '' {
    if (match.homeTeam?.club.externalId !== this.club().externalId && match.awayTeam?.club.externalId !== this.club().externalId) return '';
    const [ours, theirs] = this.score(match).split(':').map(Number);
    return ours === theirs ? '' : ours > theirs ? 'won' : 'lost';
  }
  openPicker(picker: 'player' | 'match'): void {
    this.picker.set(picker);
    this.search.set('');
    this.filterDialog.nativeElement.showModal();
  }
  closePicker(): void { this.filterDialog.nativeElement.close(); }
  selectPlayer(id: string): void { this.selectedPlayerId.set(id); this.activePointId.set(''); this.closePicker(); }
  selectMatch(id: string): void { this.selectedMatchId.set(id); this.activePointId.set(''); this.closePicker(); }
  clearFilters(): void { this.selectedPlayerId.set(''); this.selectedMatchId.set(''); this.activePointId.set(''); }
  rate(value: number | null | undefined): string { return value == null ? '—' : `${value}%`; }
  x(index: number): number { return (index + .5) * 100 / this.trend().length; }
  y(value: number): number { return (100 - value) / (100 - this.axisMin()) * 100; }
  paths(metric: typeof this.metrics[number]['key']): string[] {
    const paths: string[] = [];
    let current: string[] = [];
    this.trend().forEach((point, index) => {
      const value = point.summary?.[metric];
      if (point.incomplete) { if (current.length) paths.push(current.join(' ')); current = []; }
      else if (value != null) current.push(`${this.x(index)},${this.y(value)}`);
    });
    if (current.length) paths.push(current.join(' '));
    return paths;
  }
}
