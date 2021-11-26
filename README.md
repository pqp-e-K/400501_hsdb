# 400501_hsdb

## Modul für den Datenabgleich zwischen HSDB und ARD-Audiothek

### Installation
1. ```./install-mariadb-docker.sh```
2. ```./hsdb-mariadb-docker-start.sh```
3. Die vier HSDB-Dump-Files im Verzeichnis ```./HSP-DB-dump``` ablegen
4. ```./import-hsdb-dump-to-mariadb.sh```

### DB-Connect
```
./connect-to-mariadb.sh 
```
oder
``` 
mariadb --host 127.0.0.1 -P <port> --user <user> -p
```

Nützliche SQL-Statements:
`````roomsql
SELECT DUKEY,SUBSTRING(REPLACE(VOLLINFO,CHAR(10),''),50,200) FROM hs_du LIMIT 100;
`````