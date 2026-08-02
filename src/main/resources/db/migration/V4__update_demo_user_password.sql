-- Demo login: demo.owner@example.com / demo
update user_account
set password_hash = '$2a$10$1e8LQ8txy6SMHyQSZF3hQODrKJTRwC77fSPoXOiOc.Zw9MUp3RtwS',
    updated_at = now()
where email = 'demo.owner@example.com';
