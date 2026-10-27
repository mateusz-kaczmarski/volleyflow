import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { catchError, forkJoin, map, of } from 'rxjs';
import { ApiService } from './core/api.service';
import { ApiError, Club, Match, MatchSet, Membership, Page, Profile, User } from './core/models';
import { PlayerStatistic, PlayerStatisticInput, SetStatistics } from './core/statistics.models';
import { Language, translations } from './core/translations';
import { StatisticsCenterComponent } from './statistics/statistics-center.component';

type MatchDetailTab = 'overview' | 'sets' | 'statistics-sheet' | 'links';
interface SheetSet { externalId: string; number: number; home: number; away: number; }
interface StatisticsCache { version: 4; statistics: Record<string, PlayerStatistic[]>; dirtySetIds: string[]; }
const numericStatisticFields = [
  'points', 'balance', 'serveTotal', 'serveError', 'serveAces', 'opponentReceivedServeCount',
  'receptionTotal', 'receptionError', 'receptionNegativeCount', 'receptionNeutralCount',
  'receptionPositiveCount', 'receptionPerfectCount', 'receptionPositivePercent', 'receptionPerfectPercent',
  'attackTotal', 'attackError', 'attackBlocked', 'attackPoints', 'opponentDefendedAttackCount',
  'blockPoints', 'blockTouches', 'defense', 'assists'
] as const;
const editableStatisticFields = [
  'serveError', 'serveAces', 'opponentReceivedServeCount',
  'receptionError', 'receptionNegativeCount', 'receptionNeutralCount', 'receptionPositiveCount', 'receptionPerfectCount',
  'attackError', 'attackBlocked', 'attackPoints', 'opponentDefendedAttackCount',
  'blockPoints', 'blockTouches', 'defense', 'assists'
] as const;

@Component({
  selector: 'vf-root',
  imports: [CommonModule, FormsModule, DatePipe, RouterOutlet, StatisticsCenterComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  readonly api = inject(ApiService);
  readonly router = inject(Router);
  readonly language = signal<Language>((localStorage.getItem('volleyflow.language') as Language) || 'pl');
  readonly page = signal<Page>('overview');
  readonly clubs = signal<Club[]>([]);
  readonly activeClub = signal<Club | null>(null);
  readonly players = signal<Membership[]>([]);
  readonly playersLoading = signal(false);
  readonly playersLoadError = signal(false);
  readonly clubMembers = signal<Membership[]>([]);
  readonly matches = signal<Match[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly authMode = signal<'login' | 'register'>('login');
  readonly registrationEmail = signal('');
  readonly activationPage = signal(false);
  readonly passwordResetPage = signal(false);
  readonly passwordResetCompletePage = signal(false);
  readonly passwordResetStatus = signal<'idle' | 'loading' | 'sent' | 'success' | 'error'>('idle');
  readonly passwordResetError = signal('');
  passwordResetEmail = '';
  readonly passwordResetToken = signal('');
  newPassword = '';
  confirmNewPassword = '';
  readonly activationStatus = signal<'idle' | 'loading' | 'success' | 'error'>('idle');
  readonly activationError = signal('');
  activationLink = '';
  private activationUserId = '';
  private activationToken = '';
  private lastAutoActivationLink = '';
  readonly showClubForm = signal(false);
  readonly showMatchForm = signal(false);
  readonly showMemberForm = signal(false);
  readonly showAccountForm = signal(false);
  readonly editingMatch = signal<Match | null>(null);
  readonly editingMember = signal<Membership | null>(null);
  readonly deactivateMemberOnSave = signal(false);
  readonly clubSearchResults = signal<Club[]>([]);
  readonly searchingClubs = signal(false);
  readonly selectedMatch = signal<Match | null>(null);
  readonly matchesLoading = signal(false);
  readonly matchesLoadError = signal(false);
  readonly matchDetailTab = signal<MatchDetailTab>('overview');
  readonly statisticsStatus = signal<'idle' | 'local' | 'saving' | 'saved' | 'error'>('idle');
  readonly dirtySetIds = signal<string[]>([]);
  readonly setSaving = signal(false);
  readonly editingSetVideo = signal<SheetSet | null>(null);
  readonly matchDetailsLoading = signal(false);
  readonly finishingMatch = signal(false);
  readonly showMockPlayerPicker = signal(false);
  readonly pendingSetDeletion = signal<SheetSet | null>(null);
  readonly selectedStatisticId = signal('');
  readonly statisticsScope = signal<'ALL' | number>('ALL');
  readonly chartType = signal<'bars' | 'donuts'>('bars');
  readonly mvpPlayerExternalId = signal('');
  readonly dashboardStatistics = signal<PlayerStatistic[]>([]);
  readonly mockSets = signal<SheetSet[]>([]);
  readonly setMockStatistics = signal<Record<number, PlayerStatistic[]>>({});
  readonly setVideos = signal<Record<string, string>>({});
  readonly availablePlayers = computed(() => {
    const scope = this.statisticsScope();
    const existing = new Set(scope === 'ALL' ? [] : (this.setMockStatistics()[scope] || []).map((row) => row.playerExternalId));
    return this.players().filter((member) => member.active && !existing.has(member.personProfileExternalId));
  });
  readonly pendingStatisticsInScope = computed(() => {
    const scope = this.statisticsScope();
    return this.mockSets().some((set) => (scope === 'ALL' || scope === set.number) && this.dirtySetIds().includes(set.externalId));
  });
  readonly displayedStatistics = computed(() => this.statisticsScope() === 'ALL' ? this.aggregateSetStatistics() : this.setMockStatistics()[this.statisticsScope() as number] || []);
  readonly selectedStatistic = computed(() => this.displayedStatistics().find((statistic) => statistic.playerExternalId === this.selectedStatisticId()) || this.displayedStatistics()[0]);
  readonly bestScorer = computed(() => this.displayedStatistics().reduce<PlayerStatistic | null>((best, row) => !best || row.points > best.points ? row : best, null));
  readonly mvpPlayer = computed(() => this.players().find((player) => player.personProfileExternalId === this.mvpPlayerExternalId()) || null);
  readonly teamReception = computed(() => this.percent(this.displayedStatistics().reduce((total, statistic) => total + statistic.receptionPositivePercent * statistic.receptionTotal, 0), this.displayedStatistics().reduce((total, statistic) => total + statistic.receptionTotal * 100, 0)));
  readonly teamAttack = computed(() => this.percent(this.displayedStatistics().reduce((total, statistic) => total + statistic.attackPoints, 0), this.displayedStatistics().reduce((total, statistic) => total + statistic.attackTotal, 0)));
  readonly user = signal<User | null>(null);
  readonly profile = signal<Profile | null>(null);
  readonly login = { email: '', password: '' };
  readonly registration = { email: '', password: '', firstName: '', lastName: '', displayName: '', phone: '', jumpCm: null as number | null };
  readonly clubForm = { name: '', avatar: '', description: '' };
  readonly matchForm = { awayClubExternalId: '', opponentName: '', scheduledAt: '', city: '', zipCode: '', street: '', buildingNumber: '' };
  readonly memberForm = { role: 'PLAYER', firstName: '', lastName: '', displayName: '', shirtNumber: null as number | null, positions: [] as string[], activeFrom: '', activeTo: '' };
  readonly accountForm = { email: '', phone: '', firstName: '', lastName: '', displayName: '', jumpCm: null as number | null, oldPassword: '', newPassword: '' };
  readonly setVideoForm = { url: '' };
  readonly clubInitials = computed(() => this.activeClub()?.name.split(' ').map((word) => word[0]).join('').slice(0, 2) || 'VF');
  readonly staff = computed(() => ['OWNER', 'TRAINER', 'STATISTIC'].includes(this.activeClub()?.userRole || ''));
  readonly staffMembers = computed(() => this.clubMembers().filter((member) => member.role !== 'PLAYER'));
  readonly nextMatch = computed(() => this.matches().find((match) => new Date(match.scheduledAt) >= new Date()) || this.matches()[0]);
  readonly finishedMatchesCount = computed(() => this.matches().filter((match) => match.status === 'FINISHED').length);
  readonly dashboardTeamStatistics = computed(() => this.aggregateTeamStatistics(this.dashboardStatistics()));
  private clubSearchTimeout: ReturnType<typeof setTimeout> | undefined;
  private dashboardStatisticsRequestId = 0;
  private backendStatisticsLoadedForMatchId = '';

  constructor() {
    document.documentElement.lang = this.language();
    this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.restoreActivationRoute();
        if (!this.activationPage() && !this.passwordResetPage()) this.restoreRoute();
      }
    });
    this.restoreActivationRoute();
    if (this.api.token() && !window.location.pathname.startsWith('/activate') && !window.location.pathname.startsWith('/forgot-password') && !window.location.pathname.startsWith('/reset-password')) this.loadClubs();
  }

  submitAuth(): void {
    this.error.set('');
    this.loading.set(true);
    const registering = this.authMode() === 'register';
    const request = !registering
      ? this.api.login(this.login.email, this.login.password)
      : this.api.register(this.registration);
    request.subscribe({
      next: () => {
        this.loading.set(false);
        if (registering) {
          this.registrationEmail.set(this.registration.email.trim());
          this.registration.password = '';
          return;
        }
        this.loadClubs();
      },
      error: (error) => {
        this.loading.set(false);
        if (!registering && error instanceof HttpErrorResponse && error.status === 401) {
          this.api.logout();
          this.error.set(this.language() === 'pl'
            ? 'Nieprawidłowe dane logowania lub konto nie zostało aktywowane. Sprawdź pocztę.'
            : 'Invalid credentials or the account is not activated. Check your email.');
          return;
        }
        this.handleError(error);
      }
    });
  }

  openActivation(): void { void this.router.navigateByUrl('/activate'); }

  requestPasswordReset(): void {
    if (this.passwordResetStatus() === 'loading') return;
    this.passwordResetError.set('');
    this.passwordResetStatus.set('loading');
    this.api.requestPasswordReset(this.passwordResetEmail.trim()).subscribe({
      next: () => this.passwordResetStatus.set('sent'),
      error: (error: HttpErrorResponse) => {
        this.passwordResetStatus.set('error');
        this.passwordResetError.set(error.status === 0 || error.status >= 500
          ? (this.language() === 'pl' ? 'Nie udało się połączyć z serwerem. Spróbuj ponownie.' : 'Could not reach the server. Please try again.')
          : (this.language() === 'pl' ? 'Nie udało się wysłać prośby. Spróbuj ponownie.' : 'Could not submit your request. Please try again.'));
      }
    });
  }

  submitPasswordReset(): void {
    if (this.passwordResetStatus() === 'loading') return;
    if (this.newPassword.length < 8) {
      this.passwordResetError.set(this.language() === 'pl' ? 'Hasło musi mieć co najmniej 8 znaków.' : 'Password must be at least 8 characters long.');
      return;
    }
    if (this.newPassword !== this.confirmNewPassword) {
      this.passwordResetError.set(this.language() === 'pl' ? 'Podane hasła nie są takie same.' : 'The passwords do not match.');
      return;
    }
    this.passwordResetError.set('');
    this.passwordResetStatus.set('loading');
    this.api.resetPassword(this.passwordResetToken(), this.newPassword).subscribe({
      next: () => this.passwordResetStatus.set('success'),
      error: (error: HttpErrorResponse) => {
        this.passwordResetStatus.set('error');
        this.passwordResetError.set(error.status === 0 || error.status >= 500
          ? (this.language() === 'pl' ? 'Serwer jest niedostępny. Spróbuj ponownie później.' : 'The server is unavailable. Please try again later.')
          : (this.language() === 'pl' ? 'Link jest nieprawidłowy lub wygasł. Poproś o nowy.' : 'This link is invalid or expired. Request a new one.'));
      }
    });
  }

  backToLogin(): void {
    this.passwordResetStatus.set('idle');
    this.passwordResetError.set('');
    void this.router.navigateByUrl('/');
  }

  returnToLogin(): void {
    this.authMode.set('login');
    this.login.email = this.registrationEmail() || this.login.email;
    this.registrationEmail.set('');
    this.error.set('');
    void this.router.navigateByUrl('/');
  }

  activateAccount(): void {
    if (this.activationStatus() === 'loading' || this.activationStatus() === 'success') return;
    this.activationError.set('');
    try {
      const link = new URL(this.activationLink.trim());
      this.activationUserId = link.searchParams.get('userId') || link.searchParams.get('userExternalId') || '';
      this.activationToken = link.searchParams.get('token') || '';
      if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(this.activationUserId) || !this.activationToken) {
        throw new Error('Invalid activation link');
      }
    } catch {
      this.activationStatus.set('error');
      this.activationError.set(this.language() === 'pl'
        ? 'Wklej pełny link aktywacyjny z wiadomości, zawierający identyfikator konta i token.'
        : 'Paste the full activation link from your email, including the account ID and token.');
      return;
    }
    this.activationStatus.set('loading');
    this.api.activate(this.activationUserId, this.activationToken).subscribe({
      next: (response) => {
        // The current backend returns HTTP 200 also for failed activation.
        if (!response.externalId) {
          this.activationStatus.set('error');
          this.activationError.set(this.language() === 'pl'
            ? 'Nie udało się aktywować konta. Link jest nieważny lub wygasł.'
            : 'Activation failed. The link is invalid or expired.');
          return;
        }
        this.activationStatus.set('success');
        this.activationLink = '';
        this.activationToken = '';
        // Activation is followed by a separate login; do not store its JWT.
        void this.router.navigate(['/activate'], { replaceUrl: true });
      },
      error: (error: HttpErrorResponse) => {
        this.activationStatus.set('error');
        const unavailable = error.status === 0 || error.status >= 500;
        this.activationError.set(this.language() === 'pl'
          ? unavailable ? 'Serwer jest niedostępny. Spróbuj ponownie później.' : 'Nie udało się aktywować konta. Link jest nieważny lub wygasł.'
          : unavailable ? 'The server is unavailable. Please try again later.' : 'Activation failed. The link is invalid or expired.');
      }
    });
  }

  private restoreActivationRoute(): void {
    const route = this.router.parseUrl(this.router.url);
    const path = route.root.children['primary']?.segments.map((segment) => segment.path).join('/');
    const activating = path === 'activate';
    const resetting = path === 'reset-password';
    this.passwordResetPage.set(path === 'forgot-password' || resetting);
    this.passwordResetCompletePage.set(resetting);
    if (resetting) {
      this.passwordResetToken.set(route.queryParams['token'] || '');
      this.passwordResetStatus.set('idle');
      this.passwordResetError.set('');
    } else if (path !== 'forgot-password') {
      this.passwordResetToken.set('');
      this.passwordResetStatus.set('idle');
      this.passwordResetError.set('');
    }
    this.activationPage.set(activating);
    if (!activating) {
      this.activationStatus.set('idle');
      this.activationError.set('');
      this.activationLink = '';
      this.activationToken = '';
      this.lastAutoActivationLink = '';
      return;
    }
    if (route.queryParams['token']) {
      const link = new URL(this.router.url, window.location.origin).toString();
      if (this.activationStatus() === 'loading' || link === this.lastAutoActivationLink) return;
      this.lastAutoActivationLink = link;
      this.activationStatus.set('idle');
      this.activationError.set('');
      this.activationLink = link;
      this.activateAccount();
    }
  }

  loadClubs(): void {
    this.loading.set(true);
    this.api.clubs().subscribe({
      next: (clubs) => {
        this.clubs.set(clubs);
        this.loading.set(false);
        if (!clubs.length) return;
        const requestedClubId = this.routeState()?.clubId;
        this.selectClub(clubs.find((club) => club.externalId === requestedClubId) || clubs[0], Boolean(requestedClubId));
      },
      error: (error) => this.handleError(error)
    });
  }

  selectClub(club: Club, restoreRoute = false): void {
    this.selectedMatch.set(null);
    this.players.set([]);
    this.dashboardStatistics.set([]);
    this.matches.set([]);
    this.activeClub.set(club);
    Object.assign(this.clubForm, { name: club.name, avatar: club.avatar || '', description: club.description || '' });
    const route = restoreRoute ? this.routeState() : null;
    this.page.set(route?.matchId ? 'matches' : route?.page || 'overview');
    this.loadClubData();
    if (!restoreRoute) this.navigateToState();
  }

  loadClubData(): void {
    const club = this.activeClub();
    if (!club) return;
    this.playersLoading.set(true);
    this.playersLoadError.set(false);
    this.api.players(club.externalId).subscribe({
      next: (players) => {
        if (this.activeClub()?.externalId !== club.externalId) return;
        this.playersLoading.set(false);
        this.players.set(players);
        this.setMockStatistics.update((sets) => Object.fromEntries(Object.entries(sets).map(([number, rows]) => {
          return [number, this.completeSetStatistics(rows)];
        })));
        this.loadDashboardStatistics();
      },
      error: () => {
        if (this.activeClub()?.externalId !== club.externalId) return;
        this.playersLoading.set(false);
        this.playersLoadError.set(true);
        this.players.set([]);
      }
    });
    if (this.staff()) {
      this.api.members(club.externalId).subscribe({ next: (members) => this.clubMembers.set(members), error: () => this.clubMembers.set([]) });
    } else {
      this.clubMembers.set([]);
    }
    this.matchesLoading.set(true);
    this.matchesLoadError.set(false);
    this.api.matches(club.externalId).subscribe({
      next: (matches) => {
        if (this.activeClub()?.externalId === club.externalId) {
          this.matchesLoading.set(false);
          this.matches.set(matches);
          this.loadDashboardStatistics();
          this.restoreRoute();
        }
      },
      error: () => { if (this.activeClub()?.externalId === club.externalId) { this.matchesLoading.set(false); this.matchesLoadError.set(true); this.matches.set([]); this.dashboardStatistics.set([]); } }
    });
  }

  createClub(): void {
    this.api.createClub(this.clubForm).subscribe({
      next: (club) => { this.clubs.update((clubs) => [...clubs, club]); this.showClubForm.set(false); this.clubForm.name = ''; this.clubForm.avatar = ''; this.clubForm.description = ''; this.selectClub(club); },
      error: (error) => this.handleError(error)
    });
  }

  createMatch(): void {
    const club = this.activeClub();
    if (!club) return;
    if (!this.matchForm.awayClubExternalId) { this.error.set(this.t('chooseOpponent')); return; }
    const data = {
      createdByClubExternalId: club.externalId, homeClubExternalId: club.externalId, awayClubExternalId: this.matchForm.awayClubExternalId,
      scheduledAt: new Date(this.matchForm.scheduledAt).toISOString(),
      matchLocation: { city: this.matchForm.city, zipCode: this.matchForm.zipCode, street: this.matchForm.street, buildingNumber: this.matchForm.buildingNumber }
    };
    this.api.createMatch(data).subscribe({ next: (match) => { this.matches.update((matches) => [...matches, match]); this.showMatchForm.set(false); }, error: (error) => this.handleError(error) });
  }

  openAccount(): void {
    this.error.set(''); this.showAccountForm.set(true);
    this.api.user().subscribe({ next: (user) => { this.user.set(user); this.accountForm.email = user.email; this.accountForm.phone = user.phone || ''; }, error: (error) => this.handleError(error) });
    this.api.profile().subscribe({ next: (profile) => { this.profile.set(profile); Object.assign(this.accountForm, profile); }, error: (error) => this.handleError(error) });
  }

  saveAccount(): void {
    this.api.updateUser({ email: this.accountForm.email, phone: this.accountForm.phone }).subscribe({
      next: (user) => { this.user.set(user); const data = { firstName: this.accountForm.firstName, lastName: this.accountForm.lastName, displayName: this.accountForm.displayName, jumpCm: this.accountForm.jumpCm }; const request = this.profile() ? this.api.updateProfile(data) : this.api.createProfile(data); request.subscribe({ next: (profile) => { this.profile.set(profile); this.error.set(this.t('profileSaved')); }, error: (error) => this.handleError(error) }); }, error: (error) => this.handleError(error)
    });
  }

  savePassword(): void {
    this.api.changePassword({ oldPassword: this.accountForm.oldPassword, newPassword: this.accountForm.newPassword }).subscribe({ next: () => { this.accountForm.oldPassword = ''; this.accountForm.newPassword = ''; this.error.set(this.t('passwordChanged')); }, error: (error) => this.handleError(error) });
  }

  saveClub(): void {
    const club = this.activeClub(); if (!club) return;
    this.api.updateClub(club.externalId, { name: this.clubForm.name, avatar: this.clubForm.avatar, description: this.clubForm.description }).subscribe({ next: (updated) => { this.activeClub.set(updated); this.clubs.update((clubs) => clubs.map((item) => item.externalId === updated.externalId ? updated : item)); this.error.set(this.t('clubSaved')); }, error: (error) => this.handleError(error) });
  }

  openMember(member?: Membership): void {
    this.editingMember.set(member || null);
    this.deactivateMemberOnSave.set(false);
    Object.assign(this.memberForm, member ? { role: member.role, firstName: member.firstName, lastName: member.lastName, displayName: member.displayName || '', shirtNumber: member.shirtNumber, positions: member.positions || [], activeFrom: member.activeFrom || '', activeTo: member.activeTo || '' } : { role: 'PLAYER', firstName: '', lastName: '', displayName: '', shirtNumber: null, positions: [], activeFrom: '', activeTo: '' });
    this.showMemberForm.set(true);
  }

  saveMember(): void {
    const club = this.activeClub(); if (!club) return;
    const member = this.editingMember();
    if (member && this.deactivateMemberOnSave()) {
      this.api.deleteMembership(club.externalId, member.externalId).subscribe({ next: () => { this.showMemberForm.set(false); this.loadClubData(); }, error: (error) => this.handleError(error) });
      return;
    }
    const data = { ...this.memberForm, clubExternalId: club.externalId, displayName: this.memberForm.displayName || null, shirtNumber: this.memberForm.role === 'PLAYER' ? this.memberForm.shirtNumber : null, positions: this.memberForm.role === 'PLAYER' ? this.memberForm.positions : [], activeFrom: this.memberForm.activeFrom || null, activeTo: this.memberForm.activeTo || null };
    const request = member ? this.api.updateMembership(club.externalId, member.externalId, data) : this.api.createMembership(data);
    request.subscribe({ next: () => { this.showMemberForm.set(false); this.loadClubData(); }, error: (error) => this.handleError(error) });
  }

  setMemberActive(active: boolean): void { this.deactivateMemberOnSave.set(!active); }
  t(key: string): string { return translations[this.language()][key] || key; }
  setLanguage(language: Language): void { this.language.set(language); document.documentElement.lang = language; localStorage.setItem('volleyflow.language', language); }
  dateLocale(): string { return this.language() === 'pl' ? 'pl-PL' : 'en-US'; }
  positionLabel(position: string): string { return ({ SETTER: this.language() === 'pl' ? 'ROZGRYWAJĄCY' : 'SETTER', OUTSIDE_HITTER: this.language() === 'pl' ? 'PRZYJMUJĄCY' : 'OUTSIDE HITTER', MIDDLE_BLOCKER: this.language() === 'pl' ? 'ŚRODKOWY' : 'MIDDLE BLOCKER', OPPOSITE: this.language() === 'pl' ? 'ATAKUJĄCY' : 'OPPOSITE', LIBERO: 'LIBERO' } as Record<string, string>)[position] || position; }
  positionClass(position: string): string { return `position-${position.toLowerCase().replaceAll('_', '-')}`; }
  roleLabel(role: string): string { return ({ OWNER: this.t('owner'), TRAINER: this.t('trainer'), STATISTIC: this.t('statistician'), PLAYER: this.t('playerRole') } as Record<string, string>)[role] || role; }
  matchStatusLabel(status: string): string { return ({ SCHEDULED: this.t('matchScheduled'), FINISHED: this.t('matchFinished'), CANCELLED: this.t('matchCancelled'), IN_PROGRESS: this.t('matchInProgress') } as Record<string, string>)[status] || status; }

  deactivateMember(member: Membership): void {
    const club = this.activeClub(); if (!club || !confirm(`Dezaktywować członkostwo: ${member.firstName} ${member.lastName}?`)) return;
    this.api.deleteMembership(club.externalId, member.externalId).subscribe({ next: () => this.loadClubData(), error: (error) => this.handleError(error) });
  }

  openMatch(match?: Match): void {
    this.editingMatch.set(match || null);
    this.clubSearchResults.set([]);
    Object.assign(this.matchForm, match ? { awayClubExternalId: match.awayTeam?.club.externalId || '', opponentName: match.awayTeam?.club.name || '', scheduledAt: this.toLocalDateTime(match.scheduledAt), city: match.matchLocation.city, zipCode: match.matchLocation.zipCode, street: match.matchLocation.street, buildingNumber: match.matchLocation.buildingNumber } : { awayClubExternalId: '', opponentName: '', scheduledAt: '', city: '', zipCode: '', street: '', buildingNumber: '' });
    this.showMatchForm.set(true);
  }

  openMatchDetails(match: Match, tab: MatchDetailTab = 'overview', navigate = true): void {
    this.backendStatisticsLoadedForMatchId = '';
    this.selectedMatch.set(match);
    this.loadMvp(match);
    this.loadSetVideos(match);
    this.matchDetailTab.set(tab);
    this.statisticsScope.set('ALL');
    this.loadMatchSheet(match);
    if (navigate) this.navigateToState();
    window.scrollTo({ top: 0, behavior: 'smooth' });
    const club = this.activeClub();
    if (club) {
      this.matchDetailsLoading.set(true);
      this.api.matchDetails(club.externalId, match.externalId).subscribe({
        next: (details) => {
          this.matches.update((matches) => matches.map((item) => item.externalId === details.externalId ? details : item));
          if (this.selectedMatch()?.externalId === details.externalId) {
            this.matchDetailsLoading.set(false);
            this.selectedMatch.set(details);
            this.loadMvp(details);
            this.loadSetVideos(details);
            this.loadMatchSheet(details);
            this.loadStatisticsForActiveTab();
          }
        },
        error: (error) => { if (this.selectedMatch()?.externalId === match.externalId) { this.matchDetailsLoading.set(false); this.handleError(error); } }
      });
    }
  }
  matchTeamName(match: Match, side: 'home' | 'away'): string {
    const team = side === 'home' ? match.homeTeam : match.awayTeam;
    if (team?.club.name) return team.club.name;
    if (side === 'home') return match.createdByClub?.name || this.activeClub()?.name || 'Gospodarze';
    return 'Przeciwnik do uzupełnienia';
  }
  matchScore(match: Match): string {
    const sets = match.sets || [];
    const homeSets = sets.filter((set) => set.homePoints > set.awayPoints).length;
    const awaySets = sets.filter((set) => set.awayPoints > set.homePoints).length;
    return `${homeSets} : ${awaySets}`;
  }
  matchOutcome(match: Match): 'won' | 'lost' | '' {
    if (match.status !== 'FINISHED') return '';
    const clubId = this.activeClub()?.externalId;
    const isHome = match.homeTeam?.club.externalId === clubId;
    if (!isHome && match.awayTeam?.club.externalId !== clubId) return '';
    const sets = match.sets || [];
    const home = sets.filter((set) => set.homePoints > set.awayPoints).length;
    const away = sets.filter((set) => set.awayPoints > set.homePoints).length;
    if (home === away) return '';
    return (isHome ? home > away : away > home) ? 'won' : 'lost';
  }
  matchMvp(match: Match): Membership | null {
    return this.players().find((player) => player.personProfileExternalId === match.mvpPlayerExternalId) || null;
  }
  finishMatch(): void {
    const match = this.selectedMatch();
    const club = this.activeClub();
    if (!match || !club || !this.staff() || match.status === 'FINISHED' || this.finishingMatch() || this.matchDetailsLoading() || this.setSaving()) return;
    this.finishingMatch.set(true);
    this.api.finishMatch(match.externalId).subscribe({
      next: () => {
        const finish = (item: Match): Match => item.externalId === match.externalId ? { ...item, status: 'FINISHED' } : item;
        if (this.activeClub()?.externalId === club.externalId) {
          this.matches.update((matches) => matches.map(finish));
          this.selectedMatch.update((item) => item ? finish(item) : null);
        }
        this.finishingMatch.set(false);
      },
      error: (error) => {
        this.finishingMatch.set(false);
        this.handleError(error);
        if (error.status === 400) this.error.set('Nie można zakończyć meczu. Jedna z drużyn musi wygrać trzy sety (25 punktów, w piątym secie 15, z przewagą co najmniej dwóch punktów).');
      }
    });
  }
  closeMatchDetails(navigate = true): void {
    this.selectedMatch.set(null);
    this.mvpPlayerExternalId.set('');
    if (navigate) this.navigateToState();
  }
  openSetVideo(set: SheetSet): void {
    this.editingSetVideo.set(set);
    this.setVideoForm.url = this.setVideos()[set.externalId] || '';
  }
  saveSetVideo(): void {
    const match = this.selectedMatch(); const set = this.editingSetVideo(); const club = this.activeClub();
    if (!match || !set || !club || !this.staff()) return;
    const url = this.setVideoForm.url.trim();
    this.api.updateSetVideo(club.externalId, match.externalId, set.externalId, url || null).subscribe({ next: (updated) => {
      this.setVideos.update((videos) => {
        const next = { ...videos };
        if (updated.videoUrl) next[set.externalId] = updated.videoUrl;
        else delete next[set.externalId];
        return next;
      });
      this.editingSetVideo.set(null);
    }, error: (error) => this.handleError(error) });
  }
  setMvp(playerExternalId: string): void {
    const match = this.selectedMatch(); const club = this.activeClub();
    if (!match || !club || !this.staff()) return;
    this.mvpPlayerExternalId.set(playerExternalId);
    this.api.updateMatchMvp(club.externalId, match.externalId, playerExternalId || null).subscribe({ next: (updated) => {
      this.matches.update((matches) => matches.map((item) => item.externalId === updated.externalId ? updated : item));
      this.selectedMatch.update((item) => item?.externalId === updated.externalId ? updated : item);
    }, error: (error) => { this.loadMvp(match); this.handleError(error); } });
  }
  memberName(member: Membership): string { return member.displayName || `${member.firstName} ${member.lastName}`; }
  videoThumbnail(url: string): string {
    try {
      const parsed = new URL(url);
      const videoId = parsed.hostname === 'youtu.be' ? parsed.pathname.slice(1)
        : parsed.searchParams.get('v') || parsed.pathname.match(/^\/(?:shorts|embed)\/([^/]+)/)?.[1];
      return videoId ? `https://img.youtube.com/vi/${videoId}/hqdefault.jpg` : '';
    } catch { return ''; }
  }
  updateStatistic(statistic: PlayerStatistic, field: keyof PlayerStatistic, value: string): void {
    const scope = this.statisticsScope();
    if (scope === 'ALL') return;
    if (!editableStatisticFields.includes(field as typeof editableStatisticFields[number])) {
      this.setMockStatistics.update((sets) => ({ ...sets, [scope]: (sets[scope] || []).map((item) => this.calculateStatistic(item)) }));
      return;
    }
    const numericValue = Math.max(0, Number(value) || 0);
    this.setMockStatistics.update((sets) => ({ ...sets, [scope]: (sets[scope] || []).map((item) => item.playerExternalId === statistic.playerExternalId
      ? this.calculateStatistic({ ...item, [field]: numericValue }) : item) }));
    this.markStatisticsDirty(scope);
  }
  receptionPercent(statistic: PlayerStatistic): number { return statistic.receptionPositivePercent; }
  receptionPerfectPercent(statistic: PlayerStatistic): number { return statistic.receptionPerfectPercent; }
  attackPercent(statistic: PlayerStatistic): number { return this.percent(statistic.attackPoints, statistic.attackTotal); }
  attackEfficiency(statistic: PlayerStatistic): number { return this.percent(statistic.attackPoints - statistic.attackError - statistic.attackBlocked, statistic.attackTotal); }
  serveEfficiency(statistic: PlayerStatistic): number { return this.percent(statistic.serveAces - statistic.serveError, statistic.serveTotal); }
  totalErrors(): number { return this.displayedStatistics().reduce((total, statistic) => total + statistic.serveError + statistic.receptionError + statistic.attackError, 0); }
  errorShare(field: 'serveError' | 'receptionError' | 'attackError'): number { return this.percent(this.displayedStatistics().reduce((total, statistic) => total + statistic[field], 0), this.totalErrors()); }
  chartPercent(value: number): number { return Math.max(0, Math.min(100, value)); }
  saveMockStatistics(): void {
    const match = this.selectedMatch(); const club = this.activeClub();
    if (!match || !club || !this.staff() || this.statisticsStatus() === 'saving') return;
    const scope = this.statisticsScope();
    if (scope === 'ALL') return;
    const sets = this.mockSets().filter((set) => set.number === scope)
      .filter((set) => this.dirtySetIds().includes(set.externalId));
    if (!sets.length) return;
    const snapshots = new Map(sets.map((set) => [set.externalId, { number: set.number, json: JSON.stringify(this.statisticsPayload(set.number)) }]));
    const requests = sets.map((set) => {
      return this.api.saveSetStatistics(club.externalId, match.externalId, set.externalId, this.statisticsPayload(set.number)).pipe(
        map(() => ({ setId: set.externalId, success: true })),
        catchError(() => of({ setId: set.externalId, success: false }))
      );
    });
    this.statisticsStatus.set('saving');
    forkJoin(requests).subscribe((results) => {
      if (this.selectedMatch()?.externalId !== match.externalId) return;
      const saved = new Set(results.filter((result) => {
        const snapshot = snapshots.get(result.setId);
        return result.success && snapshot && snapshot.json === JSON.stringify(this.statisticsPayload(snapshot.number));
      }).map((result) => result.setId));
      this.dirtySetIds.update((ids) => ids.filter((id) => !saved.has(id)));
      this.saveStatisticsCache();
      this.statisticsStatus.set(results.some((result) => !result.success) ? 'error' : this.dirtySetIds().length ? 'local' : 'saved');
    });
  }
  removeMockStatistic(id: string): void {
    const scope = this.statisticsScope(); if (scope === 'ALL') return;
    this.setMockStatistics.update((sets) => ({ ...sets, [scope]: (sets[scope] || []).filter((statistic) => statistic.playerExternalId !== id) }));
    this.markStatisticsDirty(scope);
  }
  addMockPlayer(member: Membership): void {
    const scope = this.statisticsScope(); if (scope === 'ALL') return;
    if (!this.staff() || this.matchDetailsLoading() || this.statisticsStatus() === 'saving') return;
    if ((this.setMockStatistics()[scope] || []).some((row) => row.playerExternalId === member.personProfileExternalId)) return;
    this.setMockStatistics.update((sets) => ({ ...sets, [scope]: [...(sets[scope] || []), this.emptyPlayerStatistic(member)] }));
    this.selectedStatisticId.set(member.personProfileExternalId); this.showMockPlayerPicker.set(false); this.markStatisticsDirty(scope);
  }
  percent(numerator: number, denominator: number): number { return denominator ? Math.round((numerator / denominator) * 100) : 0; }
  selectStatisticsScope(scope: 'ALL' | number): void {
    this.statisticsScope.set(scope);
    this.selectedStatisticId.set(this.displayedStatistics()[0]?.playerExternalId || '');
    queueMicrotask(() => this.navigateToState());
  }
  addMockSet(): void {
    const match = this.selectedMatch(); const club = this.activeClub();
    if (!match || this.setSaving() || this.matchDetailsLoading()) return;
    if (!club) return;
    this.setSaving.set(true);
    this.api.createSet(club.externalId, match.externalId, 0, 0).subscribe({
      next: (set) => { this.appendBackendSet(match.externalId, set); this.setSaving.set(false); },
      error: (error) => { this.setSaving.set(false); this.handleError(error); }
    });
  }
  updateMockSet(set: SheetSet, field: 'home' | 'away', value: string, input: HTMLInputElement): void {
    const points = Math.max(0, Number(value) || 0);
    const match = this.selectedMatch(); const club = this.activeClub();
    if (!match || this.setSaving() || this.matchDetailsLoading()) return;
    if (!club) return;
    this.setSaving.set(true);
    this.api.updateSet(club.externalId, match.externalId, set.externalId, field === 'home' ? points : set.home, field === 'away' ? points : set.away).subscribe({
      next: (updated) => { input.value = String(field === 'home' ? updated.homePoints : updated.awayPoints); this.replaceBackendSet(match.externalId, updated); this.setSaving.set(false); },
      error: (error) => { input.value = String(set[field]); this.setSaving.set(false); this.handleError(error); }
    });
  }
  removeMockSet(): void {
    const set = this.pendingSetDeletion(); const match = this.selectedMatch(); const club = this.activeClub();
    if (!set || !match || !club || this.setSaving()) return;
    this.setSaving.set(true);
    this.api.deleteSet(club.externalId, match.externalId, set.externalId).subscribe({
      next: () => {
        const remove = (item: Match): Match => ({ ...item, sets: (item.sets || []).filter((entry) => entry.externalId !== set.externalId) });
        this.matches.update((matches) => matches.map((item) => item.externalId === match.externalId ? remove(item) : item));
        if (this.selectedMatch()?.externalId === match.externalId) {
          this.selectedMatch.update((item) => item ? remove(item) : null);
          this.mockSets.update((sets) => sets.filter((item) => item.externalId !== set.externalId));
          this.setMockStatistics.update((statistics) => {
            const { [set.number]: _, ...remaining } = statistics;
            return remaining;
          });
          this.dirtySetIds.update((ids) => ids.filter((id) => id !== set.externalId));
          this.setVideos.update((videos) => {
            const { [set.externalId]: _, ...remaining } = videos;
            return remaining;
          });
          if (this.statisticsScope() === set.number) this.selectStatisticsScope('ALL');
          this.saveStatisticsCache();
        }
        this.pendingSetDeletion.set(null);
        this.setSaving.set(false);
      },
      error: (error) => { this.setSaving.set(false); this.handleError(error); }
    });
  }
  private emptyPlayerStatistic(member: Membership): PlayerStatistic {
    return { id: member.personProfileExternalId, playerExternalId: member.personProfileExternalId, player: member.displayName || `${member.firstName} ${member.lastName}`,
      position: this.statisticPosition(member), points: 0, balance: 0, serveTotal: 0, serveError: 0, serveAces: 0,
      opponentReceivedServeCount: 0, receptionTotal: 0, receptionError: 0, receptionNegativeCount: 0,
      receptionNeutralCount: 0, receptionPositiveCount: 0, receptionPerfectCount: 0,
      receptionPositivePercent: 0, receptionPerfectPercent: 0, attackTotal: 0, attackError: 0,
      attackBlocked: 0, attackPoints: 0, opponentDefendedAttackCount: 0, blockPoints: 0,
      blockTouches: 0, defense: 0, assists: 0 };
  }
  statisticPosition(member: Membership): string {
    return ({ SETTER: 'R', OUTSIDE_HITTER: 'P', MIDDLE_BLOCKER: 'Ś', OPPOSITE: 'A', LIBERO: 'L' } as Record<string, string>)[member.positions?.[0]] || '—';
  }
  private fromBackendSet(set: MatchSet): SheetSet { return { externalId: set.externalId, number: set.setNumber, home: set.homePoints, away: set.awayPoints }; }
  private appendBackendSet(matchId: string, set: MatchSet): void {
    this.matches.update((matches) => matches.map((match) => match.externalId === matchId ? { ...match, sets: [...(match.sets || []), set] } : match));
    if (this.selectedMatch()?.externalId !== matchId) return;
    this.selectedMatch.update((match) => match ? { ...match, sets: [...(match.sets || []), set] } : null);
    this.mockSets.update((sets) => [...sets, this.fromBackendSet(set)].sort((a, b) => a.number - b.number));
    this.setMockStatistics.update((sets) => ({ ...sets, [set.setNumber]: this.completeSetStatistics([]) }));
    this.markStatisticsDirty(set.setNumber);
    this.selectStatisticsScope(set.setNumber);
  }
  private replaceBackendSet(matchId: string, set: MatchSet): void {
    const replace = (match: Match): Match => ({ ...match, sets: (match.sets || []).map((item) => item.externalId === set.externalId ? set : item) });
    this.matches.update((matches) => matches.map((match) => match.externalId === matchId ? replace(match) : match));
    if (this.selectedMatch()?.externalId !== matchId) return;
    this.selectedMatch.update((match) => match ? replace(match) : null);
    this.mockSets.update((sets) => sets.map((item) => item.externalId === set.externalId ? this.fromBackendSet(set) : item));
  }
  private loadMatchSheet(match: Match): void {
    this.backendStatisticsLoadedForMatchId = '';
    const cache = this.readStatisticsCache(match);
    const sets = (match.sets || []).map((set) => this.fromBackendSet(set));
    const previousScope = this.selectedMatch()?.externalId === match.externalId ? this.statisticsScope() : 'ALL';
    this.mockSets.set(sets);
    const cachedDirtySetIds = cache?.dirtySetIds?.filter((id) => sets.some((set) => set.externalId === id)) || [];
    this.setMockStatistics.set(Object.fromEntries(sets.map((set) => {
      const rows = cache?.statistics?.[set.externalId];
      const statistics = cachedDirtySetIds.includes(set.externalId) && this.validStatistics(rows)
        ? rows.map((row) => this.withPlayerDetails(row)) : [];
      return [set.number, this.completeSetStatistics(statistics)];
    })));
    this.dirtySetIds.set(cachedDirtySetIds);
    this.statisticsScope.set(previousScope === 'ALL' || sets.some((set) => set.number === previousScope) ? previousScope : 'ALL');
    this.selectedStatisticId.set(this.displayedStatistics()[0]?.playerExternalId || '');
    this.statisticsStatus.set(this.dirtySetIds().length ? 'local' : 'idle');
  }
  private validStatistics(rows: unknown): rows is PlayerStatistic[] {
    return Array.isArray(rows) && rows.every((row) => row && typeof row === 'object'
      && typeof row.playerExternalId === 'string' && typeof row.position === 'string' && typeof row.player === 'string'
      && numericStatisticFields.every((field) => typeof row[field] === 'number' && Number.isFinite(row[field])));
  }
  private withPlayerDetails(row: PlayerStatistic): PlayerStatistic {
    const member = this.players().find((item) => item.personProfileExternalId === row.playerExternalId);
    return member ? { ...row, id: row.playerExternalId, player: member.displayName || `${member.firstName} ${member.lastName}`, position: this.statisticPosition(member) }
       : { ...row, id: row.playerExternalId, player: row.player || `Zawodnik ${row.playerExternalId.slice(0, 8)}`, position: row.position || '—' };
  }
  private completeSetStatistics(rows: PlayerStatistic[]): PlayerStatistic[] {
    const existingPlayers = new Set(rows.map((row) => row.playerExternalId));
    return [...rows.map((row) => this.calculateStatistic(row)), ...this.players().filter((member) => member.active && !existingPlayers.has(member.personProfileExternalId)).map((member) => this.emptyPlayerStatistic(member))];
  }
  private calculateStatistic(statistic: PlayerStatistic): PlayerStatistic {
    const serveError = Math.max(0, statistic.serveError || 0);
    const serveAces = Math.max(0, statistic.serveAces || 0);
    const opponentReceivedServeCount = Math.max(0, statistic.opponentReceivedServeCount || 0);
    const receptionError = Math.max(0, statistic.receptionError || 0);
    const receptionNegativeCount = Math.max(0, statistic.receptionNegativeCount || 0);
    const receptionNeutralCount = Math.max(0, statistic.receptionNeutralCount || 0);
    const receptionPositiveCount = Math.max(0, statistic.receptionPositiveCount || 0);
    const receptionPerfectCount = Math.max(0, statistic.receptionPerfectCount || 0);
    const attackError = Math.max(0, statistic.attackError || 0);
    const attackBlocked = Math.max(0, statistic.attackBlocked || 0);
    const attackPoints = Math.max(0, statistic.attackPoints || 0);
    const opponentDefendedAttackCount = Math.max(0, statistic.opponentDefendedAttackCount || 0);
    const blockPoints = Math.max(0, statistic.blockPoints || 0);
    const blockTouches = Math.max(0, statistic.blockTouches || 0);
    const defense = Math.max(0, statistic.defense || 0);
    const assists = Math.max(0, statistic.assists || 0);
    const serveTotal = serveError + serveAces + opponentReceivedServeCount;
    const receptionTotal = receptionError + receptionNegativeCount + receptionNeutralCount + receptionPositiveCount + receptionPerfectCount;
    const attackTotal = attackError + attackBlocked + attackPoints + opponentDefendedAttackCount;
    const points = serveAces + attackPoints + blockPoints;
    return {
      ...statistic,
      points,
      balance: points - serveError - receptionError - attackError,
      serveTotal,
      serveError,
      serveAces,
      opponentReceivedServeCount,
      receptionTotal,
      receptionError,
      receptionNegativeCount,
      receptionNeutralCount,
      receptionPositiveCount,
      receptionPerfectCount,
      receptionPositivePercent: this.percent(receptionPositiveCount + receptionPerfectCount, receptionTotal),
      receptionPerfectPercent: this.percent(receptionPerfectCount, receptionTotal),
      attackTotal,
      attackError,
      attackBlocked,
      attackPoints,
      opponentDefendedAttackCount,
      blockPoints,
      blockTouches,
      defense,
      assists
    };
  }
  private loadDashboardStatistics(): void {
    const club = this.activeClub();
    if (!club) return;
    const requestId = ++this.dashboardStatisticsRequestId;
    const finishedSets = this.matches().filter((match) => match.status === 'FINISHED').flatMap((match) => (match.sets || []).map((set) => ({ match, set })));
    if (!finishedSets.length) {
      this.dashboardStatistics.set([]);
      return;
    }
    forkJoin(finishedSets.map(({ match, set }) => this.api.setStatistics(club.externalId, match.externalId, set.externalId).pipe(catchError(() => of({ players: [] }))))).subscribe((results) => {
      if (requestId !== this.dashboardStatisticsRequestId || this.activeClub()?.externalId !== club.externalId) return;
      const rows = results.flatMap((result) => result.players).map((row) => ({ ...row, id: row.playerExternalId, position: '', player: '' }));
      if (!this.validStatistics(rows)) return;
      // Historical rows may not have the later-added action details. Preserve the stored totals and percentages.
      this.dashboardStatistics.set(this.aggregateStatistics(rows.map((row) => this.withPlayerDetails(row))));
    });
  }
  private loadBackendStatistics(match: Match): void {
    const club = this.activeClub();
    if (!club || this.backendStatisticsLoadedForMatchId === match.externalId) return;
    this.backendStatisticsLoadedForMatchId = match.externalId;
    for (const set of match.sets || []) {
      this.api.setStatistics(club.externalId, match.externalId, set.externalId).subscribe({
        next: (result: SetStatistics) => {
          if (this.selectedMatch()?.externalId !== match.externalId || this.dirtySetIds().includes(set.externalId)) return;
          const players = result?.players;
          if (!Array.isArray(players)) return;
          const rows = players.map((row) => ({ ...row, id: row.playerExternalId, position: '', player: '' }));
          if (!this.validStatistics(rows)) { this.statisticsStatus.set('error'); return; }
          const statisticsWithPlayers = this.completeSetStatistics(rows.map((row) => this.withPlayerDetails(row)));
          this.setMockStatistics.update((statistics) => ({ ...statistics, [set.setNumber]: statisticsWithPlayers }));
          if (statisticsWithPlayers.length > rows.length) this.markStatisticsDirty(set.setNumber);
          this.selectedStatisticId.set(this.displayedStatistics()[0]?.playerExternalId || '');
          this.saveStatisticsCache();
        },
        error: (error: HttpErrorResponse) => { if (error.status !== 404 && this.selectedMatch()?.externalId === match.externalId) this.statisticsStatus.set('error'); }
      });
    }
  }
  private markStatisticsDirty(number: number): void {
    const set = this.mockSets().find((item) => item.number === number);
    if (!set) return;
    this.dirtySetIds.update((ids) => ids.includes(set.externalId) ? ids : [...ids, set.externalId]);
    this.statisticsStatus.set('local');
    this.saveStatisticsCache();
  }
  private statisticsPayload(number: number): SetStatistics {
    const players: PlayerStatisticInput[] = (this.setMockStatistics()[number] || []).map(({ id, position, player, ...values }) => values);
    return { players };
  }
  private statisticsCacheKey(match: Match): string | null {
    const token = this.api.token();
    if (!token) return null;
    try {
      const accountId = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).externalId;
      return typeof accountId === 'string' && accountId ? `volleyflow.statistics.v2.${accountId}.${match.externalId}` : null;
    } catch { return null; }
  }
  private loadSetVideos(match: Match): void {
    this.setVideos.set(Object.fromEntries((match.sets || [])
      .filter((set) => Boolean(set.videoUrl))
      .map((set) => [set.externalId, set.videoUrl!])));
  }
  private loadMvp(match: Match): void {
    this.mvpPlayerExternalId.set(match.mvpPlayerExternalId || '');
  }
  private readStatisticsCache(match: Match): StatisticsCache | null {
    const key = this.statisticsCacheKey(match);
    if (!key) return null;
    try {
      const cache = JSON.parse(localStorage.getItem(key) || 'null') as StatisticsCache | null;
      return cache?.version === 4 && cache.statistics && typeof cache.statistics === 'object' && Array.isArray(cache.dirtySetIds) ? cache : null;
    } catch { return null; }
  }
  private saveStatisticsCache(): void {
    const match = this.selectedMatch();
    const key = match && this.statisticsCacheKey(match);
    if (!key || !match) return;
    const statistics = Object.fromEntries(this.mockSets().map((set) => [set.externalId, this.setMockStatistics()[set.number] || []]));
    const cache: StatisticsCache = { version: 4, statistics, dirtySetIds: this.dirtySetIds() };
    try { localStorage.setItem(key, JSON.stringify(cache)); }
    catch { this.statisticsStatus.set('error'); this.error.set('Nie udało się zapisać statystyk w przeglądarce.'); }
  }
  private aggregateSetStatistics(): PlayerStatistic[] {
    return this.aggregateStatistics(Object.values(this.setMockStatistics()).flat());
  }
  private aggregateStatistics(rows: PlayerStatistic[]): PlayerStatistic[] {
    const grouped = new Map<string, PlayerStatistic[]>();
    rows.forEach((statistic) => grouped.set(statistic.playerExternalId, [...(grouped.get(statistic.playerExternalId) || []), statistic]));
    return [...grouped.values()].map((statistics) => {
      const first = statistics[0];
      const sum = (field: keyof PlayerStatistic) => statistics.reduce((total, statistic) => total + Number(statistic[field] || 0), 0);
      const receptions = sum('receptionTotal');
      return { ...first, points: sum('points'), balance: sum('balance'), serveTotal: sum('serveTotal'), serveError: sum('serveError'), serveAces: sum('serveAces'), opponentReceivedServeCount: sum('opponentReceivedServeCount'), receptionTotal: receptions, receptionError: sum('receptionError'), receptionNegativeCount: sum('receptionNegativeCount'), receptionNeutralCount: sum('receptionNeutralCount'), receptionPositiveCount: sum('receptionPositiveCount'), receptionPerfectCount: sum('receptionPerfectCount'), receptionPositivePercent: this.percent(statistics.reduce((total, statistic) => total + statistic.receptionPositivePercent * statistic.receptionTotal, 0), receptions * 100), receptionPerfectPercent: this.percent(statistics.reduce((total, statistic) => total + statistic.receptionPerfectPercent * statistic.receptionTotal, 0), receptions * 100), attackTotal: sum('attackTotal'), attackError: sum('attackError'), attackBlocked: sum('attackBlocked'), attackPoints: sum('attackPoints'), opponentDefendedAttackCount: sum('opponentDefendedAttackCount'), blockPoints: sum('blockPoints'), blockTouches: sum('blockTouches'), defense: sum('defense'), assists: sum('assists') };
    });
  }
  private aggregateTeamStatistics(rows: PlayerStatistic[]): PlayerStatistic | null {
    if (!rows.length) return null;
    return this.aggregateStatistics(rows.map((row) => ({ ...row, playerExternalId: 'team' })))[0] || null;
  }

  updateMatch(): void {
    const club = this.activeClub(); const match = this.editingMatch(); if (!club || !match) return;
    if (!this.matchForm.awayClubExternalId) { this.error.set(this.t('chooseOpponent')); return; }
    const data = { homeClubExternalId: match.homeTeam?.club.externalId || club.externalId, awayClubExternalId: this.matchForm.awayClubExternalId, scheduledAt: new Date(this.matchForm.scheduledAt).toISOString(), location: { city: this.matchForm.city, zipCode: this.matchForm.zipCode, street: this.matchForm.street, buildingNumber: this.matchForm.buildingNumber } };
    this.api.updateMatch(match.externalId, data).subscribe({ next: (updated) => { this.matches.update((matches) => matches.map((item) => item.externalId === updated.externalId ? updated : item)); this.showMatchForm.set(false); }, error: (error) => this.handleError(error) });
  }

  deleteAccount(): void { if (!confirm(this.t('deleteAccountConfirm'))) return; this.api.deleteUser().subscribe({ next: () => this.logout(), error: (error) => this.handleError(error) }); }
  deleteProfile(): void { if (!confirm(this.t('deleteProfileConfirm'))) return; this.api.deleteProfile().subscribe({ next: () => { this.profile.set(null); this.error.set(this.t('deleteProfile')); }, error: (error) => this.handleError(error) }); }
  deleteCurrentClub(): void { const club = this.activeClub(); if (!club || !confirm(this.t('deleteClubConfirm'))) return; this.api.deleteClub(club.externalId).subscribe({ next: () => { this.clubs.update((clubs) => clubs.filter((item) => item.externalId !== club.externalId)); const nextClub = this.clubs()[0] || null; this.activeClub.set(nextClub); if (nextClub) this.selectClub(nextClub); }, error: (error) => this.handleError(error) }); }
  togglePosition(position: string): void { this.memberForm.positions = this.memberForm.positions.includes(position) ? this.memberForm.positions.filter((item) => item !== position) : [...this.memberForm.positions, position]; }
  searchOpponent(query: string): void {
    this.matchForm.opponentName = query;
    this.matchForm.awayClubExternalId = '';
    this.clubSearchResults.set([]);
    if (this.clubSearchTimeout) clearTimeout(this.clubSearchTimeout);
    if (query.trim().length < 2) return;
    this.searchingClubs.set(true);
    this.clubSearchTimeout = setTimeout(() => {
      this.api.searchClubs(query.trim()).subscribe({
        next: (clubs) => { this.clubSearchResults.set(clubs.filter((club) => club.externalId !== this.activeClub()?.externalId)); this.searchingClubs.set(false); },
        error: (error) => { this.searchingClubs.set(false); this.handleError(error); }
      });
    }, 250);
  }
  selectOpponent(club: Club): void { this.matchForm.opponentName = club.name; this.matchForm.awayClubExternalId = club.externalId; this.clubSearchResults.set([]); }

  setPage(page: Page): void {
    this.page.set(page);
    if (page === 'matches') this.closeMatchDetails(false);
    this.navigateToState();
  }
  setMatchDetailTab(tab: MatchDetailTab): void {
    this.matchDetailTab.set(tab);
    this.loadStatisticsForActiveTab();
    this.navigateToState();
  }
  logout(): void {
    this.api.logout();
    this.clubs.set([]);
    this.players.set([]);
    this.clubMembers.set([]);
    this.activeClub.set(null);
    this.page.set('overview');
    this.router.navigateByUrl('/');
  }
  trackById(_: number, item: { externalId: string }): string { return item.externalId; }

  private navigateToState(): void {
    const club = this.activeClub();
    if (!club) return;
    const match = this.selectedMatch();
    const commands = match
      ? ['/clubs', club.externalId, 'matches', match.externalId, this.matchDetailTab()]
      : ['/clubs', club.externalId, this.page()];
    if (this.router.url !== this.router.createUrlTree(commands).toString()) this.router.navigate(commands);
  }

  private restoreRoute(): void {
    const route = this.routeState();
    if (!route || !this.clubs().length) return;
    const club = this.clubs().find((item) => item.externalId === route.clubId);
    if (!club) return;
    if (club.externalId !== this.activeClub()?.externalId) {
      this.selectClub(club, true);
      return;
    }
    if (!route.matchId) {
      this.page.set(route.page || 'overview');
      this.closeMatchDetails(false);
      return;
    }
    this.page.set('matches');
    const match = this.matches().find((item) => item.externalId === route.matchId);
    if (!match) return;
    if (this.selectedMatch()?.externalId === match.externalId) {
      this.matchDetailTab.set(route.tab || 'overview');
      this.loadStatisticsForActiveTab();
    }
    else this.openMatchDetails(match, route.tab || 'overview', false);
  }

  private loadStatisticsForActiveTab(): void {
    const match = this.selectedMatch();
    if (match && !this.matchDetailsLoading() && this.matchDetailTab() === 'statistics-sheet') {
      this.loadBackendStatistics(match);
    }
  }

  private routeState(): { clubId: string; page?: Page; matchId?: string; tab?: MatchDetailTab } | null {
    const segments = this.router.parseUrl(this.router.url).root.children['primary']?.segments.map((segment) => segment.path) || [];
    if (segments[0] !== 'clubs' || !segments[1]) return null;
    if (segments[2] === 'matches' && segments[3] && this.isMatchDetailTab(segments[4])) {
      return { clubId: segments[1], matchId: segments[3], tab: segments[4] };
    }
    return this.isPage(segments[2]) ? { clubId: segments[1], page: segments[2] } : null;
  }

  private isPage(value: string | undefined): value is Page {
    return value === 'overview' || value === 'matches' || value === 'statistics' || value === 'squad' || value === 'members' || value === 'settings';
  }

  private isMatchDetailTab(value: string | undefined): value is MatchDetailTab {
    return value === 'overview' || value === 'sets' || value === 'statistics-sheet' || value === 'links';
  }

  private handleError(error: HttpErrorResponse): void {
    this.loading.set(false);
    if (error.status === 401) this.api.logout();
    const body = error.error as ApiError | undefined;
    this.error.set(body?.message ? this.t('genericError') : this.t('genericError'));
  }

  private toLocalDateTime(value: string): string { const date = new Date(value); date.setMinutes(date.getMinutes() - date.getTimezoneOffset()); return date.toISOString().slice(0, 16); }
}
