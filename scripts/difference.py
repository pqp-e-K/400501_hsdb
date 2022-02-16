#!/usr/bin/env python3
import datetime
import json
import sys
import zipfile
import os
import csv

arguments = sys.argv[1:]
current_dir = os.path.dirname(__file__)

# Audiothek-Dump lesen und parsen
path = os.path.join(current_dir, f"../{arguments[0]}")

if zipfile.is_zipfile(path):
    archive = zipfile.ZipFile(path, 'r')
    content = archive.read('api.json')
    audiothek = json.loads(content)
else:
    with open(path) as file:
        audiothek = json.load(file)

items = audiothek["_embedded"]["mt:items"]
audiothek = {}

for item in items:
    clean_item = {
        "id": item["id"],
        "title": item["title"],
        "link": item["_links"]["mt:sharing"]["href"]
    }
    if not clean_item["id"] in audiothek:
        audiothek[clean_item["id"]] = []
    audiothek[clean_item["id"]].append(clean_item)

# verlinkte hsdb-audiothek-records aus hsdb-query-result parsen
hsdb = {}
hsdb_lines = 0
with open(os.path.join(current_dir, f"{arguments[1]}")) as file:
    for line in csv.reader(file, delimiter="\t"):
        hsdb_lines += 1
        print(line)
        # dukey	audiothek_id	audiothek_link	score	sorttit	sortaut	sortrfa	sortdat
        _record = {
            "dukey": line[0],
            "id": line[1],
            "link": line[2],
            "score": line[3],
            "title": line[4],
            "auth": line[5],
            "rfa": line[6],
            "date": line[7]
        }

        if not _record["id"] in hsdb:
            hsdb[_record["id"]] = []
        hsdb[_record["id"]].append(_record)

res = set(audiothek) - set(hsdb)
res = list(res)
_records = []
for _id in res:
    _record = audiothek[_id][0]
    _records.append(_record)
keys = _records[0].keys()
result_file_path = f"../hsdb_{datetime.datetime.now()}_not_found.tsv"
rows = 0
with open(os.path.join(current_dir, result_file_path), 'w') as file:
    w = csv.DictWriter(file, fieldnames=keys, delimiter='\t')
    w.writeheader()
    for r in _records:
        #print(r)
        #print(type(r))
        w.writerow(r)
        rows += 1
    file.flush()
    file.close()

print(f"Verlinkt: {hsdb_lines}")
print(f"Nicht verlinkt: {rows}")

