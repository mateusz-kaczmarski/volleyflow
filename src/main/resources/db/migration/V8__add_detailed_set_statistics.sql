alter table volleyball_match_set_statistic
    add column opponent_received_serve_count integer not null default 0,
    add column reception_negative_count integer not null default 0,
    add column reception_neutral_count integer not null default 0,
    add column reception_positive_count integer not null default 0,
    add column reception_perfect_count integer not null default 0,
    add column opponent_defended_attack_count integer not null default 0;
