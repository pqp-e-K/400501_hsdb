#!/usr/bin/env zsh

# Importiert die Dumps in die Datenbank-Tabellen

import() {
	echo "$(date) import $1"
	docker exec -i hsdb-mariadb sh -c 'exec mysql -uroot -p"$MARIADB_ROOT_PASSWORD" --default-character-set=utf8mb4' < $1
	res=$?
	echo "$(date) finished importing $1"
	echo
	return $res
}

FOLDER_NAME="HSP-DB-dump"

# Haupttabelle___hs_du.sql um folgendes erweitern:
# CREATE DATABASE IF NOT EXISTS `hsdb`
#  CHARACTER SET = 'utf8'
#  COLLATE = 'utf8_unicode_ci';
# USE `hsdb`;
import ./$FOLDER_NAME/Haupttabelle___hs_du.sql &&
import ./$FOLDER_NAME/Hilfstabellen_mit_Wertelisten.sql &&
import ./$FOLDER_NAME/hs_suchdatum.sql &&
import ./$FOLDER_NAME/hs_suchzeile.sql
