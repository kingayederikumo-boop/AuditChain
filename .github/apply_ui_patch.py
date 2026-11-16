import re,hashlib,sys
R=sys.argv[1] if len(sys.argv)>1 else '.'
def rd(p): return open(R+'/'+p,encoding='utf8').read()
def wr(p,s): open(R+'/'+p,'w',encoding='utf8',newline='').write(s)
def sub(p,a,b):
    s=rd(p); assert a in s,(p,a); wr(p,s.replace(a,b,1))
ADD='    title_sheets: ["AuditChain — Result sheets", "AuditChain — Result sheets", "AuditChain — \xc0wọn \xecw\xe9 \xe0b\xe1j\xe1de", "AuditChain — Mpempe akwụkwọ nsonaazụ", "AuditChain — Takardun sakamako"],\n    title_how: ["AuditChain — How it works", "AuditChain — How e dey work", "AuditChain — B\xed \xf3 ṣe ń ṣiṣẹ́", "AuditChain — Otu o si arụ ọrụ", "AuditChain — Yadda yake aiki"],\n    title_use: ["AuditChain — How to use", "AuditChain — How to use am", "AuditChain — B\xed a ṣe ń l\xf2 \xf3", "AuditChain — Otu esi eji ya", "AuditChain — Yadda ake amfani"],\n    go_title: ["Where to next", "Where to go next", "Ibi t\xed a ń lọ t\xf3k\xe0n", "Ebe ọzọ ị ga-aga", "Inda za a ci gaba"],\n    card_use: ["Step-by-step help for officers, observers and reviewers.", "Step-by-step help for officers, observers and reviewers.", "\xccr\xe0nlọ́wọ́ n\xed \xecgb\xe9sẹ̀ kọ̀ọ̀kan f\xfan \xe0wọn on\xedṣẹ́, ol\xf9w\xf2ye \xe0ti ol\xf9yẹ̀w\xf2.", "Nkwado nzọụkwụ ka nzọụkwụ maka ndị ọrụ, ndị nyocha na ndị nlekọta.", "Jagora mataki-mataki ga jami’ai, masu sa ido da masu bita."],\n    card_send: ["Enter the figures from a result sheet and attach a photo.", "Put di figures from result sheet and add photo.", "Tẹ \xe0wọn \xe0m\xec \xecw\xe9 \xe0b\xe1j\xe1de s\xedn\xfa, k\xed o s\xec fi fọ́t\xf2 k\xfan un.", "Tinye ọnụọgụgụ si na mpempe akwụkwọ nsonaazụ ma tinye foto.", "Shigar da alkaluman takardar sakamako kuma ka haɗa hoto."],\n'
S='src/main/resources/static/'
s=rd(S+'i18n.js'); m=re.search(r'^ +title_submit:.*\n',s,re.M); assert m
wr(S+'i18n.js',s[:m.end()]+ADD+s[m.end():])
NAV=['/','nav_check','Check'],['/sheets.html','nav_sheets','Result sheets'],['/how-it-works.html','nav_how','How it works'],['/how-to-use.html','nav_use','How to use'],['/submit.html','nav_submit','Send a sheet']
nav='\n'.join(' <a href="%s"%s data-i18n="%s">%s</a>'%(h,' aria-current="page"' if h=='/submit.html' else '',k,t) for h,k,t in NAV)
s=rd(S+'submit.html')
s,n=re.subn(r'(<nav class="site-nav"[^>]*>\n).*?(\n  </nav>)',lambda m:m.group(1)+nav+m.group(2),s,count=1,flags=re.S); assert n==1
a='href="/#use" data-i18n="sb_help"'; assert a in s
s=s.replace(a,'href="/how-to-use.html" data-i18n="sb_help"',1)
J="<script>(function(){var a=document.querySelector('.site-nav a[aria-current]');if(a){var n=a.parentNode;n.scrollLeft=a.offsetLeft-n.clientWidth/2+a.offsetWidth/2;}})();</script>\n"
assert '</body>' in s
s=s.replace('</body>',J+'</body>',1)
wr(S+'submit.html',s)
sub('src/main/java/com/auditchain/web/I18nScriptInjectionFilter.java','return !(path.equals("/") || path.equals("/index.html") || path.equals("/submit.html"));','return !(path.equals("/") || path.equals("/index.html") || path.equals("/submit.html")\n                || path.equals("/sheets.html") || path.equals("/how-it-works.html") || path.equals("/how-to-use.html"));')
sub('src/main/java/com/auditchain/security/ApiKeyAuthFilter.java','"/submit.html", "/i18n.js"','"/submit.html",\n            "/sheets.html", "/how-it-works.html", "/how-to-use.html", "/i18n.js"')
sub('src/test/java/com/auditchain/security/ApiKeyAuthFilterTest.java','        assertEquals(200, status("GET", "/a11y.css", null));\n','        assertEquals(200, status("GET", "/a11y.css", null));\n        for (String page : new String[] {"/sheets.html", "/how-it-works.html", "/how-to-use.html"}) {\n            assertEquals(200, status("GET", page, null));\n        }\n')
T='    @Test\n    void injectsIntoEveryPage() throws Exception {\n        for (String page : new String[] {"/", "/submit.html", "/sheets.html", "/how-it-works.html", "/how-to-use.html"}) {\n            var response = new MockHttpServletResponse();\n            new I18nScriptInjectionFilter().doFilter(new MockHttpServletRequest("GET", page), response, htmlChain);\n            String out = response.getContentAsString(StandardCharsets.UTF_8);\n            assertTrue(out.contains("/i18n.js"), page);\n            assertTrue(out.contains("/a11y.css"), page);\n        }\n    }\n\n'
sub('src/test/java/com/auditchain/web/I18nScriptInjectionFilterTest.java','    @Test\n    void leavesOtherPathsUntouched()',T+'    @Test\n    void leavesOtherPathsUntouched()')
for p,h in ((S+'i18n.js','9e3691d8024ff2a8cd97e4e17a7c1d8be3f728fab522f5340f7c42a09d5d01c3'),(S+'submit.html','2f148c3f6eb3b5daed49441a1c34f5baf94696147d285ac79645ec9101b10b74')):
    got=hashlib.sha256(open(R+'/'+p,'rb').read()).hexdigest()
    print(p,got==h)
    assert got==h,got
