# GeoNames city seed

This script downloads the GeoNames `cities15000` dataset and converts it into
`app/src/main/assets/cities_seed.csv`.

Run:

```bash
python3 tools/geonames/build_cities_csv.py
```

Note: Replace the sample CSV in assets before release if you want full coverage.
