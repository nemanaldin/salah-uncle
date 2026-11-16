#!/usr/bin/env python3
"""Export the actual shipped pending fields without filling or approving any religious text."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
LANGS=['fa','en','ar','tr','ur','id','bn','ms','ps','fr']
data=json.loads((ROOT/'app/src/main/assets/duas/duas-v1.json').read_text())
records=[]
for item in data['items']:
    records.append(dict(id=item['id'],title=item['titles']['fa'],guidance=item['guidance'],
        reviewStatus='needs_review' if item['reviewFields'] else 'not_marked_approved',
        flaggedFields=item['reviewFields'],arabicEmpty=not bool(item['arabic']),
        sourceEmpty=not item['sources'],sourceOrGradePending=any(k in item['reviewFields'] for k in ['source','grade']),
        translationsPresent=[l for l in LANGS if item['translations'].get(l)],
        translationsMissing=[l for l in LANGS if not item['translations'].get(l)],
        pronunciationsMissing=[l for l in LANGS if not item['pronunciations'].get(l)],
        titlesMissing=[l for l in LANGS if not item['titles'].get(l)],
        notesMissing=[l for l in LANGS if item['notes'] and not item['notes'].get(l)],
        audioEmpty=not bool(item['audio']),sources=item['sources']))
report=dict(date='2026-11-16',scope='AI-assisted inventory; no scholarly approval; printed comparison remains pending',records=records)
(ROOT/'docs/duas-review-fields.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
labels={'arabic':'متن عربی','source':'منبع','grade':'درجهٔ سند','practice':'جزئیات عمل حنفی','translation':'بازبینی ترجمه','pronunciation':'تلفظ'}
out=['# فهرست واقعی فیلدهای دعا برای بازبینی','',
     'این فهرست از فایل دادهٔ نهایی برنامه استخراج شده است. تطبیق برخط، تأیید عالم یا تطبیق چاپی نیست. هیچ موردی تأیید نهایی نشده است. سه متن عربی ارسالی مالک بدون تغییر باقی مانده‌اند.','',
     '| زبان | عنوان‌های موجود | ترجمهٔ معنیِ پیش‌نویس | تلفظ خط‌به‌خط |','|---|---:|---:|---:|']
for lang in LANGS:
    out.append(f"| {lang} | {sum(lang in x['titles'] for x in data['items'])}/{len(records)} | {sum(bool(x['translations'].get(lang)) for x in data['items'])}/{len(records)} | {sum(bool(x['pronunciations'].get(lang)) for x in data['items'])}/{len(records)} |")
out+=['','عنوان‌ها و زیردسته‌های هفت زبان فعال محلی‌سازی شده‌اند؛ عنوان‌های سه زبان غیرفعال فعلی ممکن است به انگلیسی برگردند و این رفتار در رابط اعلام می‌شود. همهٔ ده زبان و محتوای قبلی در پروژه حفظ شده‌اند. عربیِ دعا عمداً در همهٔ زبان‌ها اصل عربی است؛ این به معنی وجود ترجمهٔ توضیحی عربی نیست. همهٔ ترجمه‌های جدید نیازمند بازبینی زبانی هستند. فایل صوتی برای هیچ موردی فراهم نشده و کنترل پخش آن‌ها مخفی است.','',
     '| شناسه | عنوان | فیلدهای علامت‌دار | متن عربی خالی | زبان‌های دارای ترجمهٔ پیش‌نویس |',
     '|---|---|---|---|---|']
for r in records:
    out.append(f"| `{r['id']}` | {r['title']} | {'، '.join(labels.get(k,k) for k in r['flaggedFields'])} | {'بله' if r['arabicEmpty'] else 'خیر'} | {', '.join(r['translationsPresent']) or '—'} |")
out+=['','فهرست JSON همراه، به تفکیک هر موضوع زبان‌های فاقد عنوان، ترجمه، تلفظ یا توضیح را نیز ثبت می‌کند. فایل صوتی خالی به‌عنوان نقص فنی پخش نمایش داده نمی‌شود. دعاهای دارای علامت متن/منبع/سند و موارد بدون ترجمهٔ زبان انتخابی وارد یادآور نمی‌شوند. دلیل‌های حدیثی چهارگانه نیز متن ذکرِ تصادفی محسوب نمی‌شوند.','']
(ROOT/'docs/duas-review-fields.md').write_text('\n'.join(out))
print(f'Exported {len(records)} pending-field records; {sum(r["arabicEmpty"] for r in records)} empty Arabic fields.')
