-- Fictional, deterministic development fixture. Enabled only by the demo profile.
-- Existing V1 fixtures remain intact. Generated IDs use the normal DB sequences.
-- Once the Aurora owner exists, never overwrite locally edited demo data.
DO $demo$
DECLARE
    fixture_date CONSTANT timestamptz := '2026-09-01 12:00:00+00';
    owner_id bigint;
    profile_id bigint;
    membership_id bigint;
    aurora_id bigint;
    opponent_ids bigint[] := ARRAY[]::bigint[];
    opponent_id bigint;
    match_id bigint;
    set_id bigint;
    home_scores integer[];
    away_scores integer[];
    home_wins integer;
    away_wins integer;
    match_date timestamptz;
    player record;
    m integer;
    s integer;
    i integer;
    attack_points integer;
    attack_error integer;
    attack_blocked integer;
    defended integer;
    serve_aces integer;
    serve_error integer;
    received_serve integer;
    block_points integer;
    reception_error integer;
    reception_negative integer;
    reception_neutral integer;
    reception_positive integer;
    reception_perfect integer;
    reception_total integer;
    points integer;
BEGIN
    IF EXISTS (SELECT 1 FROM user_account
               WHERE external_id = md5('volleyflow-demo:owner')::uuid) THEN
        RETURN;
    END IF;

    -- Development-only password: demo1234 (BCrypt).
    INSERT INTO user_account (external_id, email, password_hash, status, email_verified, created_at)
    VALUES (md5('volleyflow-demo:owner')::uuid, 'aurora.owner@example.com',
            '$2b$10$pavZz3DJcGC.zQaC3TfBgemBBNqTOLMHnvxPq6YNrbmuhbozZbzf6',
            'ACTIVE', true, fixture_date)
    RETURNING id INTO owner_id;

    INSERT INTO person_profile (external_id, user_account_id, first_name, last_name, display_name, created_at)
    VALUES (md5('volleyflow-demo:owner-profile')::uuid, owner_id,
            'Aleksander', 'Przykładowy', 'Właściciel Aurora', fixture_date)
    RETURNING id INTO profile_id;

    INSERT INTO club (external_id, name, description, club_status, created_at)
    VALUES (md5('volleyflow-demo:aurora')::uuid, 'Aurora Volley',
            'Fikcyjny klub do rozwijania i testowania backendu.', 'ACTIVE', fixture_date)
    RETURNING id INTO aurora_id;

    INSERT INTO club_membership (external_id, club_id, person_profile_id, role, active, active_from, created_at)
    VALUES (md5('volleyflow-demo:owner-membership')::uuid, aurora_id, profile_id,
            'OWNER', true, '2026-09-01', fixture_date);

    INSERT INTO person_profile (external_id, first_name, last_name, display_name, created_at)
    VALUES (md5('volleyflow-demo:trainer')::uuid, 'Marek', 'Treningowy', 'Trener Aurora', fixture_date)
    RETURNING id INTO profile_id;

    INSERT INTO club_membership (external_id, club_id, person_profile_id, role, active, active_from, created_at)
    VALUES (md5('volleyflow-demo:trainer-membership')::uuid, aurora_id, profile_id,
            'TRAINER', true, '2026-09-01', fixture_date);

    FOR player IN
        SELECT * FROM (VALUES
            (1, 'Adam', 'Testowy', 'SETTER'),
            (2, 'Bartosz', 'Przykładowy', 'SETTER'),
            (3, 'Cezary', 'Modelowy', 'MIDDLE_BLOCKER'),
            (4, 'Daniel', 'Pokazowy', 'MIDDLE_BLOCKER'),
            (5, 'Emil', 'Demonstracyjny', 'OPPOSITE'),
            (6, 'Filip', 'Wzorcowy', 'OPPOSITE'),
            (7, 'Gabriel', 'Fikcyjny', 'OUTSIDE_HITTER'),
            (8, 'Hubert', 'Symulowany', 'OUTSIDE_HITTER'),
            (9, 'Igor', 'Treningowy', 'OUTSIDE_HITTER'),
            (10, 'Jan', 'Sparingowy', 'OUTSIDE_HITTER'),
            (11, 'Kamil', 'Ćwiczebny', 'LIBERO'),
            (12, 'Leon', 'Rezerwowy', 'LIBERO')
        ) AS roster(number, first_name, last_name, position)
        ORDER BY number
    LOOP
        INSERT INTO person_profile (external_id, first_name, last_name, display_name, jump_cm, created_at)
        VALUES (md5('volleyflow-demo:player:' || player.number)::uuid,
                player.first_name, player.last_name, player.first_name || ' ' || player.last_name,
                45 + player.number, fixture_date)
        RETURNING id INTO profile_id;

        INSERT INTO club_membership (external_id, club_id, person_profile_id, role, shirt_number,
                                     active, active_from, created_at)
        VALUES (md5('volleyflow-demo:membership:' || player.number)::uuid,
                aurora_id, profile_id, 'PLAYER', player.number, true, '2026-09-01', fixture_date)
        RETURNING id INTO membership_id;

        INSERT INTO club_membership_position (club_membership_id, position)
        VALUES (membership_id, player.position);
    END LOOP;

    FOR i IN 1..2 LOOP
        INSERT INTO club (external_id, name, description, club_status, created_at)
        VALUES (md5('volleyflow-demo:opponent:' || i)::uuid,
                CASE i WHEN 1 THEN 'Orion Volley' ELSE 'Polaris Volley' END,
                'Fikcyjny przeciwnik Aurora Volley.', 'ACTIVE', fixture_date)
        RETURNING id INTO opponent_id;
        opponent_ids := array_append(opponent_ids, opponent_id);
    END LOOP;

    -- Aurora plays at home: 3:1, 2:3, 3:0; two future fixtures have no results.
    FOR m IN 1..5 LOOP
        opponent_id := opponent_ids[1 + ((m - 1) % 2)];
        match_date := CASE WHEN m <= 3
            THEN '2026-09-05 16:00:00+00'::timestamptz + (m - 1) * interval '7 days'
            ELSE '2026-11-07 16:00:00+00'::timestamptz + (m - 4) * interval '7 days' END;
        home_scores := CASE m
            WHEN 1 THEN ARRAY[25, 22, 25, 25]
            WHEN 2 THEN ARRAY[25, 20, 25, 21, 12]
            WHEN 3 THEN ARRAY[25, 25, 25]
            ELSE ARRAY[]::integer[] END;
        away_scores := CASE m
            WHEN 1 THEN ARRAY[20, 25, 19, 21]
            WHEN 2 THEN ARRAY[19, 25, 22, 25, 15]
            WHEN 3 THEN ARRAY[18, 22, 20]
            ELSE ARRAY[]::integer[] END;
        home_wins := CASE m WHEN 1 THEN 3 WHEN 2 THEN 2 WHEN 3 THEN 3 ELSE 0 END;
        away_wins := CASE m WHEN 1 THEN 1 WHEN 2 THEN 3 ELSE 0 END;

        INSERT INTO volleyball_matches (external_id, status, scheduled_at, started_at, finished_at,
            location_city, location_zip_code, location_street, location_building_number,
            created_by_user_id, created_by_club_id, season_number, created_at, mvp_player_external_id)
        VALUES (md5('volleyflow-demo:match:' || m)::uuid,
            CASE WHEN m <= 3 THEN 'FINISHED' ELSE 'SCHEDULED' END,
            match_date, CASE WHEN m <= 3 THEN match_date END,
            CASE WHEN m <= 3 THEN match_date + interval '2 hours' END,
            'Miasto Demo', '00-001', 'Sportowa', '1', owner_id, aurora_id, 2026, fixture_date,
            CASE WHEN m <= 3 THEN md5('volleyflow-demo:player:' ||
                CASE WHEN m = 2 THEN 5 ELSE 7 END)::uuid END)
        RETURNING id INTO match_id;

        INSERT INTO volleyball_match_team (external_id, match_id, club_id, side, sets_won, created_on)
        VALUES (md5('volleyflow-demo:home:' || m)::uuid, match_id, aurora_id, 'HOME', home_wins, fixture_date),
               (md5('volleyflow-demo:away:' || m)::uuid, match_id, opponent_id, 'AWAY', away_wins, fixture_date);

        FOR s IN 1..cardinality(home_scores) LOOP
            INSERT INTO volleyball_match_set (external_id, match_id, set_number, home_points, away_points, created_at)
            VALUES (md5('volleyflow-demo:set:' || m || ':' || s)::uuid,
                    match_id, s, home_scores[s], away_scores[s], fixture_date)
            RETURNING id INTO set_id;

            -- Six participants per set; reserves have explicit zero statistics.
            FOR i IN 1..12 LOOP
                SELECT id INTO STRICT profile_id FROM person_profile
                WHERE external_id = md5('volleyflow-demo:player:' || i)::uuid;
                attack_points := CASE WHEN i IN (3, 5, 7, 8)
                    THEN home_scores[s] / 8 + ((m + s + i) % 2) WHEN i = 1 THEN 1 ELSE 0 END;
                attack_error := CASE WHEN i IN (3, 5, 7, 8) THEN (m + s + i) % 2 ELSE 0 END;
                attack_blocked := CASE WHEN i IN (5, 8) THEN 1 ELSE 0 END;
                defended := CASE WHEN i IN (3, 5, 7, 8) THEN 2 ELSE 0 END;
                serve_aces := CASE WHEN i IN (1, 7) THEN 1 ELSE 0 END;
                serve_error := CASE WHEN i = 5 THEN 1 ELSE 0 END;
                received_serve := CASE WHEN i IN (1, 3, 5, 7, 8) THEN 3 ELSE 0 END;
                block_points := CASE WHEN i IN (3, 5) THEN 1 ELSE 0 END;
                reception_error := CASE WHEN i = 8 THEN 1 ELSE 0 END;
                reception_negative := CASE WHEN i IN (7, 8, 11) THEN 1 ELSE 0 END;
                reception_neutral := 0;
                reception_positive := CASE WHEN i IN (7, 8, 11) THEN 1 ELSE 0 END;
                reception_perfect := CASE WHEN i IN (7, 8, 11) THEN 1 ELSE 0 END;
                reception_total := reception_error + reception_negative + reception_neutral
                    + reception_positive + reception_perfect;
                points := serve_aces + attack_points + block_points;

                INSERT INTO volleyball_match_set_statistic (external_id, set_id, club_id, player_profile_id,
                    points, balance, serve_total, serve_error, serve_aces, opponent_received_serve_count,
                    reception_total, reception_error, reception_negative_count, reception_neutral_count,
                    reception_positive_count, reception_perfect_count, reception_positive_percent, reception_perfect_percent,
                    attack_total, attack_error, attack_blocked, attack_points, opponent_defended_attack_count,
                    block_points, block_touches, defense, assists, created_at)
                VALUES (md5('volleyflow-demo:statistic:' || m || ':' || s || ':' || i)::uuid,
                    set_id, aurora_id, profile_id, points, points - serve_error - reception_error - attack_error,
                    serve_error + serve_aces + received_serve, serve_error, serve_aces, received_serve,
                    reception_total, reception_error, reception_negative, reception_neutral,
                    reception_positive, reception_perfect,
                    CASE WHEN reception_total = 0 THEN 0 ELSE
                        round(100.0 * (reception_positive + reception_perfect) / reception_total)::integer END,
                    CASE WHEN reception_total = 0 THEN 0 ELSE
                        round(100.0 * reception_perfect / reception_total)::integer END,
                    attack_error + attack_blocked + attack_points + defended,
                    attack_error, attack_blocked, attack_points, defended, block_points,
                    CASE WHEN i IN (3, 5) THEN 2 ELSE 0 END,
                    CASE WHEN i IN (7, 8, 11) THEN 3 ELSE 0 END,
                    CASE WHEN i = 1 THEN home_scores[s] / 2 ELSE 0 END, fixture_date);
            END LOOP;
        END LOOP;
    END LOOP;
END;
$demo$;
