-- DBUPDATE-031-0.SQL

-- Insert a new setting for OCR recognition
insert into T_CONFIG (CFG_ID_C, CFG_VALUE_C) values ('OCR_ENABLED', 'true');

-- Update the database version
update T_CONFIG set CFG_VALUE_C = '31' where CFG_ID_C = 'DB_VERSION';
create table T_REGISTRATION_REQUEST (
  RGR_ID_C varchar(36) not null,
  RGR_USERNAME_C varchar(50) not null,
  RGR_EMAIL_C varchar(100) not null,
  RGR_PASSWORD_C varchar(100) not null,
  RGR_CREATEDATE_D timestamp not null,
  RGR_STATUS_C varchar(10) not null,
  RGR_IPADDRESS_C varchar(45),
  RGR_STATUSDATE_D timestamp,
  RGR_STATUSUSERID_C varchar(36),
  primary key (RGR_ID_C)
);

create index IDX_RGR_STATUS_C on T_REGISTRATION_REQUEST (RGR_STATUS_C);
create index IDX_RGR_USERNAME_C on T_REGISTRATION_REQUEST (RGR_USERNAME_C);
create index IDX_RGR_STATUSUSERID_C on T_REGISTRATION_REQUEST (RGR_STATUSUSERID_C);