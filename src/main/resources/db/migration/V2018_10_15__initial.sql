create table job
(
	id serial not null
		constraint job_pkey
			primary key,
	name varchar
)
;

create unique index job_id_uindex
	on job (id)
;

