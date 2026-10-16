#!/usr/bin/env python3
"""Validate offline locale coverage, catalog isolation, media, and legacy preservation."""
from pathlib import Path
import hashlib
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
    print(f'PASS: {len(LANGUAGES)} locales, {len(reference)} strings each; {len(lessons)} lessons; 8 guides fully localized; original 21 lessons/73 steps preserved.')

if __name__ == '__main__':
    verify()
