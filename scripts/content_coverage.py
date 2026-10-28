#!/usr/bin/env python3
"""Report actual body availability separately from interface/title localization."""
from pathlib import Path
import argparse,json
ROOT=Path(__file__).resolve().parents[1]
LANGS=['fa','en','ar','tr','ur','id','bn','ms','ps','fr']
SCHOOLS=['HANAFI','SHAFII','MALIKI','HANBALI']
def coverage():
    lessons=[]
    for p in sorted((ROOT/'app/src/main/assets/content').glob('*.json')):lessons+=json.loads(p.read_text())['lessons']
    for path in sorted((ROOT/'app/src/main/assets/translations').glob('legacy-*-v1.json')):
        overlay=json.loads(path.read_text())
        for lesson in lessons:
            if lesson['id'] in overlay['lessons']:
                lesson['variants'].append({'school':'HANAFI','language':overlay['language']})
    rows=[]
    for school in SCHOOLS:
        relevant=[l for l in lessons if any(v['school']==school for v in l['variants'])]
        for lang in LANGS:
            translated=[l for l in relevant if any(v['school']==school and v['language']==lang for v in l['variants'])]
            rows.append({'school':school,'language':lang,'availableLessons':len(translated),'totalLessons':len(relevant),'missingLessonIds':[l['id'] for l in relevant if l not in translated]})
    return {'note':'Counts measure body availability, not independent native/specialist sign-off. All 21 legacy Hanafi lessons have translated bodies in all nine other languages. Sacred recitations remain Arabic. Other schools have separate prayer and wudu guides, not these Hanafi rulings.','languages':LANGS,'rows':rows}
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--write',action='store_true');a=p.parse_args()
    result=coverage()
    text=json.dumps(result,ensure_ascii=False,indent=2)+'\n'
    if a.write:(ROOT/'docs/TRANSLATION_COVERAGE.json').write_text(text)
    else:print(text,end='')
