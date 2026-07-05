alter table club drop constraint if exists club_name_key;

create unique index if not exists ux_club_active_name
    on club (name)
    where club_status = 'ACTIVE';
