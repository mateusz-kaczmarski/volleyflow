export type Page = 'overview' | 'matches' | 'statistics' | 'squad' | 'members' | 'settings';

export interface AuthResponse { token?: string | null; message: string; externalId?: string | null; }
export interface Club { externalId: string; name: string; avatar: string | null; description: string | null; userRole: string; }
export interface Membership {
  externalId: string; clubExternalId: string; personProfileExternalId: string; role: string;
  firstName: string; lastName: string; displayName: string | null; shirtNumber: number | null;
  positions: string[]; activeFrom: string; activeTo: string | null; active: boolean;
}
export interface MatchTeam { club: Club; setsWon: number; }
export interface MatchLocation { city: string; zipCode: string; street: string; buildingNumber: string; }
export interface MatchSet { externalId: string; setNumber: number; homePoints: number; awayPoints: number; videoUrl: string | null; }
export interface Match { externalId: string; createdByClub?: Club; homeTeam: MatchTeam | null; awayTeam: MatchTeam | null; status: string; scheduledAt: string; matchLocation: MatchLocation; mvpPlayerExternalId: string | null; sets?: MatchSet[]; }
export interface User { externalId: string; email: string; phone: string | null; }
export interface Profile { externalId: string; userAccountExternalId: string; firstName: string; lastName: string; displayName: string | null; jumpCm: number | null; }
export interface ApiError { message?: string; fieldErrors?: { field: string; message: string }[]; }
