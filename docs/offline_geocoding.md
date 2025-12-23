# Offline Geocoding Plan

Goal: map GPS coordinates to a city/country on-device without network.

## Data source
- GeoNames cities15000
- Fields: name, countryCode, latitude, longitude, population

## Asset import (MVP)
- Asset file: `app/src/main/assets/cities_seed.csv`
- CSV columns: `name_en,name_ko,country_code,lat,lon`
- Seeder runs once on app start via WorkManager
- Replace the sample CSV with a full dataset for production

## Build helper
- `tools/geonames/build_cities_csv.py` downloads GeoNames cities15000 and rewrites the CSV (ASCII names)

## Storage
- Preprocess into SQLite (or Room import) at build time
- Columns: id, name_ko, name_en, country_code, lat, lon, geohash
- Index: geohash, country_code

## Query flow
1) Convert (lat, lon) to geohash prefix (precision 5-6)
2) Query candidate cities by matching geohash prefix
3) Compute distance to candidates (Haversine)
4) Pick nearest within threshold (e.g., 50km)

## Localization
- Prefer Korean display name if available
- Fallback to English name

## Missing location
- If no GPS or no city match, set hasLocation=false
- Group under "Location Unknown" and date

## Future optimization
- KD-tree or R-tree for faster nearest lookup
- Per-country caches for large datasets
