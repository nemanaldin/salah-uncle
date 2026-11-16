#!/usr/bin/env python3
"""Reject removed source references and unlabelled missing lesson provenance."""
from pathlib import Path
import json,re,unicodedata

ROOT=Path(__file__).resolve().parents[1]
def normalized(text):
    text=unicodedata.normalize('NFKD',text).casefold()
    return ''.join(c for c in text if not unicodedata.combining(c) and c not in '\u200c\u200d\u200e\u200f').replace('آ','ا')
REMOVED=re.compile(r'(?:erfanabad|irfanabad|عرفان[\s_-]*اباد)')
def verify():
    errors=[]
    for folder in [ROOT/'app/src/main',ROOT/'content']:
        for path in folder.rglob('*'):
            if path.is_file() and path.suffix in {'.json','.kt','.java','.xml','.txt','.md','.properties'}:
                for n,line in enumerate(path.read_text().splitlines(),1):
                    if REMOVED.search(normalized(line)):errors.append(f'{path.relative_to(ROOT)}:{n}: removed source reference')
    for path in (ROOT/'app/src/main/assets/content').glob('*.json'):
        for lesson in json.loads(path.read_text())['lessons']:
            if not lesson.get('source','').strip() and not lesson.get('sourceReviewRequired',False):
                errors.append(f'{lesson["id"]}: missing source without review status')
            for variant in lesson['variants']:
                for source in variant.get('sources',[]):
                    if not source.get('title','').strip() or not source.get('url','').startswith(('https://','http://')):
                        errors.append(f'{lesson["id"]}: incomplete source link')
    if errors:raise SystemExit('\n'.join(errors))
    print('PASS: removed source absent from app/content; missing provenance explicitly marked for review.')
if __name__=='__main__':verify()
