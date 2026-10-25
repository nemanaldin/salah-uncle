#!/usr/bin/env python3
"""Create downloadable, self-contained review files from actual successful build/test artifacts."""
from pathlib import Path
import argparse,base64,collections,hashlib,html,json,os,shutil,subprocess,zipfile
import xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--build',type=Path,required=True);p.add_argument('--android',type=Path,required=True);p.add_argument('--output',type=Path,required=True);a=p.parse_args()
out=a.output.resolve();out.mkdir(parents=True,exist_ok=True)
build=a.build.resolve();android=a.android.resolve()
def cases(files):
    found={}
    for path in files:
        for c in ET.parse(path).getroot().iter('testcase'):
            key=(c.get('classname'),c.get('name'))
            assert not any(c.find(t) is not None for t in ['failure','error','skipped']),str(path)+str(key)
            found[key]=True
    return len(found)
unit=cases((build/'test-results/testDebugUnitTest').glob('TEST-*.xml'))
ui=cases((android/'outputs/androidTest-results').rglob('TEST-*.xml'))
assert unit>=40 and ui>=18,(unit,ui)
lint=collections.Counter(x.get('severity') for x in ET.parse(build/'reports/lint-results-debug.xml').getroot().findall('issue'))
assert lint['Error']==lint['Fatal']==0
commit=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip()
run=os.environ.get('GITHUB_RUN_ID','')
run_url='https://github.com/nemanaldin/salah-uncle/actions/runs/'+run if run else ''
for source,name in [('outputs/apk/debug/app-debug.apk','salah-2.6.0-debug.apk'),('outputs/apk/release/app-release-unsigned.apk','salah-2.6.0-release-unsigned.apk')]:shutil.copy2(build/source,out/name)
source=out/'salah-2.6.0-source.zip'
subprocess.run(['git','archive','--format=zip','--prefix=salah-uncle/','--output='+str(source),commit],cwd=ROOT,check=True)
changes=subprocess.check_output(['git','diff','--name-only','f0ffb4a272f67782a35a67cd766c5e3a9b4cdacc',commit],cwd=ROOT,text=True)
(out/'changed-files.txt').write_text(changes)
shutil.copytree(ROOT/'docs',out/'docs',dirs_exist_ok=True)
for root,label,paths in [(build,'build',['reports','test-results']),(android,'android',['reports','outputs/androidTest-results','outputs/connected_android_test_additional_output'])]:
    for path in paths:
        if (root/path).exists():shutil.copytree(root/path,out/'verification'/label/path,dirs_exist_ok=True)
screen_root=android/'outputs/connected_android_test_additional_output'
screenshots=sorted(screen_root.rglob('*.png'))
assert len(list(screen_root.rglob('home-*.png')))==10
assert len(list(screen_root.rglob('lesson-*.png')))==10
assert len(screenshots)>=22,len(screenshots)
images=out/'screenshots';images.mkdir(exist_ok=True)
for screenshot in screenshots:shutil.copy2(screenshot,images/screenshot.name)
release_bytes=(out/'salah-2.6.0-release-unsigned.apk').stat().st_size
with zipfile.ZipFile(out/'salah-2.6.0-release-unsigned.apk') as z:geo_zip_bytes=z.getinfo('assets/geography/cities-v1.bin').compress_size
stats={'commit':commit,'runUrl':run_url,'unitTestsPassed':unit,'androidTestsPassed':ui,'lintErrors':0,'lintWarnings':lint['Warning'],'screenshots':len(screenshots),'releaseApkBytes':release_bytes,'hostedBaseline25Bytes':10133870,'releaseDeltaVsHosted25Bytes':release_bytes-10133870,'heritageImageRawBytes':2507914,'legacyTranslatedSteps':657,'localBaseline24Bytes':7579045,'releaseDeltaVsLocalBaselineBytes':release_bytes-7579045,'cityAssetApkZipBytes':geo_zip_bytes,'cities':34006,'countriesAndTerritories':244,'legacyCities':279,'translationStatus':'21 legacy Hanafi lessons / 73 steps have all nine non-Persian translations. Arabic recitations remain Arabic. Independent native/specialist sign-off pending.','newBundledRecordings':0}
(out/'verification-summary.json').write_text(json.dumps(stats,ensure_ascii=False,indent=2)+'\n')
font=base64.b64encode((ROOT/'app/src/main/res/font/vazirmatn_regular.ttf').read_bytes()).decode()
css="""@font-face{font-family:Vazir;src:url(data:font/ttf;base64,FONT)}*{box-sizing:border-box}body{margin:0;background:#f1f5f0;color:#233d34;font:16px/2 Vazir,sans-serif}main{max-width:1000px;margin:24px auto;padding:28px;background:white;border-radius:18px}h1,h2{color:#16685d}h1{font-size:26px}h2{font-size:21px}a{color:#16685d}table{border-collapse:collapse;width:100%}td,th{border:1px solid #d8e2d9;padding:10px;text-align:right}code{direction:ltr;unicode-bidi:isolate;overflow-wrap:anywhere}pre{font:inherit;white-space:pre-wrap;overflow-wrap:anywhere}.gallery{display:grid;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));gap:15px}.gallery img{width:100%;border-radius:12px}figure{margin:0}figcaption{text-align:center}.notice{background:#f5e8d6;border-radius:12px;padding:16px}@media(max-width:600px){main{margin:8px;padding:16px}}@media print{main{margin:0;padding:0}.gallery{display:none}body{font-size:11pt}}""".replace('FONT',font)
esc=html.escape
parts=['<!doctype html><html lang="fa" dir="rtl"><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Salah ۲٫۶</title><style>'+css+'</style><main>',
'<h1>نسخهٔ ۲٫۶ Salah</h1><p>ترجمهٔ متن همهٔ ۷۳ مرحلهٔ قدیمی به ۹ زبان دیگر، تصاویر نسخهٔ اولیه و رابط تازهٔ خانه، آموزش، اوقات و قبله.</p>',
'<p>کامیت <code>'+esc(commit)+'</code> · <a href="https://github.com/nemanaldin/salah-uncle/pull/1">PR پیش‌نویس شمارهٔ ۱</a></p>',
'<div class="notice">متن توضیحی ۲۱ آموزش قدیمی در هر ده زبان موجود است؛ ذکر و صوت تلاوت عربی باقی می‌ماند. تأیید مستقل ویراستار بومی و متخصص فقه هنوز ادعا نمی‌شود. آموزش‌های تخصصی حنفی به مذاهب دیگر نسبت داده نمی‌شوند. این بسته برای بازبینی PR پیش‌نویس است.</div>',
'<h2>بررسی واقعی این ساخت</h2><table><tr><th>بررسی</th><th>نتیجه</th></tr>',
f'<tr><td>تست واحد</td><td>{unit} تست پاس</td></tr><tr><td>تست Android</td><td>{ui} تست پاس، بدون تست ردشده</td></tr><tr><td>Lint</td><td>صفر خطا و {lint["Warning"]} هشدار</td></tr>',
f'<tr><td>APK بهینه‌شدهٔ بدون امضا</td><td>{release_bytes:,} بایت؛ اختلاف با ساخت گیت‌هاب نسخهٔ ۲٫۵: {release_bytes-10133870:+,} بایت</td></tr><tr><td>سهم فهرست شهرها در ZIP برنامه</td><td>{geo_zip_bytes:,} بایت؛ ۳۴٬۰۰۶ شهر جهان به‌علاوهٔ ۲۷۹ رکورد قدیمی</td></tr></table>',
'<h2>فایل‌های همراه</h2><ul><li><a href="salah-2.6.0-debug.apk">APK آزمایشی قابل نصب</a></li><li><a href="salah-2.6.0-release-unsigned.apk">APK انتشار بدون امضا</a></li><li><a href="salah-2.6.0-source.zip">سورس کامل</a></li><li><a href="docs/TRANSLATION_COVERAGE.json">وضعیت دقیق ترجمه</a></li></ul>',
'<p>ZIP را کامل استخراج کنید؛ این گزارش با فونت و تصاویر داخلی بدون اینترنت روی کامپیوتر دیگر باز می‌شود. APK آزمایشی با کلید توسعه امضا می‌شود؛ کلید ساخت‌های مختلف ممکن است متفاوت باشد. خروجی انتشار به امضای مالک نیاز دارد. برای GPS، قطب‌نما و اذان پس از راه‌اندازی/با صفحهٔ خاموش، بررسی روی گوشی واقعی باقی است.</p>',
'<h2>گزارش تفصیلی</h2><pre>'+esc((ROOT/'docs/UPDATE_2_6_FA.md').read_text())+'</pre>',
'<h2>تصاویر واقعی خانه و آموزش در ده زبان، اوقات و قبله</h2><div class="gallery">']
for screenshot in screenshots:
    data=base64.b64encode(screenshot.read_bytes()).decode()
    parts.append('<figure><img alt="'+esc(screenshot.stem)+'" src="data:image/png;base64,'+data+'"><figcaption>'+esc(screenshot.stem)+'</figcaption></figure>')
parts+=['</div>','<p><a href="'+esc(run_url)+'">اجرای گیت‌هاب این ساخت</a></p>' if run_url else '', '</main></html>']
(out/'salah-2.6.0-report-fa.html').write_text(''.join(parts))
checks=[]
for f in sorted(out.rglob('*')):
    if f.is_file() and f.name not in ('SHA256SUMS.txt','salah-2.6.0-portable.zip'):
        checks.append(hashlib.sha256(f.read_bytes()).hexdigest()+'  '+str(f.relative_to(out)))
(out/'SHA256SUMS.txt').write_text('\n'.join(checks)+'\n')
with zipfile.ZipFile(out/'salah-2.6.0-portable.zip','w',zipfile.ZIP_DEFLATED) as z:
    for f in sorted(out.rglob('*')):
        if f.is_file() and f.name!='salah-2.6.0-portable.zip':z.write(f,f.relative_to(out))
print(json.dumps(stats,indent=2,ensure_ascii=False))
