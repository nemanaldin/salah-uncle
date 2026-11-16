#!/usr/bin/env python3
"""Verify the small offline OFL font bundle without a runtime font download."""
from pathlib import Path
import hashlib,json
ROOT=Path(__file__).resolve().parents[1]
def verify():
    folder=ROOT/'app/src/main/assets/licenses/fonts'
    d=json.loads((folder/'manifest.json').read_text())
    assert d['schemaVersion']==1 and 3<=len(d['families'])<=5
    assert len({f['name'] for f in d['families']})==len(d['families'])
    added=0
    for f in d['families']:
        raw=(ROOT/'app/src/main/res/font'/f['file']).read_bytes()
        assert len(raw)==f['bytes'] and hashlib.sha256(raw).hexdigest()==f['sha256']
        assert raw[:4] in [b'\x00\x01\x00\x00',b'OTTO']
        license=(folder/f['licenseFile']).read_text()
        assert 'SIL OPEN FONT LICENSE' in license and 'Version 1.1' in license
        assert f['license']=='SIL OFL 1.1' and f['modified'] is False
        if f['name']!='Vazirmatn':added+=len(raw)
    assert added<2*1024*1024,'Font additions exceed the small offline bundle budget'
    print(f'PASS: {len(d["families"])} bundled OFL families; {added} new font bytes; files/licenses/digests checked.')
if __name__=='__main__':verify()
