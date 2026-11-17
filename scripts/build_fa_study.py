#!/usr/bin/env python3
"""Export the actual Persian overlay and its unresolved fields, without tables.

Uses existing host tools (pandoc, Chromium, Playwright, PyMuPDF) and bundled fonts.
No Android dependency or downloaded font is introduced. Does not publish anything.
"""
from pathlib import Path
import argparse
import base64
import hashlib
import html
import json
import re
import subprocess

from verify_fa_duas import validate

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app/src/main/assets/duas'
FONT = ROOT / 'app/src/main/res/font'
DIGITS = str.maketrans('0123456789', '۰۱۲۳۴۵۶۷۸۹')
COMMON_NOTES = {
    'معنی فارسی، بازنویسی معنایی از متن مأخذِ بازشده در همین اجراست؛ نقلِ لفظی از مترجم چاپی نیست.',
    'خوانش خط فارسی، آوانویسی ویراستاری از عربیِ همین مأخذ است؛ ضبط مستقلِ چاپی یا گواهی تجوید محسوب نمی‌شود.',
}
GRADES = {'SAHIH': 'صحیح', 'HASAN': 'حسن', 'QURAN': 'قرآن؛ درجهٔ حدیثی موضوعیت ندارد',
          'FIQH': 'مأخذ فقه حنفی؛ درجهٔ حدیث مرفوع نیست', 'ATHAR': 'اثر موقوف؛ به پیامبر نسبت داده نشده',
          'UNVERIFIED': 'درجه احراز نشده'}


def fa(number):
    return str(number).translate(DIGITS)


def paragraph(text, css=''):
    return f'<p class="{css}">{html.escape(text)}</p>\n'


def md_html(text):
    # Preserve owner Arabic and line breaks through the Markdown converter verbatim.
    protected = []
    def keep(match):
        protected.append(match.group())
        return f'\n\nSALAHARABICBLOCK{len(protected)-1}END\n\n'
    prepared = re.sub(r'<p class="arabic"[^>]*>.*?</p>', keep, text, flags=re.S)
    rendered = subprocess.check_output(['pandoc', '--from=gfm', '--to=html5', '--wrap=none'],
                                       input=prepared, text=True)
    for n, block in enumerate(protected):
        marker = f'<p>SALAHARABICBLOCK{n}END</p>'
        assert rendered.count(marker) == 1
        rendered = rendered.replace(marker, block)
    return rendered


def generate_study(data):
    shared = json.loads((ASSETS / 'duas-v1.json').read_text())
    items = {x['id']: x for x in data['items']}
    originals = {x['id']: x for x in shared['items']}
    lines = ['# دعاها، اذکار و روش‌های فارسی Salah',
             '\nنسخهٔ مطالعه و مقابلهٔ مالک — ۱۸ نوامبر ۲۰۲۶\n',
             'همهٔ ۷۰ موضوعِ داده در این فایل آمده‌اند: ۶۸ مدخل از نظر فیلد کامل، یک راهنمای تلاوتِ کهف بدون متن کامل سوره، و یک موضوعِ واردنشده به محتوای خواندنی. وضعیت داخلی همه pending_owner_review است. حذف برچسب‌های ویراستاری از رابط فارسی به معنی تأیید عالم نیست. این فایل برای بازبینی مالک و عالم حنفی تهیه شده است.\n',
             'معنی‌ها بازنویسی معنایی از متن منابعِ بازشده در ابزار وبِ همین اجرا هستند؛ نقلِ لفظی از ترجمهٔ چاپی مشخص نیستند. تلفظ‌ها آوانویسی ویراستاریِ همان عربی به خط فارسی‌اند و جای آموزش شفاهیِ مخارج و تجوید را نمی‌گیرند. روش‌های حنفی از منابع فقهی جدا از مأخذ حدیث استفاده می‌کنند.\n',
             'سه عربیِ ارسالی مالک بدون تغییر حفظ شده‌اند. تفاوت ضبط دعای نخست با نشر برخط و دیگر نکات منبع در پیوست آمده است. عنوان «کامل از نظر فیلد» دربارهٔ وجود اطلاعات است، نه گواهی صحت مستقل همهٔ خوانش‌ها و ترجمه‌ها.\n',
             'روش‌هایی که فرمول ثابت عربی ندارند، با متنِ روش و مأخذشان آمده‌اند؛ عبارت عربی یا تلفظِ ساختگی برای پرکردن فیلد تولید نشده است. متنِ دلیل حدیثی نیز با ذکرِ قابل تکرار یکسان شمرده نشده است.\n',
             '## فهرست دسته‌ها\n']
    for category in shared['categories']:
        lines.append(f"- [{category['titles']['fa']}](#category-{category['id']})")
    seen = set()
    for category in shared['categories']:
        lines += [f'\n<h2 class="category" id="category-{category["id"]}">{html.escape(category["titles"]["fa"])}</h2>\n']
        for section in (x for x in shared['sections'] if x['category'] == category['id']):
            canonical = [key for key in section['duaIds'] if originals[key]['section'] == section['id']]
            aliases = [key for key in section['duaIds'] if originals[key]['section'] != section['id']]
            lines.append(f"\n### {section['titles']['fa']}\n")
            if aliases:
                links = '، '.join(f'<a href="#dua-{key}">{html.escape(items[key]["title"])}</a>' for key in aliases)
                lines.append(f'<p>موضوعات مشترک این زیردسته: {links}؛ متن کامل در جای اصلیِ آن‌ها آمده است.</p>\n')
            for key in canonical:
                assert key not in seen, key
                seen.add(key)
                item = items[key]
                lines += [f'\n<h4 id="dua-{key}">{html.escape(item["title"])}</h4>\n',
                          paragraph(f'شناسه: {key}', 'identifier')]
                if item['arabic']:
                    label = 'متن دلیل حدیثی' if item['kind'] == 'evidence' else 'متن عربی'
                    lines.append(paragraph(label, 'field-label'))
                    lines.append(f'<p class="arabic" lang="ar" dir="rtl" data-dua-id="{key}">{html.escape(item["arabic"])}</p>\n')
                elif item['kind'] != 'practice':
                    lines.append(paragraph('متن خواندنی افزوده نشده است؛ علت در بخش ناتمام‌ها آمده است.'))
                if item['pronunciation']:
                    lines.append(paragraph('تلفظ خط فارسی', 'field-label'))
                    for line in item['pronunciation']:
                        lines.append(paragraph(line, 'pronunciation'))
                if item['meaning']:
                    lines += [paragraph('معنی فارسی', 'field-label'), paragraph(item['meaning'])]
                if item['practice']:
                    lines += [paragraph('روش یا محل کاربرد', 'field-label'), paragraph(item['practice'])]
                if item['sources']:
                    lines.append(paragraph('مأخذ و نوع/درجه', 'field-label'))
                    for source in item['sources']:
                        lines += [paragraph(f'{source["book"]} — {source["reference"]}. {GRADES[source["grade"]]}. {source["grader"]}'),
                                  f'<p class="source-url" dir="ltr"><a href="{html.escape(source["url"], quote=True)}">{html.escape(source["url"])}</a></p>\n']
                if originals[key]['repeats']:
                    lines.append(paragraph('شمار تکرار ثبت‌شده', 'field-label'))
                    for repeat in originals[key]['repeats']:
                        lines.append(paragraph(f'{repeat["arabic"]} — {fa(repeat["count"])} بار'))
                if item['missingFields']:
                    lines.append(paragraph('فیلدهای باز: ' + '، '.join(item['missingFields']) + '؛ شرح در پیوست ناتمام‌ها.'))
    assert seen == set(items), set(items) - seen
    unfinished = (ROOT / 'docs/FA_UNFINISHED.md').read_text()
    notes = ['# اختلاف‌ها و نکات منبع\n',
             'این یادداشت‌ها برای مقابلهٔ مالک و عالم‌اند و در رابط فارسی نمایش داده نمی‌شوند. هیچ موردی تأیید نهایی نشده است.\n',
             'دو نکتهٔ مشترک برای همهٔ متن‌های تکمیل‌شده: معنی فارسی بازنویسی معنایی از مأخذ خوانده‌شده است و تلفظ خط فارسی آوانویسی ویراستاری است. این دو فیلد به مترجم یا قاریِ یک نشر مشخص نسبت داده نشده‌اند.\n',
             'در منابع، FIQH نوعِ مأخذ حنفی است و درجهٔ حدیث محسوب نمی‌شود؛ ATHAR اثرِ موقوف است؛ قرآن نیز درجه‌بندی حدیثی ندارد. درجهٔ صحیح/حسن، همراه نامِ صاحب تصحیح یا تحسین در مأخذ آمده است.\n',
             'نشانی صفحهٔ وبِ کتاب، به‌جای شمارهٔ چاپیِ حدسی ثبت شده است. هر شمارهٔ کتابی که از فتوا یا پایگاه تخریج نقل شده، شاهدِ همان واسطه است؛ مراجعهٔ مستقیم به همهٔ نسخه‌های چاپی ادعا نمی‌شود.\n']
    for item in data['items']:
        specific = [note for note in item.get('internalNotes', []) if note not in COMMON_NOTES]
        grade_notes = list(dict.fromkeys(s['internalGradeNote'] for s in item['sources'] if s.get('internalGradeNote')))
        if specific or grade_notes:
            notes += [f'\n## {item["title"]} — {item["id"]}\n']
            notes += [f'- {text}' for text in specific + grade_notes]
    source_notes = '\n'.join(notes) + '\n'
    (ROOT / 'docs/FA_SOURCE_NOTES.md').write_text(source_notes)
    study = '\n'.join(lines) + '\n<div class="appendix">\n\n' + unfinished + '\n</div>\n<div class="appendix">\n\n' + source_notes + '\n</div>\n'
    (ROOT / 'docs/FA_DUAS_STUDY.md').write_text(study)
    return study


def css():
    def font(path):
        return base64.b64encode((FONT / path).read_bytes()).decode()
    return '''
@font-face{font-family:Vazir;src:url(data:font/ttf;base64,REGULAR)}
@font-face{font-family:Vazir;src:url(data:font/ttf;base64,BOLD);font-weight:700}
@font-face{font-family:Arabic;src:url(data:font/ttf;base64,ARABIC)}
@page{size:A4;margin:18mm 16mm 20mm}*{box-sizing:border-box}
html{print-color-adjust:exact;-webkit-print-color-adjust:exact}
body{font:10.2pt/1.9 Vazir,sans-serif;color:#223e34;margin:0}
p,li{orphans:3;widows:3}p{margin:0 0 2.6mm}li{margin-bottom:1.5mm}
ul,ol{padding-inline-start:6mm}a{color:#13644f;text-decoration:none;overflow-wrap:anywhere}
code,.identifier{font:8pt/1.7 Vazir,sans-serif;direction:ltr;unicode-bidi:isolate;overflow-wrap:anywhere;white-space:normal}
.identifier{text-align:right;color:#657a70;font-size:7.5pt}
h1,h2,h3,h4{line-height:1.6;break-after:avoid;color:#125f4b}
h1{font-size:23pt;margin:5mm 0 7mm}h2{font-size:18pt;margin:7mm 0 5mm}
h3{font-size:14pt;margin:6mm 0 4mm}h4{font-size:12.5pt;margin:7mm 0 1mm}
h2.category,.appendix{break-before:page}h1{break-after:avoid}
.arabic{font:17.5pt/2.05 Arabic,serif;text-align:right;white-space:pre-line;color:#162f26;margin:1mm 0 4mm}
.pronunciation{font-size:10.6pt;color:#315448;line-height:2}
.field-label{font-size:8.8pt;font-weight:700;color:#49675c;margin-top:2.5mm;break-after:avoid}
.source-url{font-size:7.4pt;color:#526b60;text-align:left;overflow-wrap:anywhere;line-height:1.65}
.cover{break-after:page;padding-top:26mm}.cover h1{font-size:29pt}
.cover .meta{padding:6mm;background:#edf4ef;border-right:3pt solid #2f775d;margin:10mm 0}
'''.replace('REGULAR', font('vazirmatn_regular.ttf')).replace('BOLD', font('vazirmatn_bold.ttf')).replace('ARABIC', font('amiri_regular.ttf'))


def render_pdf(browser, body, title, output, preview, data=None):
    import fitz
    document = '<!doctype html><html lang="fa" dir="rtl"><head><meta charset="utf-8"><title>' + html.escape(title) + '</title><style>' + css() + '</style></head><body>' + body + '</body></html>'
    page = browser.new_page(offline=True)
    page.set_content(document, wait_until='load')
    page.evaluate('document.fonts.ready')
    assert page.evaluate("document.fonts.check('12px Vazir')")
    if data is not None:
        assert page.evaluate("document.fonts.check('18px Arabic')")
    page.emulate_media(media='print')
    assert page.locator('table').count() == 0
    overflow = page.evaluate("() => [...document.querySelectorAll('p,li,h1,h2,h3,h4')].filter(x=>x.scrollWidth>x.clientWidth+3).map(x=>x.textContent.slice(0,90))")
    assert not overflow, overflow
    if data is not None:
        arabic = page.locator('[data-dua-id]').evaluate_all('(nodes) => Object.fromEntries(nodes.map(n=>[n.dataset.duaId,n.textContent]))')
        expected = {x['id']: x['arabic'] for x in data['items'] if x['arabic']}
        assert arabic == expected, ('Arabic changed during document export', [key for key, text in expected.items() if arabic.get(key) != text])
        for item in data['items']:
            assert page.locator('#dua-' + item['id']).count() == 1
    page.pdf(path=str(output), prefer_css_page_size=True, print_background=True, tagged=True, outline=True,
             display_header_footer=True,
             header_template='<div style="font:8px Arial;width:100%;padding:0 60px;color:#52645c">SALAH · PERSIAN STUDY · 2026-11-18</div>',
             footer_template='<div style="font:9px Arial;width:100%;text-align:center;color:#52645c"><span class="pageNumber"></span> / <span class="totalPages"></span></div>')
    page.close()
    pdf = fitz.open(output)
    names = sorted({font[3] for p in pdf for font in p.get_fonts()})
    assert all(name in ' '.join(names) for name in ['Vazirmatn', 'Amiri']) if data is not None else 'Vazirmatn' in ' '.join(names)
    selected = {0, 1, len(pdf)//2, len(pdf)-1}
    # Include the long qunut and owner-dua pages in visual review previews.
    for n, p in enumerate(pdf):
        if any(token in p.get_text() for token in ('witr-qunut', 'owner-assembly', 'owner-abu-umamah', 'FA_UNFINISHED')):
            selected.add(n)
    for n in sorted(selected):
        pdf[n].get_pixmap(matrix=fitz.Matrix(1.25, 1.25)).save(preview / f'{output.stem}-{n+1:02}.png')
    return dict(file=output.name, pages=len(pdf), bytes=output.stat().st_size,
                sha256=hashlib.sha256(output.read_bytes()).hexdigest(), fonts=names,
                textCharacters=sum(len(p.get_text()) for p in pdf), horizontalOverflow=overflow,
                arabicDomMatchesData=data is not None, previewPages=[n+1 for n in sorted(selected)])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output-dir', type=Path, default=ROOT / 'docs/reports/2026-11-18')
    parser.add_argument('--preview-dir', type=Path, default=ROOT / 'build/reports/fa-study-previews')
    args = parser.parse_args()
    args.output_dir.mkdir(parents=True, exist_ok=True)
    args.preview_dir.mkdir(parents=True, exist_ok=True)
    data = validate()
    study = generate_study(data)
    assert not re.search(r'^\s*\|', study, re.M), 'Tables are not allowed'
    code_commit = subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip()
    cover = '<section class="cover"><p dir="ltr">SALAH</p><h1>دعاها و اذکار فارسی</h1><p>مجموعهٔ مطالعه و مقابلهٔ مالک</p><div class="meta"><p>۱۸ نوامبر ۲۰۲۶ · فقه حنفی</p><p>۷۰ موضوع · هفت دسته · بدون جدول</p><p>عربی، خوانش فارسی، معنی، مأخذ و روش</p></div><p>ناتمام‌ها و اختلاف‌های ضبط در پیوست آمده‌اند. همهٔ مدخل‌ها در انتظار بازبینی مالک و عالم حنفی‌اند؛ این سند تأیید نهایی دینی نیست.</p></section>'
    from playwright.sync_api import sync_playwright
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(executable_path='/usr/bin/chromium', headless=True, args=['--no-sandbox'])
        study_info = render_pdf(browser, cover + md_html(study), 'Salah — Persian Duas Study', args.output_dir / 'Salah-Persian-Duas-Study.pdf', args.preview_dir, data)
        map_info = render_pdf(browser, md_html((ROOT / 'docs/PROJECT_MAP_FA.md').read_text()), 'Salah — Project Map Persian', args.output_dir / 'Salah-Project-Map-fa.pdf', args.preview_dir)
        browser.close()
    info = dict(codeCommit=code_commit, contentSha256=hashlib.sha256((ASSETS / 'duas-fa-v1.json').read_bytes()).hexdigest(),
                topics=len(data['items']), fieldComplete=sum(not x['missingFields'] for x in data['items']),
                unresolved=[x['id'] for x in data['items'] if x['missingFields']], internalStatus='pending_owner_review',
                sourceReadRecords=len(json.loads((ASSETS / 'fa-source-reads-v1.json').read_text())['sources']),
                pdfs=[study_info, map_info])
    (args.output_dir / 'pdf-verification.json').write_text(json.dumps(info, ensure_ascii=False, indent=2)+'\n')
    (args.output_dir / 'SHA256SUMS.txt').write_text(''.join(f'{p["sha256"]}  {p["file"]}\n' for p in info['pdfs']))
    print(json.dumps(info, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
