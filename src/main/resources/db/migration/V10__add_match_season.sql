alter table volleyball_matches
    add column season_number integer;

update volleyball_matches
set season_number = 3;

alter table volleyball_matches
    alter column season_number set not null;

alter table volleyball_matches
    add constraint chk_volleyball_matches_season_positive check (season_number > 0);

create index idx_volleyball_matches_club_season
    on volleyball_matches(created_by_club_id, season_number);
