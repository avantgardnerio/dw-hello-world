alter table job add status character varying;
alter table job add submit_time timestamp;
alter table job add start_time timestamp;
alter table job add end_time timestamp;
alter table job add external_id character varying;
alter table job add cancel_request_time timestamp;
alter table job add message character varying;