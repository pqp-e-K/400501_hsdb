#!/usr/bin/env python3

import json
import pandas
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
with open(os.path.join(current_dir, "../query-results/hsdb.tsv")) as file:
    for line in csv.reader(file, delimiter="\t"):
        _record = {"dukey": line[0], "title": line[1], "auth": line[2], "rfa": line[3], "date": line[4], "id": line[5],
                   "link": line[6]}
        if not _record["id"] in hsdb:
            hsdb[_record["id"]] = []
        hsdb[_record["id"]].append(_record)

hsdb_new = {}
with open(os.path.join(current_dir, "../query-results/hsdb_new.tsv")) as file:
    for line in csv.reader(file, delimiter="\t"):
        _record = {"dukey": line[0], "title": line[1], "auth": line[2], "rfa": line[3], "date": line[4], "id": line[5],
                   "link": line[6]}
        if not _record["id"] in hsdb_new:
            hsdb_new[_record["id"]] = []
        hsdb_new[_record["id"]].append(_record)

print(f"Audiothek: {len(audiothek)}, HSDB: {len(hsdb)}, HSDB_NEW: {len(hsdb_new)}")

old = set(audiothek.keys()) - set(hsdb.keys())
new = set(audiothek.keys()) - set(hsdb_new.keys())

res_old = {}
for key in old:
    res_old[key] = audiothek[key][0]
res_new = {}
for key in new:
    res_new[key] = audiothek[key][0]

for r in res_old:
    print(f"{r}{res_old[r]}")

print()
print()
print()
for r in res_new:
    print(f"{r}{res_new[r]}")
exit(0)

res_hsdb = {}
for key in old:
    if key in audiothek.keys():
        res_hsdb[key] = audiothek[key]

res_hsdb_new = {}
for key in new:
    if key in audiothek.keys():
        res_hsdb_new[key] = audiothek[key]

print(f"Audiothek: {len(audiothek)}, HSDB: {len(res_hsdb)}, HSDB_NEW: {len(res_hsdb_new)}")

#with open('os.path.join(current_dir, "../query-results/hsdb_new_not_found.tsv")', 'wb') as file:
#    w = csv.DictWriter(file,my_dict.keys())
#    w.writerows(my_dict)
#    f.close()
