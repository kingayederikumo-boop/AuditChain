import re,json,glob,hashlib,sys
R=sys.argv[1] if len(sys.argv)>1 else '.'
S=R+'/src/main/resources/static/'
def rd(f): return open(S+f,encoding='utf8').read()
def wr(f,s): open(S+f,'w',encoding='utf8',newline='').write(s)
def cut(s,a,b):
    i=s.index(a); return s[i:s.index(b,i)]
idx=rd('index.html')
# pieces of the checker script that the how-it-works page needs
setIcon=cut(idx,'function setIcon','function renderChecks')
renderChecks=cut(idx,'function renderChecks','function renderStats')
loaders=cut(idx,'function getJson','$(\'refresh\')')
helpers=cut(idx,'function num(n)','function evidence')
# index: remove the "What we checked" section, the review note and the check renderers
idx,n=re.subn(r'  <section class="sec" aria-labelledby="ckTitle">.*?</section>\n\n','',idx,count=1,flags=re.S); assert n==1
idx,n=re.subn(r'   <p class="lead" data-i18n="rv_desc">.*?</p>\n','',idx,count=1,flags=re.S); assert n==1
idx=idx.replace(setIcon,'',1).replace(renderChecks,'',1)
a="function renderAll(){renderVerdict();renderChecks();renderStats();renderReview();}"; assert a in idx
idx=idx.replace(a,"function renderAll(){renderVerdict();renderStats();renderReview();}")
wr('index.html',idx)
# how-it-works: add the live checks and the review note
how=rd('how-it-works.html')
checks='''   <h2 id="ckTitle" style="margin-top:44px" data-i18n="ck_title">What we checked</h2>
   <ul class="checks">
    <li><span class="ic" id="icChain" aria-hidden="true">…</span><div><b data-i18n="ck_chain_q">Are the sheets joined up correctly?</b><span class="a" id="txtChain">Checking…</span></div></li>
    <li><span class="ic" id="icSnap" aria-hidden="true">…</span><div><b data-i18n="ck_snap_q">Do the public copies agree?</b><span class="a" id="txtSnap">Checking…</span></div></li>
   </ul>
'''
a='   <h3 style="font:700 20px var(--serif);margin-top:34px" data-i18n="gl_title">'; assert a in how
how=how.replace(a,checks+'   <h2 style="margin-top:44px" data-i18n="gl_title">',1)
how=how.replace('Words used on this site</h3>','Words used on this site</h2>',1)
note='   <p class="lead" data-i18n="rv_desc">This only means “please look at this sheet”. It does not mean anyone cheated.</p>\n'
a='   <p class="scope"'; assert a in how
how=how.replace(a,note+a,1)
js='''<script>
document.addEventListener('DOMContentLoaded',function(){
'use strict';
var $=function(i){return document.getElementById(i);};
var t=function(k,v){return window.AC?window.AC.t(k,v):k;};
var S={chain:null,anchors:null,entries:[],busy:true,cs:'wait',cn:0,cb:null,ss:'wait',sn:0};

'''+helpers+setIcon+renderChecks+"function renderAll(){renderChecks();}\n\n"+loaders.replace("S.ai={};S.entries.forEach(function(e){var p=parse(e.payload);if(p&&p.type==='AI_CHECK')S.ai[String(p.evidenceId)]=p;});\n","").replace("S.busy=false;S.checkedAt=new Date();renderAll();","S.busy=false;renderAll();")+"document.addEventListener('ac-lang',renderAll);\nrefreshAll();\n});\n</script>\n"
assert '</body>' in how
how=how.replace('</body>',js+'</body>',1)
wr('how-it-works.html',how)
# wording: "Please check" is not professional
L=rd('i18n.js').split('\n'); seen=set()
E={'pill_look':('Needs review','Need review'),'gl_look':('Needs review','Need review'),'st_look':('Sheets to review','Sheets wey need review'),
'u3_1':('Look at “Needs review” on the home page.','Look “Need review” for the home page.'),
'rv_please':('Please review','Abeg review am'),
'rv_desc':('This only means “please look at this sheet”. It does not mean anyone cheated.','This one just mean “abeg look this sheet”. E no mean say person cheat.')}
for i,l in enumerate(L):
    m=re.match(r'^(\s+)([a-z0-9_]+): (\[.*\])(,?)\s*$',l)
    if m and m.group(2) in E:
        a=json.loads(m.group(3)); k=m.group(2); a[0],a[1]=E[k]; seen.add(k)
        L[i]='%s%s: %s%s'%(m.group(1),k,json.dumps(a,ensure_ascii=False),m.group(4))
assert seen==set(E),set(E)-seen
wr('i18n.js','\n'.join(L))

ICON={'/':'<path d="M3 11l9-8 9 8"/><path d="M5 10v10h14V10"/>',
'/sheets.html':'<path d="M9 6h12M9 12h12M9 18h12"/><path d="M3.5 6h.01M3.5 12h.01M3.5 18h.01"/>',
'/how-it-works.html':'<circle cx="12" cy="12" r="9"/><path d="M12 8h.01M11 12h1v5h1"/>',
'/how-to-use.html':'<path d="M4 5a2 2 0 012-2h13v16H6a2 2 0 00-2 2z"/><path d="M4 21V5"/>',
'/submit.html':'<path d="M12 16V4M7 9l5-5 5 5"/><path d="M5 20h14"/>'}
NAV=[('/','nav_check','Check'),('/sheets.html','nav_sheets','Result sheets'),('/how-it-works.html','nav_how','How it works'),('/how-to-use.html','nav_use','How to use'),('/submit.html','nav_submit','Send a sheet')]
BTN='  <button class="menu-btn" id="menuBtn" type="button" aria-expanded="false" aria-controls="drawer" aria-label="Menu" data-i18n-attr="aria-label:nav_menu"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M4 7h16M4 12h16M4 17h16"/></svg></button>\n'
OLD="<script>(function(){var a=document.querySelector('.site-nav a[aria-current]');if(a){var n=a.parentNode;n.scrollLeft=a.offsetLeft-n.clientWidth/2+a.offsetWidth/2;}})();</script>\n"
JS="""<script>(function(){var b=document.getElementById('menuBtn'),d=document.getElementById('drawer'),s=document.getElementById('scrim'),c=document.getElementById('menuClose');if(!b||!d)return;
function open(){d.classList.add('open');s.classList.add('open');b.setAttribute('aria-expanded','true');document.documentElement.style.overflow='hidden';setTimeout(function(){c.focus();},60);}
function close(){d.classList.remove('open');s.classList.remove('open');b.setAttribute('aria-expanded','false');document.documentElement.style.overflow='';b.focus();}
b.addEventListener('click',open);c.addEventListener('click',close);s.addEventListener('click',close);
document.addEventListener('keydown',function(e){if(!d.classList.contains('open'))return;if(e.key==='Escape'){close();return;}if(e.key==='Tab'){var x=d.querySelectorAll('a,button'),f=x[0],l=x[x.length-1];if(e.shiftKey&&document.activeElement===f){e.preventDefault();l.focus();}else if(!e.shiftKey&&document.activeElement===l){e.preventDefault();f.focus();}}});
})();</script>
"""
for f in glob.glob(S+'*.html'):
    name=f.split('/')[-1]; s=rd(name)
    m=re.search(r'<a href="([^"]+)" aria-current="page"',s); cur=m.group(1)
    s,n=re.subn(r'  <nav class="site-nav".*?</nav>\n','',s,count=1,flags=re.S); assert n==1
    a='<div class="wrap head-row">\n'; assert a in s
    s=s.replace(a,a+BTN,1)
    items=''.join(' <li><a href="%s"%s><svg viewBox="0 0 24 24" aria-hidden="true">%s</svg><span data-i18n="%s">%s</span></a></li>\n'%(h,' aria-current="page"' if h==cur else '',ICON[h],k,t) for h,k,t in NAV)
    drawer='<div class="scrim" id="scrim"></div>\n<nav class="drawer" id="drawer" aria-label="Main menu" data-i18n-attr="aria-label:nav_main">\n <div class="drawer-head"><span>AuditChain</span><button class="menu-btn" id="menuClose" type="button" aria-label="Close menu" data-i18n-attr="aria-label:nav_close"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18"/></svg></button></div>\n <ul>\n'+items+' </ul>\n</nav>\n'
    a='</header>\n'; assert a in s
    s=s.replace(a,a+drawer,1)
    assert OLD in s
    s=s.replace(OLD,JS,1)
    wr(name,s)
CSS='''.menu-btn{display:inline-grid;place-items:center;width:44px;height:44px;border:0;border-radius:12px;background:transparent;color:var(--ink);cursor:pointer}
.menu-btn:hover{background:var(--soft)}
.menu-btn svg{width:22px;height:22px;fill:none;stroke:currentColor;stroke-width:2;stroke-linecap:round}
.scrim{position:fixed;inset:0;z-index:40;background:rgba(5,7,12,.5);opacity:0;visibility:hidden;transition:opacity .25s,visibility .25s}
.scrim.open{opacity:1;visibility:visible}
.drawer{position:fixed;z-index:50;top:0;bottom:0;left:0;width:min(310px,86vw);display:flex;flex-direction:column;gap:6px;padding:12px;background:var(--surface);border-right:1px solid var(--line);box-shadow:0 20px 60px rgba(5,7,12,.35);transform:translateX(-102%);visibility:hidden;transition:transform .25s ease,visibility .25s;overflow-y:auto}
.drawer.open{transform:none;visibility:visible}
.drawer-head{display:flex;align-items:center;justify-content:space-between;padding:4px 4px 10px 14px;font:650 18px var(--sans);letter-spacing:-.02em}
.drawer ul{list-style:none;margin:0;padding:0;display:grid;gap:4px}
.drawer a{display:flex;align-items:center;gap:12px;min-height:48px;padding:0 14px;border-radius:12px;color:var(--ink2);text-decoration:none;font-weight:550;font-size:16px}
.drawer a svg{width:20px;height:20px;flex:none;fill:none;stroke:currentColor;stroke-width:2;stroke-linecap:round;stroke-linejoin:round}
.drawer a:hover{background:var(--soft);color:var(--ink)}
.drawer a[aria-current=page]{background:var(--accent-t);color:var(--accent-ink)}
'''
c=rd('a11y.css')
a=' .lang select{width:132px}\n'; assert c.count(a)==1
c=c.replace(a,' .lang select{width:112px}\n .brand small{display:none}\n',1)
c,n=re.subn(r'^ ?\.site-nav[^\n]*\n','',c,flags=re.M); assert n==6,n
a='.banner{background'; assert c.count(a)==1
c=c.replace(a,CSS+a,1); wr('a11y.css',c)
L=rd('i18n.js').split('\n'); j=[i for i,l in enumerate(L) if re.match(r'\s+nav_main:',l)][0]
L[j+1:j+1]=['    nav_menu: %s,'%json.dumps(["Menu","Menu","Àtòjọ","Menu","Menu"],ensure_ascii=False),'    nav_close: %s,'%json.dumps(["Close menu","Close menu","Pa àtòjọ","Mechie menu","Rufe menu"],ensure_ascii=False)]
wr('i18n.js','\n'.join(L))

H={'sheets.html': 'e935d7720bc2c20fb62bdaf64357a8dbeaa1786044bb6e540034cda7558089d6', 'i18n.js': 'd6171523926930e51e5796cdae248ffbe736bbd8ff2e6aefa2b089d8355d7599', 'submit.html': '9590d08610bf64c287f30db7d16266df64d7939384218d15e51ad6dac9602363', 'index.html': '749c635a8254c81454d5b4d95dee4d912839b4dc1d8605c0358f63c4c6ef35cc', 'a11y.css': 'a0264cd84bb4f896b8381a37b3ca1ec07aa6670fe5bf3120415631166d02c007', 'how-to-use.html': '63aaec9d3627f4bdd7b3d02c11645ade588296bc48940a1c5fe0cdd3f35a8ac3', 'how-it-works.html': 'c002a6d99988907e1021ed4581ff5bf45bb413f9a49c6d470e01ebc0df791ba6'}
for f,h in H.items():
    got=hashlib.sha256(open(S+f,'rb').read()).hexdigest()
    print(f,got==h)
    assert got==h,(f,got)
