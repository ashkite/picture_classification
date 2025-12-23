#!/usr/bin/env python3
import csv
import sys
import urllib.request
import zipfile
from pathlib import Path

CITIES_URL = "https://download.geonames.org/export/dump/cities15000.zip"
ALT_NAMES_URL = "https://download.geonames.org/export/dump/alternateNamesV2.zip"
OUTPUT_PATH = Path("app/src/main/assets/cities_seed.csv")
WORK_DIR = Path("/tmp/geonames")

GEONAME_ID_IDX = 0
NAME_IDX = 1
ASCIINAME_IDX = 2
LAT_IDX = 4
LON_IDX = 5
COUNTRY_IDX = 8


def download(url: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    if dest.exists():
        return
    print(f"Downloading {url} ...")
    with urllib.request.urlopen(url) as response, dest.open("wb") as out:
        while True:
            chunk = response.read(1024 * 1024)
            if not chunk:
                break
            out.write(chunk)


def load_cities(path: Path) -> tuple[list[dict], set[str]]:
    cities = []
    city_ids: set[str] = set()
    with zipfile.ZipFile(path) as zf:
        with zf.open("cities15000.txt") as raw:
            for line in raw:
                decoded = line.decode("utf-8", errors="ignore").strip()
                if not decoded:
                    continue
                parts = decoded.split("\t")
                if len(parts) <= COUNTRY_IDX:
                    continue
                geoname_id = parts[GEONAME_ID_IDX].strip()
                name_en = parts[ASCIINAME_IDX].strip() or parts[NAME_IDX].strip()
                name_en = name_en.encode("ascii", errors="ignore").decode("ascii")
                lat = parts[LAT_IDX].strip()
                lon = parts[LON_IDX].strip()
                country = parts[COUNTRY_IDX].strip()
                if not geoname_id or not name_en or not lat or not lon or not country:
                    continue
                cities.append(
                    {
                        "geoname_id": geoname_id,
                        "name_en": name_en,
                        "lat": lat,
                        "lon": lon,
                        "country": country,
                    }
                )
                city_ids.add(geoname_id)
    return cities, city_ids


def load_korean_names(path: Path, city_ids: set[str]) -> dict[str, str]:
    korean = {}
    with zipfile.ZipFile(path) as zf:
        with zf.open("alternateNamesV2.txt") as raw:
            for line in raw:
                decoded = line.decode("utf-8", errors="ignore").strip()
                if not decoded:
                    continue
                parts = decoded.split("\t")
                if len(parts) < 4:
                    continue
                geoname_id = parts[1].strip()
                if geoname_id not in city_ids:
                    continue
                language = parts[2].strip()
                if language != "ko":
                    continue
                name_ko = parts[3].strip()
                if not name_ko:
                    continue
                is_preferred = parts[4].strip() == "1" if len(parts) > 4 else False
                if geoname_id not in korean or is_preferred:
                    korean[geoname_id] = name_ko
    return korean


def main() -> int:
    WORK_DIR.mkdir(parents=True, exist_ok=True)
    cities_zip = WORK_DIR / "cities15000.zip"
    alt_zip = WORK_DIR / "alternateNamesV2.zip"
    download(CITIES_URL, cities_zip)
    download(ALT_NAMES_URL, alt_zip)

    print("Parsing city list ...")
    cities, city_ids = load_cities(cities_zip)
    print("Parsing Korean alternate names ...")
    korean_names = load_korean_names(alt_zip, city_ids)

    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    with OUTPUT_PATH.open("w", newline="", encoding="utf-8") as csvfile:
        writer = csv.writer(csvfile)
        writer.writerow(["name_en", "name_ko", "country_code", "lat", "lon"])
        for city in cities:
            name_ko = korean_names.get(city["geoname_id"], city["name_en"])
            writer.writerow([city["name_en"], name_ko, city["country"], city["lat"], city["lon"]])

    print(f"Wrote {OUTPUT_PATH}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
