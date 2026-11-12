/* AuditChain language layer (key based).
 * Elements carry data-i18n="key" (text) or data-i18n-attr="attr:key;attr:key". Scripts call AC.t(key, vars).
 * Column order: English, Naijá Pidgin, Yorùbá, Igbo, Hausa. English is the source text.
 * Pidgin, Yorùbá, Igbo and Hausa are drafts awaiting native-speaker review (docs/LANGUAGES.md).
 * Ijaw (ijc) is listed but disabled until a native translator supplies a dictionary.
 */
(function () {
  "use strict";
  var L = ["en", "pcm", "yo", "ig", "ha"];
  var D = {
    title_index: ["AuditChain — Result sheet checker", "AuditChain — Result sheet checker", "AuditChain — Olùṣàyẹ̀wò ìwé àbájáde", "AuditChain — Onye nyocha mpempe akwụkwọ nsonaazụ", "AuditChain — Mai duba takardun sakamako"],
    title_submit: ["AuditChain — Send a result sheet", "AuditChain — Send result sheet", "AuditChain — Fi ìwé àbájáde ráńṣẹ́", "AuditChain — Zipu mpempe akwụkwọ nsonaazụ", "AuditChain — Aika takardar sakamako"],
    skip: ["Skip to main content", "Skip go main content", "Fo lọ sí àkóónú pàtàkì", "Wụga gaa na isi ọdinaya", "Tsallaka zuwa babban abun ciki"],
    brand_tag: ["Result sheet checker", "Result sheet checker", "Olùṣàyẹ̀wò ìwé àbájáde", "Onye nyocha mpempe akwụkwọ nsonaazụ", "Mai duba takardun sakamako"],
    nav_main: ["Main menu", "Main menu", "Àtòkọ pàtàkì", "Nchịkọta bụ isi", "Babban menu"],
    nav_check: ["Check", "Check", "Àyẹ̀wò", "Nyocha", "Duba"],
    nav_how: ["How it works", "How e dey work", "Bí ó ṣe ń ṣiṣẹ́", "Otu o si arụọ ọrụ", "Yadda yake aiki"],
    nav_sheets: ["Result sheets", "Result sheets", "Àwọn ìwé àbájáde", "Mpempe akwụkwọ nsonaazụ", "Takardun sakamako"],
    nav_use: ["How to use", "How to use am", "Bí a ṣe ń lò ó", "Otu esi eji ya", "Yadda ake amfani"],
    nav_submit: ["Send a sheet", "Send sheet", "Fi ìwé ráńṣẹ́", "Zipu mpempe akwụkwọ", "Aika takarda"],
    lang: ["Language", "Language", "Èdè", "Asụsụ", "Harshe"],
    banner: ["Practice version. Every record here is made-up test data. Do not enter real election results or personal details.", "Practice version. All the records here na made-up test data. No put real election result or person details.", "Ẹ̀yà ìdánrawò ni. Gbogbo àkọsílẹ̀ níbí jẹ́ àdàkọ fún ìdánwò. Má ṣe fi àbájáde ìdìbò gidi tàbí àlàyè ara ẹnì sí i.", "Nke ọzụzụ ka a bụ. Ndekọ niile ebe a bụ data nnwale e mebere. Etinye nsonaazụ ntuli aka n'ezie ma ọ bụ ozi nkeonwe.", "Wannan sigar gwaji ce. Duk bayanan nan na jabu ne na gwaji. Kada ka saka ainihin sakamakon zaɗe ko bayanan sirri."],

    v_load_t: ["Checking the records…", "We dey check the records…", "À ń ṣàyẹ̀wò àwọn àkọsílẹ̀…", "Anyị na-enyocha ndekọ…", "Muna duba bayanan…"],
    v_load_d: ["This takes a few seconds. It can take longer if the site was asleep.", "E go take small time. E fit take longer if the site don sleep.", "Ó máa gba ìjẹ́jú ááyá díì ẹ̀. Ó lè pẹ́ jù bí ojú-òpó náà bá ti sùn.", "Ọ ga-were obere oge. O nwere ike ịdị ogologo ma ọ bụrụ na weebụsaịtị ahụ hiere ụra.", "Zai ɗauki ƙanƙanin lokaci. Yana iya yin tsawo idan shafin yana barci."],
    v_ok_t: ["Nothing has been changed.", "Nothing don change.", "Kò sí ohun tí a yípadà.", "Ọ nweghị ihe e gbanwere.", "Ba a canza komai ba."],
    v_ok_d: ["Every saved result sheet still matches its fingerprint, and the public snapshots agree. No one has edited the records.", "Every result sheet wey dem save still match im fingerprint, and the public snapshots agree. Nobody don edit the records.", "Gbogbo ìwé àbájáde tí a fipamọ́ ṣì bá àmì ìka rẹ̀ mu, àwọn àwòrán gbogbo-ènìyan sì fohùn ṣọ̀kọn. Ẹnikẹ́ni kò yí àwọn àkọsílẹ̀ padà.", "Mpempe akwụkwọ nsonaazụ ọ bụla echekwara ka kwekọrọ na akara mkịpị aka ya, foto ọha na-ekwekokwa. Onweghị onye dezighịrịrị ndekọ ahu.", "Kowace takardar sakamako da aka adana har yanzu tana dacewa da alamar yatsarta, kuma hotunan jama'a sun yarda. Ba wanda ya gyara bayanan."],
    v_bad_t: ["Something has been changed.", "Something don change.", "A ti yí nǹkan padà.", "E gbanweela ihe.", "An canza wani abu."],
    v_bad_d: ["At least one saved record no longer matches its fingerprint. Stop and ask a supervisor to investigate.", "At least one saved record no longer match im fingerprint. Stop, tell supervisor make dem investigate.", "Ó kéré jù, àkọsílẹ̀ kàn tí a fipamọ́ kò bá àmì ìka rẹ̀ mu mọ́. Dúró kí o sì bẹ́ alábòójútó kàn láti ṣèwádìí.", "Opekata mpe, otu ndekọ echekwara adighịzi ekwekoro na akara mkịpị aka ya. Kwụsị ma ka onye nlekọta nyochaa.", "Aƙalla bayani ɗaya da aka adana ba ya dacewa da alamar yatsarsa. Ka tsaya ka nemi mai kula ya bincika."],
    v_cp_t: ["A public snapshot does not match.", "One public snapshot no match.", "Àwòrán gbogbo-ènìyan kan kò bá mu.", "Foto ọha otu adighị ekwekoro.", "Hoton jama'a ɗaya bai yi daidai ba."],
    v_cp_d: ["The records no longer agree with a snapshot published earlier. Ask a supervisor to investigate before you trust them.", "The records no longer agree with one snapshot wey dem publish before. Ask supervisor make e investigate before you trust dem.", "Àwọn àkọsílẹ̀ kò fohùn ṣọ̀kọn mọ́ pẹ̀lú àwòrán tí a tẹ̀jáde tẹ́lẹ̀. Bẹ́ alábòójútó láti ṣèwádìí kí o tó gbẹ́kẹ̀lé wọn.", "Ndekọ ahụ adighịzi ekwekoro na foto e bipụtara na mbụ. Rịo onye nlekọta ka o nyochaa tupu ị tụkwasị ha obi.", "Bayanan ba su sake dacewa da hoton da aka wallafa a baya ba. Ka nemi mai kula ya bincika kafin ka amince da su."],
    v_part_t: ["The records look fine so far.", "The records dey look fine so far.", "Àwọn àkọsílẹ̀ dà bí ẹnì pé wọń dára tí́tí dì ìsísìnylì.", "Ndekọ ahụ dị mma ruo ugbu a.", "Bayanan suna da kyau zuwa yanzu."],
    v_part_d: ["The records link together correctly, but we could not compare them with the public snapshots. Try again in a minute.", "The records link well, but we no fit compare dem with the public snapshots. Try again after small time.", "Àwọn àkọsílẹ̀ so mọ́ ara wọn dáádáá, ṣùgbọn a kò lè fi wé àwọn àwòrán gbogbo-ènìyan. Gbìyànjú lẹ́ẹ̀kan sí i láìpẹ́.", "Ndekọ ahụ jikọtara nke ọma, mana anyị enweghị ike iji ha tụnyere foto ọha. Nwaa ọzọ n'ime obere oge.", "Bayanan sun haɗu daidai, amma ba mu iya kwatanta su da hotunan jama'a ba. Ka sake gwadawa nan da ɗan lokaci."],
    v_na_t: ["We could not check right now.", "We no fit check now.", "A kò lè ṣàyẹ̀wò ní báyìn.", "Anyị enweghị ike ilele ugbu a.", "Ba mu iya dubawa yanzu ba."],
    v_na_d: ["The check did not finish. Press “Check again” before you rely on this page.", "The check no finish. Press “Check again” before you trust dis page.", "Àyẹ̀wò náà kò parí. Tẹ “Ṣàyẹ̀wò lẹ́ẹ̀kan sí i” kí o tó gbẹ́kẹ̀lé ojú-ìwé yìní.", "Nyocha ahụ agwụghị. Pịa “Lelee ọzọ” tupu ị tụkwasị ibe a obi.", "Binciken bai ƙare ba. Danna “Sake dubawa” kafin ka amince da wannan shafin."],
    btn_recheck: ["Check again", "Check again", "Ṣàyẹ̀wò lẹ́ẹ̀kan sí i", "Lelee ọzọ", "Sake dubawa"],
    last_checked: ["Last checked {time}", "Last time wey we check: {time}", "Àyẹ̀wò ìkẹ̀yìn: {time}", "Nlele ikpeazụ: {time}", "Binciken ƙarshe: {time}"],
    checking_now: ["Checking now…", "We dey check now…", "À ń ṣàyẹ̀wò báyìní…", "Na-elele ugbu a…", "Ana dubawa yanzu…"],
    not_checked: ["Not checked yet", "We never check yet", "A kò tìí ṣàyẹ̀wò", "Anyị elebeghị", "Ba a duba ba tukuna"],

    st_sheets: ["Result sheets saved", "Result sheets wey dem save", "Àwọn ìwé àbájáde tí a fipamọ́", "Mpempe akwụkwọ nsonaazụ echekwara", "Takardun sakamako da aka adana"],
    st_sheets_h: ["Every sheet sent in so far", "All sheets wey dem don send", "Gbogbo ìwé tí a ti fi ráńṣẹ́", "Mpempe akwụkwọ niile e zitere ruo ugbu a", "Duk takardun da aka aika zuwa yanzu"],
    st_snap: ["Public snapshots that match", "Public snapshots wey match", "Àwọn àwòrán gbogbo-ènìyan tí ó bá mu", "Foto ọha ndị kwekọrọ", "Hotunan jama'a da suka yi daidai"],
    st_snap_h: ["Signed copies kept on a public page", "Signed copies wey dem keep for public page", "Àwọn ẹ̀dà tí a fọwọ́sí tí a pa mọ́ sí ojú-ìwé gbogbo-ènìyan", "Mbipụta e binyere aka echekwara na ibe ọha", "Kwafi masu sa hannu da ake ajiye a shafin jama'a"],
    st_wait: ["Waiting to be stored", "Dey wait make dem store", "Ń dúró láti fipamọ́", "Na-eche ka echekwa", "Ana jiran adanawa"],
    st_wait_h: ["Received, not yet in the permanent database", "We don receive, but e never enter the permanent database", "A ti gbà á, kò tíì wà nínú ibi ìpamọ́ àìlópín", "Anyị natara ya, ma ọ noghị na nchekwa ebighi ebi", "An karɓa, amma bai shiga ma'ajiyar dindindin ba"],
    st_look: ["Need a second look", "Need another look", "Nílò àyẹ̀wò kejì", "Chọrọ ileba anya nke ọzọ", "Suna buƙatar sake dubawa"],
    st_look_h: ["Figures that do not add up or look unusual", "Figures wey no add up or look strange", "Àwọn nọ́ńbà tí kò dọ́gba tàbí tí ó dà bí àjèjì", "Ọnụọgụ ndị na-agbakwụneghị ma ọ bụ ndị dị ka ihe ọhụrụ", "Alƙaluman da ba su haɗu ba ko masu ban mamaki"],

    ck_title: ["What we checked", "Wetin we check", "Ohun tí a ṣàyẹ̀wò", "Ihe anyị lere", "Abin da muka duba"],
    ck_chain_q: ["Are the records linked correctly?", "Dem link the records well?", "Ṣé àwọn àkọsílẹ̀ so mọ́ ara wọn dáádáá?", "Ndekọ ahụ ọ jikọtara nke ọma?", "Shin bayanan sun haɗu daidai?"],
    ck_chain_ok: ["Yes. Records checked: {n}. All link to the one before.", "Yes. Records wey we check: {n}. All link to the one before.", "Bẹ́ẹ̀ ni. Àwọn àkọsílẹ̀ tí a ṣàyẹ̀wò: {n}. Gbogbo wọn so mọ́ èyí tó ṣáájú.", "Ee. Ndekọ anyị lere: {n}. Ha niile jikọtara na nke bu ha ụzọ.", "Eh. Bayanan da aka duba: {n}. Dukansu sun haɗu da na baya."],
    ck_chain_bad: ["No. A link is broken.", "No. One link don break.", "Rárá. Ọ̀kan nínú ìsopọ̀ ti já.", "Mba. Njikọ otu agbajiela.", "A'a. Wata hanyar haɗi ta karye."],
    ck_chain_bad_at: ["No. The link is broken at record {n}.", "No. The link break for record {n}.", "Rárá. ìsopọ̀ náà já ní àkọsílẹ̀ {n}.", "Mba. Njikọ ahụ agbajiela na ndekọ {n}.", "A'a. Hanyar haɗin ta karye a bayani na {n}."],
    ck_wait: ["Checking…", "We dey check…", "À ń ṣàyẹ̀wò…", "Na-elele…", "Ana dubawa…"],
    ck_na: ["We could not run this check.", "We no fit run this check.", "A kò lè ṣe àyẹ̀wò yìní.", "Anyị enweghị ike ịme nyocha a.", "Ba mu iya yin wannan binciken ba."],
    ck_snap_q: ["Do the public snapshots agree?", "Public snapshots agree?", "Ṣé àwọn àwòrán gbogbo-ènìyan fohùn ṣọ̀kọn?", "Foto ọha ọ kwekọrọ?", "Shin hotunan jama'a sun yarda?"],
    ck_snap_ok: ["Yes. Public snapshots checked: {n}. All match.", "Yes. Public snapshots wey we check: {n}. All match.", "Bẹ́ẹ̀ ni. Àwọn àwòrán gbogbo-ènìyan tí a ṣàyẹ̀wò: {n}. Gbogbo wọn bá mu.", "Ee. Foto ọha anyị lere: {n}. Ha niile kwekọrọ.", "Eh. Hotunan jama'a da aka duba: {n}. Duka sun yi daidai."],
    ck_snap_bad: ["No. A public snapshot does not match the records.", "No. One public snapshot no match the records.", "Rárá. Àwòrán gbogbo-ènìyan kan kò bá àwọn àkọsílẹ̀ mu.", "Mba. Foto ọha otu adighị ekwekoro na ndekọ.", "A'a. Hoton jama'a ɗaya bai dace da bayanan ba."],
    ck_snap_int: ["No. A signed snapshot stored here does not match the records.", "No. One signed snapshot wey dey here no match the records.", "Rárá. Àwòrán tí a fọwọ́sí tí a fipamọ́ síbí kò bá àwọn àkọsílẹ̀ mu.", "Mba. Foto e binyere aka echekwara ebe a adighị ekwekoro na ndekọ.", "A'a. Hoton da aka sa hannu a nan bai dace da bayanan ba."],
    ck_snap_none: ["No public snapshots were found yet, so this check could not finish.", "We no see any public snapshot yet, so this check no fit finish.", "A kò tìí rí àwòrán gbogbo-ènìyan kankan, nítorí náà àyẹ̀wò yìní kò lè parí.", "Anyị ahụbeghị foto ọha ọ bụla, ya mere nyocha a enweghị ike ịla n'iru.", "Ba a sami hotunan jama'a ba tukuna, don haka ba za a iya kammala wannan binciken ba."],

    sh_desc: ["Every sheet sent in, newest first.", "All sheets wey dem send, newest first.", "Gbogbo ìwé tí a fi ráńṣẹ́, tuntun ni àkọ́kọ́.", "Mpempe akwụkwọ niile e zitere, nke ọhụrụ bụ nke mbụ.", "Duk takardun da aka aika, sababbi a farko."],
    sh_search_ph: ["Search by polling unit", "Search by polling unit", "Wá nípa ibùdó ìdìbò", "Chọọ site na ebe ntuli aka", "Nemo ta rumfar zaɓe"],
    sh_search_l: ["Search result sheets", "Search result sheets", "Wá àwọn ìwé àbájáde", "Chọọ mpempe akwụkwọ nsonaazụ", "Nemo takardun sakamako"],
    sh_count: ["Showing {n}", "We dey show {n}", "Ń fihàn: {n}", "Na-egosi: {n}", "Ana nuna: {n}"],
    sh_count_q: ["Found: {n}", "We find: {n}", "A rí: {n}", "Achọtara: {n}", "An samu: {n}"],
    sh_empty: ["No sheets yet. Send one and it will appear here.", "No sheet yet. Send one and e go show here.", "Kò tíì sí ìwé kankan. Fi ọ̀kan ráńṣẹ́ yóò sì hàn níbí.", "Enweghị mpempe akwụkwọ ọ bụla. Zipu otu ọ ga-egosi ebe a.", "Babu takarda tukuna. Aika ɗaya zai bayyana a nan."],
    sh_empty_q: ["Nothing matches that search.", "Nothing match wetin you search.", "Kò sí ohun tó bá ìwádìí yẹn mu.", "Ọ nweghị ihe kwekọrọ na ọchụchọ ahụ.", "Babu abin da ya dace da wannan nema."],
    sh_err: ["We could not load the sheets. Press “Check again”.", "We no fit load the sheets. Press “Check again”.", "A kò lè gbé àwọn ìwé náà jáde. Tẹ “Ṣàyẹ̀wò lẹ́ẹ̀kan sí i”.", "Anyị enweghị ike ibupụta mpempe akwụkwọ ahụ. Pịa “Lelee ọzọ”.", "Ba mu iya ɗora takardun ba. Danna “Sake dubawa”."],
    sh_load: ["Loading sheets…", "We dey load sheets…", "À ń gbé àwọn ìwé jáde…", "Na-ebupụta mpempe akwụkwọ…", "Ana ɗora takardun…"],
    rec_n: ["Record {n}", "Record {n}", "Àkọsílẹ̀ {n}", "Ndekọ {n}", "Bayani na {n}"],
    rec_sum: ["Parties add up to {sum}. The sheet says {total}.", "Parties add up to {sum}. The sheet talk say {total}.", "Àpapọ̀ àwọn ẹgúbgẹ́ jẹ́ {sum}. Ìwé náà sọ pé {total}.", "Ọnụ ọgụgụ ndị otu bụ {sum}. Mpempe akwụkwọ ahụ kwuru {total}.", "Jimillar jam'iyyu ita ce {sum}. Takardar ta ce {total}."],
    rec_nototal: ["No total on this sheet to compare.", "No total dey this sheet to compare.", "Kò sí àpapọ̀ lórí ìwé yìní láti fi wé.", "Ọ nweghị ngụkọta na mpempe akwụkwọ a iji tụnyere.", "Babu jimilla a takardar da za a kwatanta."],
    pill_ok: ["Adds up", "E add up", "Ó dọ́gba", "Ọ na-agbakwụnye", "Ya haɗu"],
    pill_diff: ["Does not add up", "E no add up", "Kò dọ́gba", "Ọ naghị agbakwụnye", "Bai haɗu ba"],
    pill_look: ["Needs a second look", "Need another look", "Nílò àyẹ̀wò kejì", "Chọrọ ileba anya nke ọzọ", "Yana buƙatar sake dubawa"],
    pill_na: ["Not checked", "We no check am", "A kò ṣàyẹ̀wò", "Elebeghị ya", "Ba a duba ba"],
    unknown_pu: ["Unknown polling unit", "Polling unit wey we no know", "Ibùdó ìdìbò tí a kò mọ̀", "Ebe ntuli aka amaghị", "Rumfar zaɓe da ba a sani ba"],

    rv_desc: ["A flag means “please check this sheet”. It does not mean fraud has happened.", "Flag mean “abeg check this sheet”. E no mean say fraud don happen.", "Àmì àsíká túmọ̀ sí “jọ̀wọ́ ṣàyẹ̀wò ìwé yìní”. Kò túmọ̀ sí pé ìwà ẹ̀tàn ti ṣẹ̀lẹ̀.", "Ọkaọlọtọ pụtara “biko lelee mpempe akwụkwọ a”. Ọ pụtaghị na aghụghọ emeela.", "Alama tana nufin “don Allah a duba wannan takarda”. Ba ta nufin an yi zamba ba."],
    rv_none: ["Nothing to review right now.", "Nothing to review now.", "Kò sí ohun láti ṣàyẹ̀wò báyìní.", "Ọ dịghị ihe a ga-enyocha ugbu a.", "Babu abin dubawa yanzu."],
    rv_wait: ["Waiting for a reviewer", "Dey wait for reviewer", "Ń dúró de olùyẹ̀wò", "Na-eche onye nyocha", "Ana jiran mai dubawa"],
    rv_please: ["Please check", "Abeg check", "Jọ̀wọ́ ṣàyẹ̀wò", "Biko lelee", "Don Allah a duba"],
    rv_diff: ["The parties add up to {sum}, but the sheet says {total}.", "The parties add up to {sum}, but the sheet talk say {total}.", "Àpapọ̀ àwọn ẹgúbgẹ́ jẹ́ {sum}, ṣùgbọn ìwé náà sọ pé {total}.", "Ọnụ ọgụgụ ndị otu bụ {sum}, mana mpempe akwụkwọ ahụ kwuru {total}.", "Jimillar jam'iyyu ita ce {sum}, amma takardar ta ce {total}."],
    rv_ai: ["The automatic check asked for a person to look.", "The automatic check ask make person look am.", "Àyẹ̀wò aládááṣe béèrè pé kí ènìyan wò ó.", "Nyocha akpaka chọrọ ka mmadụ lelee ya.", "Binciken na atomatik ya nemi mutum ya duba."],
    rv_queued: ["A reviewer has not decided yet.", "Reviewer never decide yet.", "Olùyẹ̀wò kò tíì pinnu.", "Onye nyocha ekpebeghị.", "Mai dubawa bai yanke shawara ba tukuna."],

    hw_title: ["How AuditChain protects result sheets", "How AuditChain dey protect result sheets", "Bí AuditChain ṣe ń dáàbò bo àwọn ìwé àbájáde", "Otu AuditChain si echebe mpempe akwụkwọ nsonaazụ", "Yadda AuditChain ke kare takardun sakamako"],
    hw_intro: ["Think of a notebook where each page is stamped with a copy of the page before it. Change an old page and every stamp after it stops matching.", "Think of notebook wey each page get stamp from the page before am. Change old page and every stamp after am go stop to match.", "Ronú nípa ìwé kan tí a fi ẹ̀dà ojú-ìwé tó ṣáájú sí ojú-ìwé kọ̀ọ̀kan. Yí ojú-ìwé àtìjọ́ padà, gbogbo èdìdè tó tẹ̀lé e yóò dẹ́kun láti bá mu.", "Chee akwụkwọ ebe e kere peeji ọ bụla akara sitere na peeji ọ bụ ya ụzọ. Gbanwee peeji ochie, akara niile na-esote ya agaghị ekwekokwa.", "Ka yi tunanin littafi inda kowane shafi yana da hatimi daga shafin da ya gabace shi. Idan aka canza tsohon shafi, duk hatimin da ke biye da shi zai daina dacewa."],
    hw1_t: ["Save", "Save", "Fipamọ́", "Chekwaa", "Adana"],
    hw1_d: ["An officer sends the figures from a sheet and a photo of it. We save them and give them a unique fingerprint.", "Officer go send the figures from the sheet and photo. We go save dem and give dem one special fingerprint.", "Ọ̀gá ìdìbò kan fi àwọn nọ́ńbà láti inú ìwé àti fọ́tò rẹ̀ ráńṣẹ́. A fipamọ́ wọn, a sì fún wọn ní àmì ìka àrà ọ̀tọ̀.", "Onye ọrụ na-ezipu ọnụọgụ sitere na mpempe akwụkwọ na foto ya. Anyị na-echekwa ha, nye ha akara mkịpị aka pụrụ iche.", "Jami'i yana aika alƙaluman takardar da hotonta. Muna adana su, mu ba su alamar yatsa ta musamman."],
    hw2_t: ["Link", "Link", "Sopọ̀", "Jikọọ", "Haɗa"],
    hw2_d: ["Each fingerprint also contains the one before it. That is why editing one old sheet breaks every link after it.", "Each fingerprint get the one wey dey before am inside. Na why if you edit one old sheet, every link after am go break.", "Àmì ìka kọ̀ọ̀kan tún ní èyí tó ṣáájú rẹ̀ nínú. Ìdí nìyẹn tí yíyí ìwé àtìjọ́ kan padà fi ń fọ́ gbogbo ìsopọ̀ tó tẹ̀lé e.", "Akara mkịpị aka ọ bụla nwekwara nke bu ya ụzọ. Ọ bụ ya mere ịdezighịarị otu mpempe akwụkwọ ochie na-agbaji njikọ niile na-esote ya.", "Kowace alamar yatsa tana ɗauke da wadda ta gabace ta. Shi ya sa gyara tsohuwar takarda ɗaya yake karya duk hanyoyin haɗin da ke biye."],
    hw3_t: ["Publish", "Publish", "Tẹ̀jáde", "Bipụta", "Wallafa"],
    hw3_d: ["Every 6 hours we sign a snapshot of the latest fingerprint and copy it to a public page. Nobody can quietly rewrite history.", "Every 6 hours we go sign snapshot of the latest fingerprint and copy am put for public page. Nobody fit rewrite history for quiet.", "Ní wákàtí mẹ́fà mẹ́fà, a máa fọwọ́ sí àwòrán àmì ìka tuntun jùlọ, a sì máa dà á kọ sí ojú-ìwé gbogbo-ènìyan. Ẹnikẹ́ni kò lè kọ ìtàn padà ní ìkọ̀kọ̀.", "Kwa awa isii anyị na-ebinye aka na foto akara mkịpị aka kachasị ọhụrụ ma detuo ya na ibe ọha. Onweghị onye pụrụ ide akụkọ ọzọ n'ezoghi ọnụ.", "Duk bayan awa 6 muna sa hannu a hoton sabuwar alamar yatsa mu kwafe ta zuwa shafin jama'a. Babu wanda zai iya sake rubuta tarihi a ɓoye."],
    hw4_t: ["Check", "Check", "Ṣàyẹ̀wò", "Lelee", "Duba"],
    hw4_d: ["Anyone can compare the records with the public snapshots. This page does it for you each time you open it.", "Anybody fit compare the records with the public snapshots. This page dey do am for you every time you open am.", "Ẹnikẹ́ni lè fi àwọn àkọsílẹ̀ wé àwọn àwòrán gbogbo-ènìyan. Ojú-ìwé yìní ń ṣe é fún ọ ní ìgbàkigbà tí o bá ṣí i.", "Onye ọ bụla pụrụ iji ndekọ tụnyere foto ọha. Ibe a na-eme ya gị oge ọ bụla ị mepere ya.", "Kowa na iya kwatanta bayanan da hotunan jama'a. Wannan shafin yana yi maka duk lokacin da ka buɗe shi."],

    use_title: ["How to use this site", "How to use dis site", "Bí a ṣe ń lo ojú-òpó yìní", "Otu esi eji weebụsaịtị a", "Yadda ake amfani da wannan shafi"],
    u1_t: ["I am a polling officer", "I be polling officer", "Ọ̀gá ibùdó ìdìbò ni mí", "Abụ m onye ọrụ ebe ntuli aka", "Ni jami'in rumfar zaɓe ne"],
    u1_1: ["Open “Send a sheet”.", "Open “Send sheet”.", "Ṣí “Fi ìwé ráńṣẹ́”.", "Mepee “Zipu mpempe akwụkwọ”.", "Buɗe “Aika takarda”."],
    u1_2: ["Enter your access code and the polling unit code.", "Enter your access code and the polling unit code.", "Tẹ kóòdù àǹfààní rẹ àti kóòdù ibùdó ìdìbò.", "Tinye koodu ịbanye gị na koodu ebe ntuli aka.", "Shigar da lambar shigarka da lambar rumfar zaɓe."],
    u1_3: ["Type each party’s votes and the total written on the sheet.", "Type each party votes and the total wey dem write for the sheet.", "Tẹ àwọn ìbò ẹgúbgẹ́ kọ̀ọ̀kan àti àpapọ̀ tí a kọ sórí ìwé náà.", "Dee votu otu ọ bụla na ngụkọta e dere na mpempe akwụkwọ ahụ.", "Rubuta ƙuri'un kowace jam'iyya da jimillar da aka rubuta a takardar."],
    u1_4: ["Add a clear photo of the sheet and press “Send sheet”.", "Add clear photo of the sheet and press “Send sheet”.", "Fi fọ́tò tó ṣe kedere ti ìwé náà kún un, kí o sì tẹ “Fi ìwé ráńṣẹ́”.", "Tinye foto doro anya nke mpempe akwụkwọ ahụ, pịa “Zipu mpempe akwụkwọ”.", "Ƙara hoto mai haske na takardar sannan ka danna “Aika takarda”."],
    u1_5: ["No network? The sheet waits on your phone. Press “Send saved sheets” when you are back online.", "No network? The sheet go wait for your phone. Press “Send saved sheets” when network come back.", "Kò sí nẹ́tíwọ́kì? Ìwé náà yóò dúró lórí fóónù rẹ. Tẹ “Fi àwọn ìwé tí a fipamọ́ ráńṣẹ́” nígbà tí o bá padà sórí íńtánẹ́ẹ̀tì.", "Enweghị netwọkọ? Mpempe akwụkwọ ahụ ga-eche na ekwenti gị. Pịa “Zipu mpempe akwụkwọ echekwara” mgbe netwọkọ laghachiri.", "Babu hanyar sadarwa? Takardar za ta jira a wayarka. Danna “Aika takardun da aka adana” idan sadarwa ta dawo."],
    u2_t: ["I am an observer or journalist", "I be observer or journalist", "Alábòójútó tàbí akọ̀ròyìn ni mí", "Abụ m onye nlele ma ọ bụ onye nta akụkọ", "Ni mai sa ido ne ko ɗan jarida"],
    u2_1: ["Read the big message at the top. It says whether anything was changed.", "Read the big message for top. E dey tell if anything change.", "Ka ọ̀rọ̀ ńlá tó wà lókè. Ó sọ bóyá a yí ohunkóhun padà.", "Gụọ ozi ukwu dị n'elu. O na-agwa gị ma e gbanwere ihe ọ bụla.", "Karanta babban saƙon da ke sama. Yana gaya maka ko an canza wani abu."],
    u2_2: ["Open “Result sheets” to see each sheet and whether its figures add up.", "Open “Result sheets” to see each sheet and if the figures add up.", "Ṣí “Àwọn ìwé àbájáde” láti rí ìwé kọ̀ọ̀kan àti bóyá àwọn nọ́ńbà rẹ̀ dọ́gba.", "Mepee “Mpempe akwụkwọ nsonaazụ” ka ịhụ mpempe akwụkwọ ọ bụla na ma ọnụọgụ ya na-agbakwụnye.", "Buɗe “Takardun sakamako” don ganin kowace takarda da ko alƙaluman ta sun haɗu."],
    u2_3: ["Open “Technical details” to copy a fingerprint and compare it with the public snapshots on GitHub.", "Open “Technical details” to copy fingerprint and compare am with the public snapshots for GitHub.", "Ṣí “Àwọn àlàyè ọ̀nà-ìmọ̀” láti ṣàdàkọ àmì ìka kí o sì fi wé àwọn àwòrán gbogbo-ènìyan lórí GitHub.", "Mepee “Nkọwa teknụzụ” ka ịdetuo akara mkịpị aka ma jiri ya tụnyere foto ọha na GitHub.", "Buɗe “Bayanan fasaha” don kwafar alamar yatsa ka kwatanta ta da hotunan jama'a a GitHub."],
    u3_t: ["I am a reviewer", "I be reviewer", "Olùyẹ̀wò ni mí", "Abụ m onye nyocha", "Ni mai dubawa ne"],
    u3_1: ["Look at “Needs a second look” on the checker page.", "Look “Need another look” for the checker page.", "Wo “Nílò àyẹ̀wò kejì” lórí ojú-ìwé olùṣàyẹ̀wò.", "Lee “Chọrọ ileba anya nke ọzọ” na ibe onye nyocha.", "Duba “Suna buƙatar sake dubawa” a shafin mai dubawa."],
    u3_2: ["Compare the figures with the photo of the sheet.", "Compare the figures with the photo of the sheet.", "Fi àwọn nọ́ńbà wé fọ́tò ìwé náà.", "Tụnyere ọnụọgụ ahụ na foto mpempe akwụkwọ ahụ.", "Kwatanta alƙaluman da hoton takardar."],
    u3_3: ["Your decision is made with the review tools and added to the same protected history.", "You go make your decision with the review tools and dem go add am to the same protected history.", "A máa ṣe ìpinnu rẹ pẹ̀lú àwọn irinṣẹ́ àyẹ̀wò, a sì máa fi kún ìtàn ààbò kan náà.", "A na-eme mkpebi gị site n'ọrụ nyocha ma tinye ya na akụkọ nchekwa ahụ ọ bụ otu.", "Ana yin shawararka da kayan dubawa kuma a ƙara ta a tarihi mai kariya iri ɗaya."],
    gl_title: ["Words used on this site", "Words wey dey dis site", "Àwọn ọ̀rọ̀ tí a lò níbí", "Okwu ndị e ji eme ebe a", "Kalmomin da aka yi amfani da su"],
    gl_fp: ["Fingerprint", "Fingerprint", "Àmì ìka", "Akara mkịpị aka", "Alamar yatsa"],
    gl_fp_d: ["A short code worked out from a record. Change one letter in the record and the code changes completely.", "Short code wey dem work out from record. Change one letter for the record and the code go change completely.", "Kóòdù kúkúrú tí a ṣírò láti inú àkọsílẹ̀ kan. Yí lẹ́tà kan padà nínú àkọsílẹ̀ náà, kóòdù náà yóò yí padà pátápátá.", "Koodu mkpụmkpụ e si na ndekọ gbakọọ. Gbanwee mkpụrụedemede otu n'ime ndekọ ahụ, koodu ahụ ga-agbanwe kpamkpam.", "Gajeriyar lamba da ake ƙƙirƙira daga bayani. Idan ka canza harafi ɗaya a bayanin, lambar za ta canza gaba ɗaya."],
    gl_snap: ["Snapshot", "Snapshot", "Àwòrán àkókò", "Foto oge", "Hoton lokaci"],
    gl_snap_d: ["A signed copy of the latest fingerprint, kept on a public page outside this site.", "Signed copy of the latest fingerprint, wey dem keep for public page outside dis site.", "Ẹ̀dà tí a fọwọ́sí ti àmì ìka tuntun jùlọ, tí a pa mọ́ sí ojú-ìwé gbogbo-ènìyan lẹ́yìn ojú-òpó yìní.", "Mbipụta e binyere aka nke akara mkịpị aka kachasị ọhụrụ, echekwara na ibe ọha n'èzí weebụsaịtị a.", "Kwafi mai sa hannu na sabuwar alamar yatsa, da ake ajiyewa a shafin jama'a a wajen wannan shafin."],
    gl_look: ["Second look", "Another look", "Àyẹ̀wò kejì", "Ileba anya nke ọzọ", "Sake dubawa"],
    gl_look_d: ["A sheet whose figures do not add up, or that the automatic check was unsure about. A person decides what it means.", "Sheet wey figures no add up, or wey automatic check no sure about. Person go decide wetin e mean.", "Ìwé kan tí àwọn nọ́ńbà rẹ̀ kò dọ́gba, tàbí tí àyẹ̀wò aládááṣe ṣiyèméjì lé lórí. Ènìyan ni yóò pinnu ohun tó túmọ̀ sí.", "Mpempe akwụkwọ ọnụọgụ ya na-agbakwụneghị, ma ọ bụ nke nyocha akpaka ejighi n'aka. Mmadụ ga-ekpebi ihe o pụtara.", "Takardar da alƙaluman ta ba su haɗu ba, ko wadda binciken atomatik bai tabbatar ba. Mutum ne zai yanke abin da take nufi."],
    td_title: ["Technical details", "Technical details", "Àwọn àlàyè ọ̀nà-ìmọ̀", "Nkọwa teknụzụ", "Bayanan fasaha"],
    td_sum: ["Records: {n}. Latest fingerprint: {hash}", "Records: {n}. Latest fingerprint: {hash}", "Àwọn àkọsílẹ̀: {n}. Àmì ìka tuntun jùlọ: {hash}", "Ndekọ: {n}. Akara mkịpị aka kachasị ọhụrụ: {hash}", "Bayanai: {n}. Sabuwar alamar yatsa: {hash}"],
    td_na: ["not available", "no dey", "kò sí", "adighị", "babu"],
    sc: ["This is a practice version with made-up data. A clean result means the saved records have not been edited since they were saved. It does not prove the original figures were honest, or that an election result is correct.", "Dis na practice version with made-up data. Clean result mean say dem never edit the saved records since dem save dem. E no prove say the original figures honest, or say election result correct.", "Èyí jẹ́ ẹ̀yà ìdánrawò pẹ̀lú àdàkọ dátà. Àbájáde tó mọ́ túmọ̀ sí pé a kò ṣàtúnṣe àwọn àkọsílẹ̀ tí a fipamọ́ látìgbà tí a ti fipamọ́ wọn. Kò fi hàn pé àwọn nọ́ńbà àkọ́kọ́ jẹ́ òtítọ́, tàbí pé àbájáde ìdìbò tọ́.", "Nke a bụ ụdị ọzụzụ nwere data e mebere. Nsonaazụ doro anya pụtara na e dezighị ndekọ echekwara kemgbe e chekwara ha. Ọ naghị egosi na ọnụọgụ mbụ bụ eziokwu, ma ọ bụ na nsonaazụ ntuli aka ziri ezi.", "Wannan sigar gwaji ce mai bayanan jabu. Sakamako mai tsabta yana nufin ba a gyara bayanan da aka adana ba tun lokacin da aka adana su. Ba ya tabbatar da cewa alƙaluman asali gaskiya ne, ko cewa sakamakon zaɓe daidai ne."],
    ft: ["AuditChain · Result sheet integrity prototype", "AuditChain · Result sheet integrity prototype", "AuditChain · Àpẹẹrẹ ìdánwò òtítọ́ ìwé àbájáde", "AuditChain · Ihe nlere nnwale eziokwu mpempe akwụkwọ nsonaazụ", "AuditChain · Samfurin gwajin gaskiyar takardun sakamako"],
    ft2: ["For demonstrations and authorised testing only", "For demonstrations and authorised testing only", "Fún ìfihàn àti ìdánwò tí a fún láṣẹ nìkan", "Naanị maka ngosi na nnwale e kwere ya", "Don nunawa da gwajin da aka ba da izini kawai"],

    sb_back: ["Back to the checker", "Go back to the checker", "Padà sí olùṣàyẹ̀wò", "Laghachi na onye nyocha", "Koma mai dubawa"],
    sb_title: ["Send a result sheet", "Send result sheet", "Fi ìwé àbájáde ráńṣẹ́", "Zipu mpempe akwụkwọ nsonaazụ", "Aika takardar sakamako"],
    sb_lede: ["Enter the figures from the sheet and add a photo. Your sheet is added to the protected history.", "Enter the figures from the sheet and add photo. Dem go add your sheet to the protected history.", "Tẹ àwọn nọ́ńbà láti inú ìwé náà kí o sì fi fọ́tò kún un. A ó fi ìwé rẹ kún ìtàn ààbò náà.", "Tinye ọnụọgụ sitere na mpempe akwụkwọ ahụ ma tinye foto. A ga-etinye mpempe akwụkwọ gị na akụkọ nchekwa ahụ.", "Shigar da alƙaluman takardar ka ƙara hoto. Za a ƙara takardarka a tarihi mai kariya."],
    sb_help: ["Need help? Read how to use this site.", "Need help? Read how to use dis site.", "Ṣé o nílò ìrànlọ́wọ́? Ka bí a ṣe ń lo ojú-òpó yìní.", "Chọrọ enyemaka? Gụọ otu esi eji weebụsaịtị a.", "Kana buƙatar taimako? Karanta yadda ake amfani da wannan shafi."],
    f1_t: ["Who you are", "Who you be", "Ẹnì tí ìwọ jẹ́", "Onye ị bụ", "Wanene kai"],
    f_key: ["Access code", "Access code", "Kóòdù àǹfààní", "Koodu ịbanye", "Lambar shiga"],
    f_key_h: ["Your coordinator gives you this code. It stays on this screen and is never saved.", "Your coordinator go give you this code. E dey stay for this screen only and dem no dey save am.", "Alákòóso rẹ ni ó fún ọ ní kóòdù yìní. Ó wà lórí ojú-ìwé yìní nìkan, a kò sì fipamọ́ rẹ̀.", "Onye nhazi gị na-enye gị koodu a. Ọ na-anọ naanị na ihuenyo a, a naghị echekwa ya.", "Mai kula da kai ne ke ba ka wannan lambar. Tana nan a wannan allon kawai, ba a adana ta."],
    f_key_ph: ["Paste your access code", "Paste your access code", "Lẹ kóòdù àǹfààní rẹ mọ́ ibí", "Tinye koodu ịbanye gị", "Manna lambar shigarka"],
    f2_t: ["Where", "Where", "Ibo", "Ebe", "Ina"],
    f_loc: ["Polling unit code", "Polling unit code", "Kóòdù ibùdó ìdìbò", "Koodu ebe ntuli aka", "Lambar rumfar zaɓe"],
    f_loc_h: ["Use the test code, for example PU-001.", "Use the test code, for example PU-001.", "Lo kóòdù ìdánwò, fún àpẹẹrẹ PU-001.", "Jiri koodu nnwale, dịka ọmụmaatụ PU-001.", "Yi amfani da lambar gwaji, misali PU-001."],
    f3_t: ["What the sheet says", "Wetin the sheet talk", "Ohun tí ìwé náà sọ", "Ihe mpempe akwụkwọ ahụ kwuru", "Abin da takardar ta ce"],
    f_party: ["Party", "Party", "Ẹgúbgẹ́", "Otu", "Jam'iyya"],
    f_party_ph: ["Name or code, e.g. A", "Name or code, e.g. A", "Orúkọ tàbí kóòdù, fún àpẹẹrẹ A", "Aha ma ọ bụ koodu, dịka A", "Suna ko lamba, misali A"],
    f_votes: ["Votes", "Votes", "Ìbò", "Votu", "Ƙuri'u"],
    f_add: ["Add another party", "Add another party", "Fi ẹgúbgẹ́ míìràn kún un", "Tinye otu ọzọ", "Ƙara wata jam'iyya"],
    f_remove: ["Remove", "Remove", "Yọ kúrò", "Wepụ", "Cire"],
    f_remove_l: ["Remove this party", "Remove this party", "Yọ ẹgúbgẹ́ yìní kúrò", "Wepụ otu a", "Cire wannan jam'iyyar"],
    f_total: ["Total on the sheet", "Total for the sheet", "Àpapọ̀ lórí ìwé náà", "Ngụkọta dị na mpempe akwụkwọ ahụ", "Jimilla a takardar"],
    f_total_h: ["The total number of votes written at the bottom of the sheet.", "The total number of votes wey dem write for bottom of the sheet.", "Àpapọ̀ iye ìbò tí a kọ sí ìsàlẹ̀ ìwé náà.", "Ngụkọta ọnụọgụ votu e dere n'okpuru mpempe akwụkwọ ahụ.", "Jimillar ƙuri'un da aka rubuta a ƙasan takardar."],
    f_sum: ["The parties add up to {sum}.", "The parties add up to {sum}.", "Àpapọ̀ àwọn ẹgúbgẹ́ jẹ́ {sum}.", "Ọnụ ọgụgụ ndị otu bụ {sum}.", "Jimillar jam'iyyu ita ce {sum}."],
    f_sum_ok: ["The parties add up to {sum}. This matches the total.", "The parties add up to {sum}. E match the total.", "Àpapọ̀ àwọn ẹgúbgẹ́ jẹ́ {sum}. Ó bá àpapọ̀ náà mu.", "Ọnụ ọgụgụ ndị otu bụ {sum}. Ọ kwekọrọ na ngụkọta ahụ.", "Jimillar jam'iyyu ita ce {sum}. Ta yi daidai da jimillar."],
    f_sum_diff: ["The parties add up to {sum}, but the total says {total}. You can still send it. A reviewer will take a look.", "The parties add up to {sum}, but the total talk say {total}. You fit still send am. Reviewer go look am.", "Àpapọ̀ àwọn ẹgúbgẹ́ jẹ́ {sum}, ṣùgbọn àpapọ̀ náà sọ pé {total}. O ṣì lè fi ráńṣẹ́. Olùyẹ̀wò yóò wò ó.", "Ọnụ ọgụgụ ndị otu bụ {sum}, mana ngụkọta ahụ kwuru {total}. Ị ka nwere ike izipu ya. Onye nyocha ga-elele ya.", "Jimillar jam'iyyu ita ce {sum}, amma jimillar ta ce {total}. Har yanzu za ka iya aika ta. Mai dubawa zai duba."],
    f4_t: ["Photo of the sheet", "Photo of the sheet", "Fọ́tò ìwé náà", "Foto mpempe akwụkwọ ahụ", "Hoton takardar"],
    f_file: ["Choose a photo", "Choose photo", "Yan fọ́tò", "Họrọ foto", "Zaɓi hoto"],
    f_file_h: ["PNG or JPEG. We shrink it and remove hidden details such as location before it leaves your phone.", "PNG or JPEG. We go shrink am and comot hidden details like location before e comot your phone.", "PNG tàbí JPEG. A dín in kù, a sì yọ àwọn àlàyè ìkọ̀kọ̀ bí ibi tí o wà kúrò kí ó tó kúrò lórí fóónù rẹ.", "PNG ma ọ bụ JPEG. Anyị na-eme ya obere ma wepụ nkọwa zoro ezo dị ka ebe ị nọ tupu ọ pụọ n'ekwentị gị.", "PNG ko JPEG. Muna rage girmansa mu cire ɓoyayyun bayanai kamar wuri kafin ya bar wayarka."],
    f_send: ["Send sheet", "Send sheet", "Fi ìwé ráńṣẹ́", "Zipu mpempe akwụkwọ", "Aika takarda"],
    q_title: ["Saved on this phone", "Wey dem save for dis phone", "Tí a fipamọ́ sórí fóónù yìní", "Echekwara na ekwentị a", "An adana a wannan wayar"],
    q_desc: ["With no network, sheets wait here until you send them.", "If no network, sheets go wait here until you send dem.", "Bí kò bá sí nẹ́tíwọ́kì, àwọn ìwé yóò dúró níbí tí́tí o fi ráńṣẹ́ wọn.", "Ọbụrụ na enweghị netwọkọ, mpempe akwụkwọ na-eche ebe a ruo mgbe ị zipụrụ ha.", "Idan babu sadarwa, takardun za su jira a nan har sai ka aika su."],
    q_retry: ["Send saved sheets", "Send saved sheets", "Fi àwọn ìwé tí a fipamọ́ ráńṣẹ́", "Zipu mpempe akwụkwọ echekwara", "Aika takardun da aka adana"],
    q_clear: ["Delete saved sheets", "Delete saved sheets", "Pa àwọn ìwé tí a fipamọ́ rẹ́", "Hichapụ mpempe akwụkwọ echekwara", "Share takardun da aka adana"],
    q_confirm: ["Delete every sheet saved on this phone?", "Delete every sheet wey dem save for dis phone?", "Ṣé kí a pa gbogbo ìwé tí a fipamọ́ sórí fóónù yìní rẹ́?", "Hichapụ mpempe akwụkwọ niile echekwara na ekwentị a?", "Share duk takardun da aka adana a wannan wayar?"],
    q_count: ["Waiting on this phone: {n}", "Dey wait for dis phone: {n}", "Ń dúró lórí fóónù yìní: {n}", "Na-eche na ekwentị a: {n}", "Suna jira a wannan wayar: {n}"],
    q_unavail: ["Saved sheets are not available in this browser.", "Saved sheets no dey available for dis browser.", "Àwọn ìwé tí a fipamọ́ kò sí nínú aṣàwákìí yìní.", "Mpempe akwụkwọ echekwara adighị na ihe nchọgharị a.", "Takardun da aka adana ba su samuwa a wannan burauzar."],
    m_processing: ["Preparing the photo…", "We dey prepare the photo…", "À ń múrà fọ́tò sílẹ̀…", "Na-akwadebe foto ahụ…", "Ana shirya hoton…"],
    m_saved: ["Photo saved on this phone. Sending…", "Photo don save for dis phone. We dey send am…", "A ti fi fọ́tò pamọ́ sórí fóónù yìní. À ń fi ráńṣẹ́…", "Echekwara foto na ekwentị a. Na-ezipu…", "An adana hoton a wannan wayar. Ana aikawa…"],
    m_saved_off: ["Saved on this phone. It will wait until you have network.", "Dem don save am for dis phone. E go wait until network come.", "A ti fi pamọ́ sórí fóónù yìní. Yóò dúró tí́tí nẹ́tíwọ́kì yóò fi wà.", "Echekwara na ekwentị a. Ọ ga-eche ruo mgbe netwọkọ dị.", "An adana a wannan wayar. Zai jira har sai an sami sadarwa."],
    m_need_key: ["Enter your access code first.", "Enter your access code first.", "Kọ́kọ́ tẹ kóòdù àǹfààní rẹ.", "Buru ụzọ tinye koodu ịbanye gị.", "Da farko shigar da lambar shigarka."],
    m_net: ["No network. Your sheet is safe on this phone and will wait.", "No network. Your sheet dey safe for dis phone and e go wait.", "Kò sí nẹ́tíwọ́kì. Ìwé rẹ wà ní ààbò lórí fóónù yìní, yóò sì dúró.", "Enweghị netwọkọ. Mpempe akwụkwọ gị nọ n'udo na ekwentị a ma ọ ga-eche.", "Babu sadarwa. Takardarka tana lafiya a wannan wayar kuma za ta jira."],
    m_ok: ["Sent. Your sheet is now part of the protected history.", "E don go. Your sheet don join the protected history.", "A ti fi ráńṣẹ́. Ìwé rẹ ti di apá kan ìtàn ààbò náà.", "Ezipụla. Mpempe akwụkwọ gị abụrụla akụkụ nke akụkọ nchekwa ahụ.", "An aika. Takardarka ta zama ɓangaren tarihi mai kariya."],
    m_401: ["Your access code was not accepted. Check it and press “Send saved sheets”.", "Dem no accept your access code. Check am and press “Send saved sheets”.", "A kò gba kóòdù àǹfààní rẹ. Ṣàyẹ̀wò rẹ̀ kí o sì tẹ “Fi àwọn ìwé tí a fipamọ́ ráńṣẹ́”.", "Anaghị anabata koodu ịbanye gị. Lelee ya ma pịa “Zipu mpempe akwụkwọ echekwara”.", "Ba a karɓi lambar shigarka ba. Ka duba ta sannan ka danna “Aika takardun da aka adana”."],
    m_http: ["The server refused it (error {code}). Your sheet is still saved on this phone. {detail}", "The server refuse am (error {code}). Your sheet still dey saved for dis phone. {detail}", "Olùpèsè kọ̀ ọ́ (àṣíṣe {code}). Ìwé rẹ ṣì wà lórí fóónù yìní. {detail}", "Sava ahụ jụrụ ya (njehie {code}). Mpempe akwụkwọ gị ka echekwara na ekwentị a. {detail}", "Sabar ta ƙi (kuskure {code}). Takardarka har yanzu tana adane a wannan wayar. {detail}"],
    m_pick_img: ["Choose a PNG or JPEG photo.", "Choose PNG or JPEG photo.", "Yan fọ́tò PNG tàbí JPEG.", "Họrọ foto PNG ma ọ bụ JPEG.", "Zaɓi hoton PNG ko JPEG."],
    m_need_loc: ["Enter the polling unit code.", "Enter the polling unit code.", "Tẹ kóòdù ibùdó ìdìbò.", "Tinye koodu ebe ntuli aka.", "Shigar da lambar rumfar zaɓe."],
    m_need_party: ["Add at least one party with a name and its votes.", "Add at least one party with name and votes.", "Fi ó kéré jù ẹgúbgẹ́ kan kún un pẹ̀lú orúkọ àti ìbò rẹ̀.", "Tinye opekata mpe otu otu nwere aha na votu ya.", "Ƙara aƙalla jam'iyya ɗaya mai suna da ƙuri'unta."],
    m_bad_num: ["Votes and the total must be whole numbers, like 12.", "Votes and the total must be whole numbers, like 12.", "Ìbò àti àpapọ̀ gbọ́dọ̀ jẹ́ nọ́ńbà odidi, bíí 12.", "Votu na ngụkọta ga-abụ ọnụọgụ zuru ezu, dịka 12.", "Ƙuri'u da jimilla dole su zama cikakkun lambobi, kamar 12."],
    m_dup: ["Each party name can be used only once.", "Each party name fit show only once.", "A lè lo orúkọ ẹgúbgẹ́ kọ̀ọ̀kan lẹ́ẹ̀kan péré.", "Enwere ike iji aha otu ọ bụla otu ugboro naanị.", "Ana iya amfani da kowane sunan jam'iyya sau ɗaya kawai."],
    m_none_q: ["No saved sheets to send.", "No saved sheet to send.", "Kò sí ìwé tí a fipamọ́ láti fi ráńṣẹ́.", "Enweghị mpempe akwụkwọ echekwara iji zipu.", "Babu takardun da aka adana da za a aika."],
    m_deleted: ["Saved sheets deleted.", "Dem don delete the saved sheets.", "A ti pa àwọn ìwé tí a fipamọ́ rẹ́.", "Ehichapụla mpempe akwụkwọ echekwara.", "An share takardun da aka adana."],
    m_online: ["Network is back. Enter your access code and press “Send saved sheets”.", "Network don come back. Enter your access code and press “Send saved sheets”.", "Nẹ́tíwọ́kì ti padà. Tẹ kóòdù àǹfààní rẹ kí o sì tẹ “Fi àwọn ìwé tí a fipamọ́ ráńṣẹ́”.", "Netwọkọ alaghachila. Tinye koodu ịbanye gị ma pịa “Zipu mpempe akwụkwọ echekwara”.", "Sadarwa ta dawo. Shigar da lambar shigarka ka danna “Aika takardun da aka adana”."],
    m_img_fail: ["We could not prepare the photo: {msg}", "We no fit prepare the photo: {msg}", "A kò lè múrà fọ́tò sílẹ̀: {msg}", "Anyị enweghị ike ikwadebe foto ahụ: {msg}", "Ba mu iya shirya hoton ba: {msg}"],
    m_img_big: ["The photo is still over 2 MB after shrinking. Choose a smaller photo.", "The photo still pass 2 MB after we shrink am. Choose smaller photo.", "Fọ́tò náà ṣì ju 2 MB lọ lẹ́yìn títúnṣe. Yan fọ́tò tó kéré jù.", "Foto ahụ ka karịrị 2 MB mgbe a mere ya obere. Họrọ foto ka nta.", "Hoton har yanzu ya fi 2 MB bayan rage shi. Zaɓi ƙaramin hoto."],
    m_img_unavail: ["This browser cannot prepare photos.", "Dis browser no fit prepare photos.", "Aṣàwákìí yìní kò lè múrà fọ́tò sílẹ̀.", "Ihe nchọgharị a enweghị ike ikwadebe foto.", "Wannan burauzar ba ta iya shirya hotuna ba."],
    r_title: ["Your receipt", "Your receipt", "Ìwé ẹ̀rí rẹ", "Akwụkwọ nnata gị", "Rasidin ka"],
    r_note: ["Keep this. It shows the sheet was received.", "Keep this. E show say dem receive the sheet.", "Pa èyí mọ́. Ó fi hàn pé a gba ìwé náà.", "Debe nke a. Ọ na-egosi na anabatara mpempe akwụkwọ ahụ.", "Ka ajiye wannan. Yana nuna an karɓi takardar."],
    r_tech: ["Show receipt details", "Show receipt details", "Fi àlàyè ìwé ẹ̀rí hàn", "Gosi nkọwa akwụkwọ nnata", "Nuna cikakkun bayanan rasidi"]
  };

  var lang = "en";

  function fmt(s, v) {
    if (!v) return s;
    return s.replace(/\{(\w+)\}/g, function (m, k) { return v[k] === undefined ? m : v[k]; });
  }
  function t(key, vars) {
    var e = D[key];
    if (!e) return key;
    return fmt(e[L.indexOf(lang)] || e[0], vars);
  }
  function each(sel, fn) {
    var n = document.querySelectorAll(sel);
    for (var i = 0; i < n.length; i++) fn(n[i]);
  }
  function refresh() {
    document.documentElement.setAttribute("lang", lang);
    each("[data-i18n]", function (el) { el.textContent = t(el.getAttribute("data-i18n")); });
    each("[data-i18n-attr]", function (el) {
      el.getAttribute("data-i18n-attr").split(";").forEach(function (pair) {
        var p = pair.split(":");
        if (p.length === 2) el.setAttribute(p[0].trim(), t(p[1].trim()));
      });
    });
    var key = document.documentElement.getAttribute("data-title-key");
    if (key) document.title = t(key);
    var sel = document.getElementById("lang");
    if (sel) sel.value = lang;
  }
  function setLang(code) {
    if (L.indexOf(code) < 0) code = "en";
    lang = code;
    try { localStorage.setItem("auditchain-lang", code); } catch (e) { /* storage unavailable */ }
    refresh();
    document.dispatchEvent(new Event("ac-lang"));
  }

  window.AC = { t: t, refresh: refresh, setLang: setLang, keys: D, langs: L };

  var saved = "en";
  try { saved = localStorage.getItem("auditchain-lang") || "en"; } catch (e) { /* ignore */ }
  if (L.indexOf(saved) < 0) saved = "en";
  lang = saved;
  refresh();
  var sel = document.getElementById("lang");
  if (sel) sel.addEventListener("change", function () { setLang(sel.value); });
})();
