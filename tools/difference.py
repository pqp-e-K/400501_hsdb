#!/usr/bin/env python3

import json
import sys

import os
import csv

current_dir = os.path.dirname(__file__)

path = os.path.join(current_dir, "../query-results/audiothek.json")
with open(path) as apipath:
    audiothek = json.load(apipath)
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

hsdb = {}
with open(os.path.join(current_dir, "../query-results/new.tsv")) as file:
    for line in csv.reader(file, delimiter="\t"):
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

print(f"Audiothek: {len(audiothek)}, HSDB: {len(hsdb)}")

res = set(audiothek) - set(hsdb)
res = list(res)
_records = []
for _id in res:
    _record = audiothek[_id][0]
    _records.append(_record)
keys = _records[0].keys()
with open(os.path.join(current_dir, "../query-results/hsdb_20220202_not_found.tsv"), 'w') as file:
    w = csv.DictWriter(file, fieldnames=keys, delimiter='\t')
    w.writeheader()
    for r in _records:
        print(r)
        print(type(r))
        w.writerow(r)
    file.close()
