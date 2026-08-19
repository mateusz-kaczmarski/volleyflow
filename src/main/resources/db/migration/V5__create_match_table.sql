create table volleyball_matches (
    id bigserial primary key,
    external_id uuid not null unique,
    status varchar(20) not null,
    scheduled_at timestamp with time zone not null,
    started_at timestamp with time zone,
    finished_at timestamp with time zone,
    location_city varchar(80) not null,
    location_zip_code varchar(6) not null,
    location_street varchar(120) not null,
    location_building_number varchar(20) not null,
    created_by_user_id bigint not null references user_account(id),
    created_by_club_id bigint not null references club(id),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    version integer not null default 0
);

create index idx_volleyball_matches_scheduled_at on volleyball_matches(scheduled_at);
create index idx_volleyball_matches_created_by_user_id on volleyball_matches(created_by_user_id);
create index idx_volleyball_matches_created_by_club_id on volleyball_matches(created_by_club_id);

create table volleyball_match_team (
    id bigserial primary key,
    external_id uuid not null unique,
    match_id bigint not null references volleyball_matches(id),
    club_id bigint not null references club(id),
    side varchar(10) not null,
    sets_won integer not null default 0,
    created_on timestamp with time zone
);

create index idx_volleyball_match_team_match_id on volleyball_match_team(match_id);
create index idx_volleyball_match_team_club_id on volleyball_match_team(club_id);

create table volleyball_match_set (
    id bigserial primary key,
    external_id uuid not null unique,
    match_id bigint not null references volleyball_matches(id),
    set_number integer not null,
    home_points integer not null,
    away_points integer not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    version integer not null default 0
);

create index idx_volleyball_match_set_match_id on volleyball_match_set(match_id);
