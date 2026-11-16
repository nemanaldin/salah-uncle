#!/usr/bin/env python3
"""Validate the actual shipped dua payload, including exact owner text and provenance gates."""
from pathlib import Path
import hashlib,json,unicodedata
ROOT=Path(__file__).resolve().parents[1]
BASE=ROOT/'app/src/main/assets/duas'
def normalize(value):
    return ''.join(c for c in unicodedata.normalize('NFKD',value) if not unicodedata.combining(c)).replace('ـ','').replace(' ','')
def validate():
    data=json.loads((BASE/'duas-v1.json').read_text());items={x['id']:x for x in data['items']}
    assert data['version']==1 and data['school']=='hanafi'
    assert len(items)==len(data['items']) and len(data['categories'])==7
    sections={x['id']:x for x in data['sections']};cats={x['id'] for x in data['categories']}
    for item in items.values():
        assert item['category'] in cats and item['id'] in sections[item['section']]['duaIds']
        assert all(k in item for k in ['titles','arabic','pronunciations','translations','sources','repeats','audio','reviewFields'])
        assert item['audio']=='' # No fabricated recordings or licensed audio was supplied.
        assert item['arabic'] or {'arabic','source','grade'}<=set(item['reviewFields'])
        if item['arabic'] and not item['ownerText']:
            assert item['sources'] and all(x['checked'] and x['grade'] in ['SAHIH','HASAN','QURAN'] and x['reference'] and x['url'].startswith('https://') for x in item['sources'])
        assert all(x['count']>0 and x['arabic'] in item['arabic'] for x in item['repeats'])
        for forbidden in ['وجهت وجهي','التحيات المباركات']:
            assert normalize(forbidden) not in normalize(item['arabic']),item['id']
    for section in sections.values():
        assert section['category'] in cats and all(i in items for i in section['duaIds'])
        assert len(section['duaIds'])==len(set(section['duaIds']))
    owner=json.loads((BASE/'owner-texts.json').read_text())
    for original in owner['duas']:
        item=items[original['id']]
        assert item['arabic']==original['arabic']
        assert hashlib.sha256(item['arabic'].encode()).hexdigest()==original['sha256']
        assert not item['reminder']
    assert 'grade' in items['owner-abu-umamah']['reviewFields']
    assert 'source' in items['owner-prostration']['reviewFields']
    assert not items['friday-seven']['arabic'] and 'grade' in items['friday-seven']['reviewFields']
    checks=json.loads((BASE/'source-checks.json').read_text())['extracts']
    assert {x['id'] for x in checks}=={x['id'] for x in items.values() if x['arabic'] and not x['ownerText']}
    for check in checks:assert check['sha256']==hashlib.sha256(items[check['id']]['arabic'].encode()).hexdigest()
    print(f"PASS: Hanafi-only dua catalog: {len(items)} topics, {sum(bool(x['arabic']) for x in items.values())} Arabic payloads; exact owner text, sources, grades, references and review gates checked.")
    return data
if __name__=='__main__':validate()
