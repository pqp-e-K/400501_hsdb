#!/usr/bin/env zsh

if [ -r ./import-hsdb-dump-to-mariadb.sh ]; then
    source ./import-hsdb-dump-to-mariadb.sh > /dev/null
else
    echo "ERROR ./import-hsdb-dump-to-mariadb.sh nicht gefunden. Exit 1"
    exit 1
fi

SCRIPT_HOME=$(cd "$(dirname "$0")" && pwd)
DUMP_DIR=$SCRIPT_HOME/../HSP-DB-dump
echo "Running from $SCRIPT_HOME"

if test -f "$DUMP_DIR"/HSP-DB-dump_Teil-1.zip; then
    echo "Merging dump-files..."
    unzip "$DUMP_DIR"/HSP-DB-dump_Teil-1.zip -d "$DUMP_DIR"
    if test -f "$DUMP_DIR"/HSP-DB-dump_Teil-2.zip; then
        unzip "$DUMP_DIR"/HSP-DB-dump_Teil-2.zip -d "$DUMP_DIR"
        cat "$DUMP_DIR"/Haupttabelle__hs_du__Teil-2.sql >> "$DUMP_DIR"/Haupttabelle__hs_du__Teil-1.sql
    fi
    mv "$DUMP_DIR"/Haupttabelle__hs_du__Teil-1.sql "$DUMP_DIR"/Haupttabelle_hs_du.sql
    rm "$DUMP_DIR"/Haupttabelle__hs_du__Teil-2.sql
    echo "Merge finished."
fi

DB_NAME="hsdb"
echo "Update Database: $DB_NAME"

for file in Haupttabelle_hs_du.sql hs_link_audiothek.sql #Hilfstabellen_mit_Wertelisten.sql  hs_suchdatum.sql hs_suchzeile.sql
do
    echo "$DUMP_DIR/$file"
    if test -f "$DUMP_DIR/$file"; then
        import "$DUMP_DIR/$file"
    else
        echo "$DUMP_DIR/$file nicht gefunden. Skipping..."
    fi
done