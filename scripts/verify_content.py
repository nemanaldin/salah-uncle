#!/usr/bin/env python3
"""Validate offline locale coverage, catalog isolation, media, and legacy preservation."""
from pathlib import Path
import hashlib
import gzip
from zoneinfo import ZoneInfo
import json
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'app/src/main/res'
ASSETS = ROOT / 'app/src/main/assets'
LANGUAGES = {'fa', 'en', 'ar', 'tr', 'ur', 'id', 'bn', 'ms', 'ps', 'fr'}
SCHOOLS = {'HANAFI', 'SHAFII', 'MALIKI', 'HANBALI'}

def verify():
    config = ET.parse(RES / 'xml/locales_config.xml').getroot()
    assert {x.get('{http://schemas.android.com/apk/res/android}name') for x in config} == LANGUAGES
    strings = {}
    for language in sorted(LANGUAGES):
        folder = 'values' if language == 'fa' else 'values-' + ('in' if language == 'id' else language)
        elements = list(ET.parse(RES / folder / 'ui_strings.xml').getroot())
        values = {e.get('name'): e.text for e in elements}
        assert len(elements) == len(values), f'Duplicate strings: {language}'
        assert all(v and v.strip() for v in values.values()), f'Empty strings: {language}'
        strings[language] = values
    reference = strings['en']
    for language, values in strings.items():
        assert values.keys() == reference.keys(), f'Incomplete translation: {language}'
        for key, text in values.items():
            assert sorted(re.findall(r'%\d+\$[dsf]', text)) == sorted(re.findall(r'%\d+\$[dsf]', reference[key])), (language, key)

    lessons = []
    for path in sorted((ASSETS / 'content').glob('*.json')):
        catalog = json.loads(path.read_text())
        assert catalog['schemaVersion'] == 1, path.name
        lessons.extend(catalog['lessons'])
    by_id = {lesson['id']: lesson for lesson in lessons}
    assert len(by_id) == len(lessons), 'Duplicate lesson IDs'
    for lesson in lessons:
        variants = lesson['variants']
        assert len({(v['school'], v['language']) for v in variants}) == len(variants), lesson['id']
        for variant in variants:
            assert variant['school'] in SCHOOLS, (lesson['id'], variant['school'])
            assert variant['language'] in LANGUAGES
            steps = variant['steps']
            assert steps and len({s['id'] for s in steps}) == len(steps), lesson['id']
            for step in steps:
                assert step['body'].strip() and '{school}' not in step['body'], (lesson['id'], step['id'])
                if step.get('audio'):
                    assert list((RES / 'raw').glob(step['audio'] + '.*')), step['audio']
                for image in step['illustrations']:
                    assert list((RES / 'drawable').glob(image + '.*')), image

    for school in SCHOOLS:
        for prefix, step_count in [('prayer', 10), ('purification', 4)]:
            lesson = by_id[f'{prefix}-{school.lower()}']
            assert set(lesson['titles']) == LANGUAGES
            variants = lesson['variants']
            assert {v['school'] for v in variants} == {school}, lesson['id']
            assert {v['language'] for v in variants} == LANGUAGES, lesson['id']
            step_ids = [s['id'] for s in variants[0]['steps']]
            assert len(step_ids) == step_count
            for variant in variants:
                assert [s['id'] for s in variant['steps']] == step_ids
                assert variant['sources'] and all(s['url'].startswith(('https://', 'http://')) for s in variant['sources'])
    legacy = json.loads((ASSETS / 'content/hanafi-fa-v1.json').read_text())['lessons']
    original = {l['id']: [v for v in l['variants'] if v['language'] == 'fa'] for l in legacy}
    assert len(original) == 21
    assert sum(len(v['steps']) for variants in original.values() for v in variants) == 73
    # Digest of original Persian variants at e43232a, before adding localized titles.
    digest = hashlib.sha256(json.dumps(original, ensure_ascii=False, sort_keys=True, separators=(',', ':')).encode()).hexdigest()
    assert digest == '06cb69865525803103375873035ddb15dc4c068d27739bd8c2feb532cc7eac81', 'Original Hanafi text or media changed'
    from build_lesson_translations import source_data
    _, original_steps, passages = source_data()
    source_digest = hashlib.sha256((ASSETS/'content/hanafi-fa-v1.json').read_bytes()).hexdigest()
    for language in LANGUAGES - {'fa'}:
        compiled = json.loads((ASSETS/f'translations/legacy-{language}-v1.json').read_text())
        template = json.loads((ROOT/f'content/legacy-i18n/{language}.json').read_text())['steps']
        assert compiled['language'] == language and compiled['sourceSha256'] == source_digest
        assert set(compiled['lessons']) == set(original)
        assert set(template) == set(original_steps)
        expected_tokens=json.loads((ROOT/'content/legacy-i18n/en.json').read_text())['steps']
        for key,text in template.items():
            assert re.findall(r'\{\{.*?\}\}',text)==re.findall(r'\{\{.*?\}\}',expected_tokens[key]), (language,key,'Arabic passages differ')
        for lesson_id, bodies in compiled['lessons'].items():
            assert set(bodies) == {x['id'] for x in original[lesson_id][0]['steps']}
            for key, body in bodies.items():
                expected = re.sub(r'\{\{([^}]+)\}\}', lambda m: passages[m[1]], template[key])
                assert body == expected and body != original_steps[key]['body']
                assert '{{' not in body
        # A translated explanation must precede every retained Arabic-only recitation.
        assert all(re.search(r'[\u0600-\u06ff\u0980-\u09ffA-Za-z]', text.split('{{')[0]) for text in template.values())
    editorial = json.loads((ASSETS/'translations/editorial-v1.json').read_text())
    assert set(editorial['faCorrections']) <= set(original_steps)
    assert set(editorial['titleOverrides']['signs']) == LANGUAGES
    heritage = json.loads((ROOT/'docs/HERITAGE_IMAGES.json').read_text())
    for entry in heritage['files']:
        image = (RES/'drawable-nodpi'/entry['file']).read_bytes()
        assert len(image) == entry['bytes'] and hashlib.sha256(image).hexdigest() == entry['sha256']
    remembrance = by_id['wudu-remembrance']
    assert len(remembrance['variants']) == 40
    assert {(v['school'], v['language']) for v in remembrance['variants']} == {(school, language) for school in SCHOOLS for language in LANGUAGES}
    for variant in remembrance['variants']:
        assert [x['id'] for x in variant['steps']] == ['after-wudu', 'shared-source']
        assert variant['sources'][0]['url'] == 'https://sunnah.com/muslim:234b'
    geography = ASSETS / 'geography'
    metadata = json.loads((geography / 'metadata.json').read_text())
    compressed = (geography / 'cities-v1.bin').read_bytes()
    assert hashlib.sha256(compressed).hexdigest() == metadata['catalogSha256']
    rows = [line.split('\t') for line in gzip.decompress(compressed).decode().splitlines()]
    assert len(rows) == metadata['cities'] == 34006
    assert len({r[0] for r in rows}) == len(rows)
    assert len({r[1] for r in rows}) == metadata['countriesAndTerritories'] == 244
    for row in rows:
        assert len(row) == 7 and len(row[1]) == 2 and row[2].strip()
        assert -90 <= float(row[3]) <= 90 and -180 <= float(row[4]) <= 180
        ZoneInfo(row[5])
        assert isinstance(json.loads(row[6]), list)
    place_names = json.loads((geography/'place-names-v1.json').read_text())
    old_places = json.loads((ASSETS/'locations.json').read_text())
    expected_ids = {f'{country}/{province}/{name}' for country, provinces in old_places.items() for province, places in provinces.items() for name in places}
    assert set(place_names['legacy']) == expected_ids and len(expected_ids) == 279
    for value in place_names['legacy'].values():
        assert value['en'].strip() and value['provinceEn'].strip()
        assert not re.search(r'[\u0600-\u06ff]', value['en'] + value['provinceEn'])
    for value in place_names['common'].values(): assert set(value) == LANGUAGES
    for entry in json.loads((ROOT/'docs/PRAYER_ARTWORK.json').read_text())['files']:
        raw = (RES/'drawable-nodpi'/entry['file']).read_bytes()
        assert len(raw) == entry['bytes'] and hashlib.sha256(raw).hexdigest() == entry['sha256']
    from content_coverage import coverage
    assert json.loads((ROOT/'docs/TRANSLATION_COVERAGE.json').read_text()) == coverage(), 'Stale coverage report'
    print(f'PASS: {len(LANGUAGES)} locales, {len(reference)} strings each; {len(lessons)} lessons; 8 localized guides and shared wudu remembrance; original 21 lessons/73 steps preserved.')
    print(f'PASS: {len(rows)} offline world cities / 244 countries and territories; coordinates, zones and digest checked; legacy city IDs retained.')
    print('PASS: all 21 legacy lessons / 73 steps have nine translated body variants; original recitations preserved. Independent native/specialist sign-off remains pending.')

if __name__ == '__main__':
    verify()
