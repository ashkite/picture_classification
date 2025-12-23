#!/usr/bin/env python3
import csv
import io
import sys
import urllib.request
import zipfile
from pathlib import Path

GEONAMES_URL = "https://download.geonames.org/export/dump/cities15000.zip"
OUTPUT_PATH = Path("app/src/main/assets/cities_seed.csv")

NAME_IDX = 1
LAT_IDX = 4
LON_IDX = 5
COUNTRY_IDX = 8


def main() -> int:
    print(f"Downloading {GEONAMES_URL} ...")
    with urllib.request.urlopen(GEONAMES_URL) as response:
        data = response.read()

    with zipfile.ZipFile(io.BytesIO(data)) as zf:
        with zf.open("cities15000.txt") as raw:
            lines = raw.read().decode("utf-8", errors="ignore").splitlines()

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT_PATH.open("w", newline="", encoding="utf-8") as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(["name_en", "name_ko", "country_code", "lat", "lon"])
        for line in lines:
            if not line.strip():
                continue
            parts = line.split("\t")
            if len(parts) <= COUNTRY_IDX:
                continue
            name = parts[NAME_IDX].strip()
            lat = parts[LAT_IDX].strip()
            lon = parts[LON_IDX].strip()
            country = parts[COUNTRY_IDX].strip()
            if not name or not lat or not lon or not country:
                continue
            writer.writerow([name, name, country, lat, lon])

    print(f"Wrote {OUTPUT_PATH}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
