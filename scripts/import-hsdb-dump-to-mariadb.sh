#!/usr/bin/env zsh

# Importiert die Dumps in die Datenbank-Tabellen

import() {
	echo "$(date) import $1"

	ddl=$(cat ./create_database.sql)
	ddl="${ddl}\n\n$(cat "$1")"

	echo "$ddl" | docker exec -i hsdb-mariadb sh -c 'exec mysql -uroot -p"$MARIADB_ROOT_PASSWORD" $DATABASE --default-character-set=utf8mb4'

	res=$?
	echo "$(date) finished importing $1"
	echo
	return $res
}

### create database
#docker exec -i hsdb-mariadb sh -c 'exec mysql -uroot -p"$MARIADB_ROOT_PASSWORD" --default-character-set=utf8mb4' < "./$FOLDER_NAME/create_database.sql"

### import
#import ./$FOLDER_NAME/Haupttabelle_hs_du.sql &&
#import ./$FOLDER_NAME/Hilfstabellen_mit_Wertelisten.sql &&
#import ./$FOLDER_NAME/hs_suchdatum.sql &&
#import ./$FOLDER_NAME/hs_suchzeile.sql
#import ./$FOLDER_NAME/hs_link_audiothek.sql

export -f import