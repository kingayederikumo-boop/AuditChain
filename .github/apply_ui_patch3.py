import hashlib,sys
R=sys.argv[1] if len(sys.argv)>1 else '.'
P=R+'/src/main/resources/static/a11y.css'
s=open(P,encoding='utf8').read()
PAIRS=[(' --bg:#f7f7f8;--surface:#fff;--soft:#f0f0f3;--ink:#111114;--ink2:#5a5a63;--line:#e4e4e9;--line2:#c9c9d1;', ' --bg:#fff;--surface:#fff;--soft:#f2f4f8;--ink:#0b0d12;--ink2:#555b69;--line:#e3e6ed;--line2:#c6cbd6;'), (' --accent:#4f46e5;--accent-h:#4338ca;--on-accent:#fff;--accent-t:#eeedfd;--accent-ink:#3730a3;', ' --accent:#2f54d8;--accent-h:#2441b5;--on-accent:#fff;--accent-t:#e8eeff;--accent-ink:#223fb0;'), ('--idle:#6b6b75;--idle-t:#eeeef2;', '--idle:#5f6676;--idle-t:#eef0f5;'), (' --glow:rgba(79,70,229,.10);--focus:#4f46e5;--shadow:0 1px 2px rgba(17,17,20,.05),0 10px 28px -14px rgba(17,17,20,.18);', ' --glow:rgba(47,84,216,.10);--focus:#2f54d8;--shadow:0 1px 2px rgba(11,13,18,.05),0 10px 28px -14px rgba(11,13,18,.2);'), (' --bg:#0e0e10;--surface:#17171a;--soft:#222226;--ink:#f3f3f5;--ink2:#a6a6b0;--line:#2b2b31;--line2:#3b3b43;', ' --bg:#0a0b0f;--surface:#13151b;--soft:#1c1f27;--ink:#f4f5f8;--ink2:#a4a9b6;--line:#252934;--line2:#383d4b;'), (' --accent:#9b94ff;--accent-h:#b1abff;--on-accent:#0e0e10;--accent-t:#25244a;--accent-ink:#c9c5ff;', ' --accent:#7f9bff;--accent-h:#9bb1ff;--on-accent:#0a0b0f;--accent-t:#17224f;--accent-ink:#bccaff;'), ('--idle:#9a9aa5;--idle-t:#232328;', '--idle:#9aa0ae;--idle-t:#1e212a;'), (' --glow:rgba(155,148,255,.14);--focus:#b1abff;', ' --glow:rgba(127,155,255,.16);--focus:#9bb1ff;'), ('background:linear-gradient(140deg,#6d63f5,#3b32c9);box-shadow:0 4px 12px -4px rgba(79,70,229,.6)', 'background:linear-gradient(140deg,#4a6cf0,#1e3aa8);box-shadow:0 4px 12px -4px rgba(47,84,216,.6)'), ('box-shadow:0 1px 3px rgba(17,17,20,.14),0 0 0 1px var(--line)', 'box-shadow:0 1px 3px rgba(11,13,18,.14),0 0 0 1px var(--line)')]
for a,b in PAIRS:
    assert s.count(a)==1,a
    s=s.replace(a,b)
open(P,'w',encoding='utf8',newline='').write(s)
h=hashlib.sha256(s.encode()).hexdigest()
print(h)
assert h=='2e9a42e892bbebaaa89afb68aaac157c0b1f399c37fb9bc09b2fe82cbebbb380'
