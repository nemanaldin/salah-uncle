# Offline world city catalogue

The existing 279 city records in Iran, Afghanistan and Tajikistan are retained with their original IDs and coordinates. Saved locations are not migrated or silently changed. A separate GeoNames snapshot adds 34,006 cities across 244 countries and territories. Some large legacy cities also have a GeoNames record; the catalogue therefore has 34,285 selectable records, not 34,285 unique settlements.

The UI supports country filtering followed by city search, and also global city search. Country names use Android locale data. City names are proper place names from the source; aliases support searches in additional scripts. Search strips diacritics, normalizes Persian/Arabic letter variants and Turkish dotted I, runs off the UI thread after a short debounce, and returns at most 100 rows at once. Coordinates and each city's own IANA zone flow into the existing prayer engine. The calculation formula, GPS permissions and manual location persistence are retained.

## Options and measured size

| Choice | Size and operation | Tradeoff |
| --- | --- | --- |
| Full source snapshot | 16,670,875 bytes of JSON, with population/metadata and all aliases | More fields than the app needs |
| Selected offline catalogue | 6,572,599 bytes TSV; **2,503,892 bytes gzip** | Offline search, no server/key or runtime network; updates require a new snapshot |
| External search API | Depends on service, request and cache | Internet, privacy, limits, API credentials and continued service maintenance |
| Hybrid | Offline catalogue plus an optional service | More coverage for small settlements, but the same external-service obligations |

The selected offline data fits this release without introducing a backend or API account. This is a cities15000 snapshot (larger cities and capitals), **not every village or a live guarantee of completeness**. Country/territory counts follow GeoNames and are not an editorial sovereignty classification. Users can still select their GPS coordinates outside the city list.

The compressed asset itself is about 2.39 MiB. The final release APK comparison is in the generated build report; do not equate source JSON size with APK growth. The unchanged local 2.4 baseline release at `f0ffb4a` measured 7,579,045 bytes. APK ZIP entry sizes and actual before/after bytes are recorded separately.

## Source, licence and reproducibility

- Original data: [GeoNames](https://www.geonames.org/export/), [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/).
- Distribution snapshot: [geonamescache 3.0.2](https://pypi.org/project/geonamescache/3.0.2/), its [upstream repository](https://github.com/yaph/geonamescache) and MIT notice are included. No Python dependency is added to the Android app.
- `app/src/main/assets/geography/metadata.json` records the wheel/source/output SHA256 hashes and actual row/byte counts. `GEONAMESCACHE-MIT.txt` retains the distribution's notice. GeoNames attribution is visible in the location picker and About page.
- `scripts/build_city_catalog.py` reproducibly strips unused fields and unsupported-script aliases and uses deterministic gzip. The gzip stream uses a `.bin` filename because Android packaging expands and renames assets ending in `.gz`; the reader deliberately opens it with GZIPInputStream. `scripts/verify_content.py` checks IDs, coordinates, timezones, licence metadata and the output digest.
- The snapshot's retrieval/package version is known; the upstream per-record modification dates are not included in geonamescache. A fresh retrieval is not claimed to make every record current.

Rebuild the asset with:

```sh
python -m pip download geonamescache==3.0.2 --no-deps -d /tmp/geography
python scripts/build_city_catalog.py /tmp/geography/geonamescache-3.0.2-py3-none-any.whl
python scripts/verify_content.py
```

## Older Android timezone databases

The first API 34 regression run detected that its tzdb does not recognize `America/Coyhaique`. [IANA 2025b](https://data.iana.org/time-zones/tzdb-2025b/NEWS) introduced this ID for Aysén's permanent UTC−03 from March 2025. The source row retains its IANA ID. For current/upcoming prayer calculations only, selecting it on a device without that ID saves the documented fixed `-03:00` offset; devices that know the ID keep it. This is not a general historical-zone conversion or a guessed country offset. No other unknown ID is silently substituted. Tests compare June and December 2026 against the current IANA zone. Other time-law changes remain dependent on the device timezone database being current.
