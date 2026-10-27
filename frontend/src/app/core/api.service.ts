import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, Club, Match, MatchSet, Membership, Profile, User } from './models';
import { PlayerCareerStatisticsResponse, SetStatistics } from './statistics.models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  readonly token = signal(this.readStoredToken());
  constructor(private readonly http: HttpClient) {}

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/login', { email, password }).pipe(tap(({ token }) => {
      if (!token) throw new Error('Login response does not contain an access token');
      this.setToken(token);
    }));
  }
  register(data: Record<string, unknown>): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/register', data);
  }
  activate(userExternalId: string, token: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>('/api/auth/activate', null, { params: { userExternalId, token } });
  }
  requestPasswordReset(email: string): Observable<void> {
    return this.http.post<void>('/api/auth/password-reset', { email });
  }
  resetPassword(token: string, newPassword: string): Observable<void> {
    return this.http.post<void>('/api/auth/password-reset/confirm', { token, newPassword });
  }
  logout(): void { localStorage.removeItem('volleyflow.token'); this.token.set(null); }
  clubs(): Observable<Club[]> { return this.http.get<Club[]>('/api/clubs/my-clubs'); }
  searchClubs(query: string): Observable<Club[]> { return this.http.get<Club[]>('/api/clubs/search', { params: { query } }); }
  createClub(data: { name: string; avatar: string; description: string }): Observable<Club> { return this.http.post<Club>('/api/clubs', data); }
  updateClub(clubId: string, data: Record<string, unknown>): Observable<Club> { return this.http.put<Club>(`/api/clubs/${clubId}`, data); }
  deleteClub(clubId: string): Observable<void> { return this.http.delete<void>(`/api/clubs/${clubId}`); }
  players(clubId: string): Observable<Membership[]> { return this.http.get<Membership[]>(`/api/memberships/${clubId}/players`); }
  members(clubId: string): Observable<Membership[]> { return this.http.get<Membership[]>(`/api/memberships/${clubId}/members`); }
  createMembership(data: Record<string, unknown>): Observable<Membership> { return this.http.post<Membership>('/api/memberships', data); }
  updateMembership(clubId: string, membershipId: string, data: Record<string, unknown>): Observable<Membership> { return this.http.put<Membership>(`/api/memberships/${clubId}/${membershipId}`, data); }
  deleteMembership(clubId: string, membershipId: string): Observable<void> { return this.http.delete<void>(`/api/memberships/${clubId}/${membershipId}`); }
  matches(clubId: string): Observable<Match[]> { return this.http.get<Match[]>(`/internal/matches?clubExternalId=${clubId}`); }
  createMatch(data: Record<string, unknown>): Observable<Match> { return this.http.post<Match>('/internal/matches', data); }
  updateMatch(matchId: string, data: Record<string, unknown>): Observable<Match> { return this.http.put<Match>(`/internal/matches/${matchId}`, data); }
  matchDetails(clubId: string, matchId: string): Observable<Match> { return this.http.get<Match>(`/internal/matches/${clubId}/${matchId}`); }
  finishMatch(matchId: string): Observable<void> { return this.http.post<void>(`/internal/matches/${matchId}/finish`, {}); }
  updateMatchMvp(clubId: string, matchId: string, mvpPlayerExternalId: string | null): Observable<Match> { return this.http.put<Match>(`/internal/matches/${clubId}/${matchId}/mvp`, { mvpPlayerExternalId }); }
  createSet(clubId: string, matchId: string, homePoints: number, awayPoints: number): Observable<MatchSet> { return this.http.post<MatchSet>(`/internal/matches/${clubId}/${matchId}/set`, { homePoints, awayPoints }); }
  updateSet(clubId: string, matchId: string, setId: string, homePoints: number, awayPoints: number): Observable<MatchSet> { return this.http.put<MatchSet>(`/internal/matches/${clubId}/${matchId}/set/${setId}`, { homePoints, awayPoints }); }
  updateSetVideo(clubId: string, matchId: string, setId: string, videoUrl: string | null): Observable<MatchSet> { return this.http.put<MatchSet>(`/internal/matches/${clubId}/${matchId}/set/${setId}/video`, { videoUrl }); }
  deleteSet(clubId: string, matchId: string, setId: string): Observable<void> { return this.http.delete<void>(`/internal/matches/${clubId}/${matchId}/set/${setId}`); }
  setStatistics(clubId: string, matchId: string, setId: string): Observable<SetStatistics> { return this.http.get<SetStatistics>(`/internal/matches/${clubId}/${matchId}/set/${setId}/statistics`); }
  saveSetStatistics(clubId: string, matchId: string, setId: string, data: SetStatistics): Observable<void> { return this.http.put<void>(`/internal/matches/${clubId}/${matchId}/set/${setId}/statistics`, data); }
  playerCareerStatistics(clubId: string, playerId: string): Observable<PlayerCareerStatisticsResponse> { return this.http.get<PlayerCareerStatisticsResponse>(`/internal/matches/clubs/${clubId}/players/${playerId}/statistics`); }
  user(): Observable<User> { return this.http.get<User>('/api/users/me'); }
  updateUser(data: Record<string, unknown>): Observable<User> { return this.http.put<User>('/api/users/me', data); }
  changePassword(data: { oldPassword: string; newPassword: string }): Observable<void> { return this.http.put<void>('/api/users/me/password', data); }
  deleteUser(): Observable<void> { return this.http.delete<void>('/api/users/me'); }
  profile(): Observable<Profile> { return this.http.get<Profile>('/api/profiles/me'); }
  createProfile(data: Record<string, unknown>): Observable<Profile> { return this.http.post<Profile>('/api/profiles', data); }
  updateProfile(data: Record<string, unknown>): Observable<Profile> { return this.http.put<Profile>('/api/profiles', data); }
  deleteProfile(): Observable<void> { return this.http.delete<void>('/api/profiles/me'); }

  private readStoredToken(): string | null {
    const token = localStorage.getItem('volleyflow.token');
    if (!token || token === 'undefined' || token === 'null') {
      localStorage.removeItem('volleyflow.token');
      return null;
    }
    return token;
  }
  private setToken(token: string): void { localStorage.setItem('volleyflow.token', token); this.token.set(token); }
}
