
-- Script to generate a database SGHARCHIV with users and schemas for a DEVelopment environment. 
-- For other environments, replace sgharchiv_dev with sgharchiv_test (or whatever) everywhere. 
-- Run it with a (temporary) super user of your choice (postgres or a dedicated such as sgharchiv).

CREATE DATABASE sgharchiv
  WITH ENCODING = 'UTF8';

-- Connect to the database sgharchiv while executing the following!

CREATE ROLE sgharchiv_dev_scis 
  LOGIN PASSWORD 'Merci5mitmachen'
  NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;

CREATE ROLE sgharchiv_dev_liferay 
  LOGIN PASSWORD 'Merci5mitmachen'
  NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;

CREATE SCHEMA AUTHORIZATION sgharchiv_dev_scis; -- for the "speleo & co" tables
CREATE SCHEMA AUTHORIZATION sgharchiv_dev_liferay; -- for the portlets tables

GRANT CONNECT, TEMP ON DATABASE sgharchiv TO sgharchiv_dev_scis;
GRANT CONNECT, TEMP ON DATABASE sgharchiv TO sgharchiv_dev_liferay;
GRANT ALL ON SCHEMA sgharchiv_dev_liferay TO sgharchiv_dev_scis;
GRANT ALL ON SCHEMA sgharchiv_dev_scis TO sgharchiv_dev_liferay;

-- Hint: to change the default schema for the current session:
--SET search_path = sgharchiv_dev_scis;
