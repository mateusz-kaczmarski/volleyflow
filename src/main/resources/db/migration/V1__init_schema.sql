create table user_account (
    id bigserial primary key,
    external_id uuid not null unique,
    email varchar(255) not null unique,
    password_hash varchar(255) not null,
    phone varchar(255) unique,
    status varchar(20) not null,
    email_verified boolean not null,
    last_login_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    version integer not null default 0
);

create table person_profile (
    id bigserial primary key,
    external_id uuid not null unique,
    user_account_id bigint unique references user_account(id),
    first_name varchar(80) not null,
    last_name varchar(80) not null,
    display_name varchar(120),
    jump_cm integer,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    version integer not null default 0
);

create table club (
    id bigserial primary key,
    external_id uuid not null unique,
    name varchar(120) not null unique,
    avatar varchar(255),
    description varchar(1024),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    club_status varchar(20) not null,
    version integer not null default 0
);

create table club_membership (
    id bigserial primary key,
    external_id uuid not null unique,
    club_id bigint not null references club(id),
    person_profile_id bigint not null references person_profile(id),
    role varchar(20) not null,
    shirt_number integer,
    active boolean not null,
    active_from date,
    active_to date,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    version integer not null default 0
);

create table club_membership_position (
    club_membership_id bigint not null references club_membership(id),
    position varchar(30) not null
);

create index idx_club_membership_club_id on club_membership(club_id);
create index idx_club_membership_person_profile_id on club_membership(person_profile_id);
create index idx_club_membership_position_membership_id on club_membership_position(club_membership_id);

insert into user_account (
    id, external_id, email, password_hash, phone, status, email_verified, last_login_at, created_at, updated_at, version
) values (
    1,
    '00000000-0000-0000-0000-000000000001',
    'demo.owner@example.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    null,
    'ACTIVE',
    true,
    null,
    now(),
    now(),
    0
);

insert into person_profile (
    id, external_id, user_account_id, first_name, last_name, display_name, jump_cm, created_at, updated_at, version
) values
    (1, '00000000-0000-0000-0000-000000000101', 1, 'Demo', 'Owner', 'Demo Owner', null, now(), now(), 0),
    (2, '00000000-0000-0000-0000-000000000102', null, 'Player', 'One', 'Player One', null, now(), now(), 0),
    (3, '00000000-0000-0000-0000-000000000103', null, 'Player', 'Two', 'Player Two', null, now(), now(), 0),
    (4, '00000000-0000-0000-0000-000000000104', null, 'Player', 'Three', 'Player Three', null, now(), now(), 0),
    (5, '00000000-0000-0000-0000-000000000105', null, 'Player', 'Four', 'Player Four', null, now(), now(), 0),
    (6, '00000000-0000-0000-0000-000000000106', null, 'Player', 'Five', 'Player Five', null, now(), now(), 0),
    (7, '00000000-0000-0000-0000-000000000107', null, 'Player', 'Six', 'Player Six', null, now(), now(), 0),
    (8, '00000000-0000-0000-0000-000000000108', null, 'Player', 'Seven', 'Player Seven', null, now(), now(), 0),
    (9, '00000000-0000-0000-0000-000000000109', null, 'Player', 'Eight', 'Player Eight', null, now(), now(), 0);

insert into club (
    id, external_id, name, avatar, description, created_at, updated_at, club_status, version
) values (
    1,
    '00000000-0000-0000-0000-000000000201',
    'Demo Club',
    'demo-club.png',
    'Demo volleyball club for local development',
    now(),
    now(),
    'ACTIVE',
    0
);

insert into club_membership (
    id, external_id, club_id, person_profile_id, role, shirt_number, active, active_from, active_to, created_at, updated_at, version
) values
    (1, '00000000-0000-0000-0000-000000000301', 1, 1, 'OWNER', null, true, null, null, now(), now(), 0),
    (2, '00000000-0000-0000-0000-000000000302', 1, 2, 'PLAYER', 6, true, '2024-09-01', '2025-06-30', now(), now(), 0),
    (3, '00000000-0000-0000-0000-000000000303', 1, 3, 'PLAYER', 2, true, '2024-09-01', '2025-06-30', now(), now(), 0),
    (4, '00000000-0000-0000-0000-000000000304', 1, 4, 'PLAYER', 97, true, '2024-09-01', '2025-06-30', now(), now(), 0),
    (5, '00000000-0000-0000-0000-000000000305', 1, 5, 'PLAYER', 3, true, '2024-09-01', '2025-06-30', now(), now(), 0),
    (6, '00000000-0000-0000-0000-000000000306', 1, 6, 'PLAYER', 10, true, '2024-09-01', '2025-06-30', now(), now(), 0),
    (7, '00000000-0000-0000-0000-000000000307', 1, 7, 'PLAYER', 9, true, '2024-09-01', '2025-06-30', now(), now(), 0),
    (8, '00000000-0000-0000-0000-000000000308', 1, 8, 'PLAYER', 13, true, '2024-09-01', '2025-06-30', now(), now(), 0),
    (9, '00000000-0000-0000-0000-000000000309', 1, 9, 'PLAYER', 26, true, '2024-09-01', '2025-06-30', now(), now(), 0);

insert into club_membership_position (club_membership_id, position) values
    (2, 'SETTER'),
    (3, 'MIDDLE_BLOCKER'),
    (4, 'MIDDLE_BLOCKER'),
    (5, 'OPPOSITE'),
    (6, 'OUTSIDE_HITTER'),
    (7, 'OUTSIDE_HITTER'),
    (8, 'LIBERO'),
    (9, 'MIDDLE_BLOCKER');

select setval('user_account_id_seq', (select max(id) from user_account));
select setval('person_profile_id_seq', (select max(id) from person_profile));
select setval('club_id_seq', (select max(id) from club));
select setval('club_membership_id_seq', (select max(id) from club_membership));
