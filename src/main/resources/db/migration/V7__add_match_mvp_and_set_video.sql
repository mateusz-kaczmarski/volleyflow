alter table volleyball_matches
    add column mvp_player_external_id uuid;

alter table volleyball_match_set
    add column video_url varchar(2048);
