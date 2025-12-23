# GeoNames city seed

This script downloads the GeoNames `cities15000` dataset and converts it into
`app/src/main/assets/cities_seed.csv` using ASCII English names and Korean
alternate names when available.

Note: the alternate names download is large and may take a few minutes.

Run:

```bash
python3 tools/geonames/build_cities_csv.py
```

Note: Replace the sample CSV in assets before release if you want full coverage.
