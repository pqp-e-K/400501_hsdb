#!/usr/bin/env zsh

query="SELECT a.dukey, a.audiothek_id, a.audiothek_link, a.score, b.sorttit,b.sortaut,b.sortrfa,b.sortdat
from hsdb.hs_link_audiothek a join hsdb.hs_du b on a.dukey = b.dukey
order by a.dukey ASC"

echo "$(date) INFO Frage verlinkte Datensätze in HSDB ab"
linked_records_filename="../linked-records-$(date +%Y-%m-%d-%T).tsv"
mariadb --host 127.0.0.1 -P 3306 --user root -proot -e "$query" > "$linked_records_filename"

echo "$(date) INFO Starte difference.py"

./difference.py "src/test/resources/api-examples/api.json.zip" "$linked_records_filename"