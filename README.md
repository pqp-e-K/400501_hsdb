# 400501_hsdb

## Modul für den Datenabgleich zwischen HSDB und ARD-Audiothek

### Installation / Update

1. In HSP-DB-dump die SQL/Zip-Dateien ablegen
2. scripts/update.sh ausführen

Beim Update werden standardmäßig nur die Haupttabelle sowie 
die hs_link_audiothek-Tabelle angelegt und befüllt.


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