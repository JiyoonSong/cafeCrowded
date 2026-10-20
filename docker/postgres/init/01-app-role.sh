#!/bin/sh
# Creates the application-only role (no superuser, no createdb/createrole) and
# makes it the owner of the application database so Flyway can migrate.
# Runs only on first initialisation of an empty data volume.
set -eu

psql -v ON_ERROR_STOP=1 -v app_user="$APP_DB_USER" -v app_password="$APP_DB_PASSWORD" \
     -v db_name="$POSTGRES_DB" --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'SQL'
CREATE ROLE :"app_user" LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE PASSWORD :'app_password';
REVOKE ALL ON DATABASE :"db_name" FROM PUBLIC;
ALTER DATABASE :"db_name" OWNER TO :"app_user";
SQL
