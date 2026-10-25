#!/usr/bin/env python3
"""Compile source-compared templates; preserve Arabic recitations verbatim.
No network, model API, or runtime translation is used by the Android application.
"""
from pathlib import Path
import hashlib,json,re
ROOT=Path(__file__).resolve().parents[1]
SOURCE=ROOT/'app/src/main/assets/content/hanafi-fa-v1.json'
TEMPLATES=ROOT/'content/legacy-i18n'
LANGS=['en','ar','tr','ur','id','bn','ms','ps','fr']

def source_data():
    lessons=json.loads(SOURCE.read_text())['lessons']
    steps={s['id']:s for l in lessons for s in l['variants'][0]['steps']}
    passages={}
    for step in steps.values():
        for i,q in enumerate(re.findall('«([^»]+)»',step['body'])):passages[f'q_{step["id"]}_{i}']=q
    for i in range(1,13):
        s=steps[f'tasbihat{i}']['body']
        passages[f'a_tasbihat{i}']=s.split('ـ',1)[1].split('«',1)[0].strip()
    for i in range(3,12):
        s=steps[f'vuzu{i}']['body'];start=s.index('اَلَّلهُمَّ')
        passages[f'a_vuzu{i}']=s[start:]
    s=steps['vuzu1']['body'];start=s.index('اَعُوْذُ');end=s.index('، ميگوييم')
    passages['a_vuzu1']=s[start:end]
    return lessons,steps,passages

def compile_all():
    lessons,steps,passages=source_data()
    target=ROOT/'app/src/main/assets/translations';target.mkdir(exist_ok=True)
    digest=hashlib.sha256(SOURCE.read_bytes()).hexdigest()
    editorial=json.loads((TEMPLATES/'editorial.json').read_text())
    for key,text in editorial['faCorrections'].items():
        assert key in steps
        editorial['faCorrections'][key]=re.sub(r'\{\{([^}]+)\}\}',lambda m:passages[m[1]],text)
    (target/'editorial-v1.json').write_text(json.dumps(editorial,ensure_ascii=False,indent=2)+'\n')
    for language in LANGS:
        source=json.loads((TEMPLATES/(language+'.json')).read_text())
        assert set(source['steps'])==set(steps),(language,set(steps)-set(source['steps']))
        resolved={}
        for key,text in source['steps'].items():
            assert text.strip()
            used=re.findall(r'\{\{([^}]+)\}\}',text)
            for token in used:assert token in passages,(language,key,token)
            body=re.sub(r'\{\{([^}]+)\}\}',lambda m:passages[m[1]],text)
            assert '{{' not in body and body!=steps[key]['body'],(language,key)
            # Explicitly translated prose surrounds intentionally Arabic-only recitations.
            assert body.strip()
            resolved[key]=body
        result={'schemaVersion':1,'language':language,'sourceSha256':digest,
                'translationMethod':'Source-compared translation; recitations retained in Arabic. Independent native/specialist sign-off is not claimed.',
                'lessons':{l['id']:{s['id']:resolved[s['id']] for s in l['variants'][0]['steps']} for l in lessons}}
        (target/f'legacy-{language}-v1.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n')
        print(language,len(resolved),'steps')

if __name__=='__main__':compile_all()
