In dieses Verzeichnis müssen die vier *.sql-Dumps abgelegt werden, damit der vorbereitete Import funktioniert.

`Haupttabelle___hs_du.sql` um Folgendes erweitern (zu Beginn des Skripts):

``` SQL
CREATE DATABASE IF NOT EXISTS `hsdb`
CHARACTER SET = 'utf8'
COLLATE = 'utf8_unicode_ci';
USE `hsdb`;
```

``` SQL 
SELECT DUKEY, SUBSTRING(REPLACE(VOLLINFO,CHAR(10),''),50,200) FROM hs_du LIMIT 100;
```