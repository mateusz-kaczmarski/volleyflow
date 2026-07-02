do $$
begin
    if exists (
        select 1
        from information_schema.columns
        where table_schema = current_schema()
          and table_name = 'club'
          and column_name = 'club_status'
          and data_type = 'smallint'
    ) then
        alter table club drop constraint if exists club_club_status_check;

        alter table club
            alter column club_status type varchar(20)
            using case club_status
                when 0 then 'ACTIVE'
                when 1 then 'DEACTIVATED'
                when 2 then 'DELETED'
                else 'ACTIVE'
            end;
    end if;
end $$;
