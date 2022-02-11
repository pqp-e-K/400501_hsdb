#!/usr/bin/env zsh
# Startet Mariadb-Container und mapped auf localhost:3306
# docker pull mariadb
docker run -p 3306:3306 --name hsdb-mariadb --env MARIADB_USER=hsdb-user --env MARIADB_PASSWORD=pass --env MARIADB_ROOT_PASSWORD=root  mariadb:latest