#!/usr/bin/env python3
"""Extract the pinned GeoNames snapshot without adding a Python runtime to Android.
Usage: pip download geonamescache==3.0.2 --no-deps -d /tmp/geography
       python scripts/build_city_catalog.py /tmp/geography/geonamescache-3.0.2-py3-none-any.whl
"""
from pathlib import Path
import gzip,hashlib,json,sys,zipfile
ROOT=Path(__file__).resolve().parents[1]
wheel=Path(sys.argv[1])
with zipfile.ZipFile(wheel) as z:
    raw=z.read('geonamescache/data/cities15000.json')
    data=json.loads(raw)
    rows=[]
    for c in sorted(data.values(),key=lambda x:x['geonameid']):
        # Retain searchable aliases for the supported scripts; CJK is outside this release's languages.
        aliases=[n for n in c['alternatenames'] if n!=c['name'] and all(ord(ch)<0x3000 for ch in n)]
        fields=[str(c['geonameid']),c['countrycode'],c['name'],str(c['latitude']),str(c['longitude']),c['timezone'],json.dumps(aliases,ensure_ascii=False,separators=(',',':'))]
        assert all('\t' not in s and '\n' not in s for s in fields)
        rows.append('\t'.join(fields))
    text=('\n'.join(rows)+'\n').encode()
    compressed=gzip.compress(text,compresslevel=9,mtime=0)
    target=ROOT/'app/src/main/assets/geography';target.mkdir(exist_ok=True)
    (target/'cities-v1.bin').write_bytes(compressed)
    metadata={'schemaVersion':1,'source':'GeoNames cities15000 via geonamescache 3.0.2','sourceUrl':'https://www.geonames.org/export/','packageUrl':'https://pypi.org/project/geonamescache/3.0.2/','licence':'CC BY 4.0','licenceUrl':'https://creativecommons.org/licenses/by/4.0/','wheelSha256':hashlib.sha256(wheel.read_bytes()).hexdigest(),'sourceSha256':hashlib.sha256(raw).hexdigest(),'catalogSha256':hashlib.sha256(compressed).hexdigest(),'cities':len(data),'countriesAndTerritories':len({c['countrycode'] for c in data.values()}),'uncompressedBytes':len(text),'compressedBytes':len(compressed),'note':'Snapshot, not live data. Cities over 15000 inhabitants and capitals, not every settlement. Original per-city IANA timezone preserved. Legacy local cities are merged at runtime.'}
    (target/'metadata.json').write_text(json.dumps(metadata,indent=2)+'\n')
    (target/'GEONAMESCACHE-MIT.txt').write_bytes(z.read('geonamescache-3.0.2.dist-info/licenses/LICENSE'))
    print(json.dumps(metadata,indent=2))
