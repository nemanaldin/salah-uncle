#!/usr/bin/env python3
"""Persian overlay checks; shared-language and old source gates are never relaxed."""
from pathlib import Path
import hashlib, json, re

ROOT = Path(__file__).resolve().parents[1]
BASE = ROOT / 'app/src/main/assets/duas'

def validate():
    data = json.loads((BASE / 'duas-fa-v1.json').read_text())
    shared = json.loads((BASE / 'duas-v1.json').read_text())
    assert data['version'] == 1 and data['language'] == 'fa' and data['school'] == 'hanafi'
    items = {x['id']: x for x in data['items']}
    assert len(items) == len(data['items']) and set(items) == {x['id'] for x in shared['items']}
    reads = {x['id']: x for x in json.loads((BASE / 'fa-source-reads-v1.json').read_text())['sources']}
    complete = 0
    for item in items.values():
        assert item['reviewStatus'] == 'pending_owner_review'
        assert item['kind'] in {'dua', 'evidence', 'practice'}
        assert not re.search(r'بازبینی|پیش.نویس|تأییدنشده|تأیید نشده', item['title'])
        if item['missingFields']:
            assert not item['reminder'], item['id']
            continue
        complete += 1
        assert item['meaning'].strip() and item['sources'] and item['evidenceIds'], item['id']
        if item['kind'] == 'practice':
            assert item['practice'].strip() and not item['reminder'], item['id']
        else:
            assert item['arabic'].strip() and item['pronunciation'] and all(x.strip() for x in item['pronunciation']), item['id']
        for source in item['sources']:
            assert source['checked'] and source['grade'] in {'SAHIH', 'HASAN', 'QURAN', 'FIQH', 'ATHAR'}, item['id']
            assert source['book'].strip() and source['reference'].strip() and source['url'].startswith('https://'), item['id']
            assert source['url'] in {reads[k]['url'] for k in item['evidenceIds']}, item['id']
        for key in item['evidenceIds']:
            read = reads[key]
            assert read['readAt'] == '2026-11-18' and read['webToolRead'] and read['locator'] and read['excerpt'], key
    for owner in json.loads((BASE / 'owner-texts.json').read_text())['duas']:
        assert items[owner['id']]['arabic'] == owner['arabic']
        assert hashlib.sha256(items[owner['id']]['arabic'].encode()).hexdigest() == owner['sha256']
    fixed = json.loads((ROOT / 'content/fa-completion-protected.json').read_text())
    for path, sha in fixed.items():
        assert hashlib.sha256((ROOT / path).read_bytes()).hexdigest() == sha, ('Protected data changed', path)
    import xml.etree.ElementTree as ET
    values = ET.parse(ROOT / 'app/src/main/res/values/ui_strings.xml').getroot()
    for element in values:
        assert not re.search(r'بازبینی|پیش.نویس|تأییدنشده|تأیید نشده', ''.join(element.itertext())), element.get('name')
    print(f'PASS: {len(items)} Persian entries; {complete} field-complete; internal owner review retained; strict sources and non-Persian integrity checked.')
    return data

if __name__ == '__main__':
    validate()
