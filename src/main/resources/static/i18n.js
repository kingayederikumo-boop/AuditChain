/* AuditChain language layer. Translates the console and submit page in the browser.
 * English is the source text. Draft translations: native-speaker review is pending (see docs/LANGUAGES.md).
 * Ijaw (ijc) is wired in but has no dictionary yet, so it stays disabled until a native translator supplies one.
 */
(function () {
  "use strict";

  var LANGS = [["en", "English"], ["pcm", "Naijá Pidgin"], ["yo", "Yorùbá"], ["ig", "Igbo"], ["ha", "Hausa"], ["ijc", "Ịjọ (Ijaw) — translator needed"]];
  var COL = { yo: 1, ig: 2, ha: 3 };
  var ATTRS = ["placeholder", "aria-label", "title"];

  // [English, Yoruba, Igbo, Hausa]
  var X = [
    ["Language", "Èdè", "Asụsụ", "Harshe"],
    ["Integrity console", "Pánẹ́ẹ̀lì àyẹ̀wò òtítọ́", "Ebe nlele eziokwu", "Dandalin duba gaskiyar bayanai"],
    ["Workspace", "Ibi iṣẹ́", "Ebe ọrụ", "Wurin aiki"],
    ["Workspace /", "Ibi iṣẹ́ /", "Ebe ọrụ /", "Wurin aiki /"],
    ["Main navigation", "Ìtọ́sọ́nà pàtàkì", "Nchọgharị bụ isi", "Babban jagora"],
    ["Overview", "Àkópọ̀", "Nchịkọta", "Taƙaitawa"],
    ["Result sheets", "Ìwé àbájáde", "Mpempe akwụkwọ nsonaazụ", "Takardun sakamako"],
    ["Submit evidence", "Fi ẹ̀rí ránṣẹ́", "Zipu akaebe", "Aika shaida"],
    ["Environment", "Àyíká", "Gburugburu", "Muhalli"],
    ["Prototype environment", "Àyíká àdánwò", "Gburugburu nnwale", "Muhallin gwaji"],
    ["Synthetic records · Read-only console", "Àwọn àkọsílẹ̀ àdàkọ · Kíkà nìkan", "Ndekọ e mepụtara · Ịgụ naanị", "Bayanan gwaji · Karatu kawai"],
    ["Live checks", "Àyẹ̀wò lọ́wọ́lọ́wọ́", "Nlele ugbu a", "Bincike kai tsaye"],
    ["Refresh", "Sọ di tuntun", "Megharịa", "Sabunta"],
    ["Integrity overview", "Àkópọ̀ òtítọ́ àkọsílẹ̀", "Nchịkọta eziokwu ndekọ", "Taƙaitaccen gaskiyar bayanai"],
    ["Monitoring / Integrity", "Ìṣọ́ / Òtítọ́", "Nlekọta / Eziokwu", "Sa ido / Gaskiya"],
    ["A clear view of whether recorded result sheets still match their verified history.", "Àfihàn kedere bóyá àwọn ìwé àbájáde tí a kọ sílẹ̀ ṣì bá ìtàn tí a ti jẹ́rìí sí mu.", "Ọhụụ doro anya nke ma mpempe akwụkwọ nsonaazụ edekọrọ ka kwekọrọ na akụkọ ejiri nyochaa.", "Bayyanannen kallo kan ko takardun sakamakon da aka rubuta har yanzu sun yi daidai da tarihin da aka tabbatar."],
    ["Waiting for first check", "Ń dúró de àyẹ̀wò àkọ́kọ́", "Na-eche nlele mbụ", "Ana jiran bincike na farko"],
    ["Current integrity status", "Ipò òtítọ́ lọ́wọ́lọ́wọ́", "Ọnọdụ eziokwu ugbu a", "Halin gaskiyar bayanai yanzu"],
    ["Checking records…", "Ń ṣàyẹ̀wò àwọn àkọsílẹ̀…", "Na-elele ndekọ…", "Ana duba bayanai…"],
    ["The console is checking the record chain and published checkpoints.", "Pánẹ́ẹ̀lì yìí ń ṣàyẹ̀wò ẹ̀wọ̀n àkọsílẹ̀ àti àwọn ibùdó ìjẹ́rìí tí a tẹ̀jáde.", "Ebe a na-elele yinye ndekọ na ebe nyocha ekwupụtara.", "Dandalin yana duba jerin bayanai da wuraren tabbatarwa da aka wallafa."],
    ["Checks run directly against the audit history", "A ṣe àyẹ̀wò náà tààrà lórí ìtàn àyẹ̀wò", "A na-elele ya ozugbo megide akụkọ nyocha", "Ana yin binciken kai tsaye a kan tarihin bincike"],
    ["Verification checks", "Àyẹ̀wò ìjẹ́rìísí", "Nlele nkwenye", "Binciken tabbatarwa"],
    ["Live status", "Ipò lọ́wọ́lọ́wọ́", "Ọnọdụ ugbu a", "Halin yanzu"],
    ["Record chain", "Ẹ̀wọ̀n àkọsílẹ̀", "Yinye ndekọ", "Jerin bayanai"],
    ["Waiting for the record check.", "Ń dúró de àyẹ̀wò àkọsílẹ̀.", "Na-eche nlele ndekọ.", "Ana jiran binciken bayanai."],
    ["Public checkpoints", "Àwọn ibùdó ìjẹ́rìí gbangba", "Ebe nyocha ọha", "Wuraren tabbatarwa na jama'a"],
    ["Waiting for the checkpoint check.", "Ń dúró de àyẹ̀wò ibùdó ìjẹ́rìí.", "Na-eche nlele ebe nyocha.", "Ana jiran binciken wuraren tabbatarwa."],
    ["Recorded entries", "Àwọn àkọsílẹ̀ tí a kọ", "Ndekọ edekọrọ", "Bayanan da aka rubuta"],
    ["Items in the audit history", "Àwọn nǹkan inú ìtàn àyẹ̀wò", "Ihe dị n'akụkọ nyocha", "Abubuwan da ke cikin tarihin bincike"],
    ["Signed history snapshots found", "Àwọn àwòrán ìtàn tí a fọwọ́ sí tí a rí", "Foto akụkọ e binyere aka hụrụ", "Hotunan tarihi masu sa hannu da aka samu"],
    ["Waiting to sync", "Ń dúró láti ṣọ̀kan", "Na-eche ịkwekọrịta", "Ana jiran daidaitawa"],
    ["Submissions waiting for delivery", "Àwọn ìfisílẹ̀ tó ń dúró láti dé", "Ntinye na-eche izipu", "Abubuwan da ake jiran aikawa"],
    ["Needs attention", "Nílò àfiyèsí", "Chọrọ nlebara anya", "Yana buƙatar kulawa"],
    ["Figures or AI checks need review", "Àwọn nọ́ńbà tàbí àyẹ̀wò AI nílò àtúnyẹ̀wò", "Ọnụọgụ ma ọ bụ nlele AI chọrọ nyochaghachi", "Alƙaluma ko binciken AI suna buƙatar sake dubawa"],
    ["Evidence needing attention", "Ẹ̀rí tó nílò àfiyèsí", "Akaebe chọrọ nlebara anya", "Shaidar da ke buƙatar kulawa"],
    ["Arithmetic checks and AI flags are review signals, not findings of fraud.", "Àyẹ̀wò ìṣírò àti àmì AI jẹ́ àmì fún àtúnyẹ̀wò, kì í ṣe ìdájọ́ pé ẹ̀tàn wà.", "Nlele mgbakọ na mwepụ na ọkọlọtọ AI bụ ihe ngosi maka nyochaghachi, ọ bụghị nchọpụta aghụghọ.", "Binciken lissafi da alamun AI sigina ne na sake dubawa, ba hukuncin cewa an yi zamba ba."],
    ["Checking evidence…", "Ń ṣàyẹ̀wò ẹ̀rí…", "Na-elele akaebe…", "Ana duba shaida…"],
    ["Recorded result sheets", "Àwọn ìwé àbájáde tí a kọsílẹ̀", "Mpempe akwụkwọ nsonaazụ edekọrọ", "Takardun sakamakon da aka rubuta"],
    ["Evidence records received by AuditChain, newest first.", "Àwọn àkọsílẹ̀ ẹ̀rí tí AuditChain gbà, tuntun lákọ̀ọ́kọ́.", "Ndekọ akaebe AuditChain natara, nke ọhụrụ buru ụzọ.", "Bayanan shaida da AuditChain ta karɓa, sababbi da farko."],
    ["Submit evidence ↗", "Fi ẹ̀rí ránṣẹ́ ↗", "Zipu akaebe ↗", "Aika shaida ↗"],
    ["Loading result sheets…", "Ń gbé àwọn ìwé àbájáde wọlé…", "Na-ebufe mpempe akwụkwọ nsonaazụ…", "Ana ɗora takardun sakamako…"],
    ["Search records", "Wá àwọn àkọsílẹ̀", "Chọọ ndekọ", "Nemo bayanai"],
    ["Search result sheets", "Wá àwọn ìwé àbájáde", "Chọọ mpempe akwụkwọ nsonaazụ", "Nemo takardun sakamako"],
    ["Loading records…", "Ń gbé àwọn àkọsílẹ̀ wọlé…", "Na-ebufe ndekọ…", "Ana ɗora bayanai…"],
    ["Technical audit details", "Àlàyé àyẹ̀wò ní ìtọ́ni ẹ̀rọ", "Nkọwa nyocha teknụzụ", "Bayanan fasaha na bincike"],
    ["Loading technical details…", "Ń gbé àlàyé wọlé…", "Na-ebufe nkọwa…", "Ana ɗora bayanan fasaha…"],
    ["Prototype scope: records shown here are synthetic test data. A successful integrity check confirms that recorded history matches its checksums and available checkpoints; it does not prove that the original figures submitted were truthful or that an election outcome is correct.", "Ààlà àdánwò: àwọn àkọsílẹ̀ tí a fihàn níbí jẹ́ dátà àdàkọ fún àdánwò. Àyẹ̀wò òtítọ́ tó yege fi hàn pé ìtàn tí a kọsílẹ̀ bá àwọn àmì ìdánilójú àti àwọn ibùdó ìjẹ́rìí tó wà mu; kò fi hàn pé òtítọ́ ni àwọn nọ́ńbà tí a kọ́kọ́ fi sílẹ̀, tàbí pé èsì ìdìbò tọ̀nà.", "Oke nnwale: ndekọ ndị e gosiri ebe a bụ data nnwale e mepụtara. Nlele eziokwu gara nke ọma na-egosi na akụkọ edekọrọ kwekọrọ na ihe ngosi nchekwa na ebe nyocha dị; ọ naghị egosi na ọnụọgụ izizi e zitere bụ eziokwu ma ọ bụ na nsonaazụ ntuli aka ziri ezi.", "Iyakar gwaji: bayanan da aka nuna a nan na gwaji ne. Nasarar binciken gaskiya tana tabbatar da cewa tarihin da aka rubuta ya yi daidai da lambobin tabbatarwa da wuraren tabbatarwa da ake da su; ba ta tabbatar da cewa alƙaluman farko da aka aika gaskiya ne ba, ko cewa sakamakon zaɓe daidai ne."],
    ["AuditChain · Electoral records integrity prototype", "AuditChain · Àdánwò òtítọ́ àkọsílẹ̀ ìdìbò", "AuditChain · Nnwale eziokwu ndekọ ntuli aka", "AuditChain · Gwajin gaskiyar bayanan zaɓe"],
    ["For demonstration and authorized testing only", "Fún ìfihàn àti àdánwò tí a fún láṣẹ nìkan", "Maka ngosi na nnwale ekwere naanị", "Don nuni da gwaji mai izini kawai"],

    ["Status unavailable", "Ipò kò sí", "Ọnọdụ adịghị", "Halin bai samu ba"],
    ["The record-chain check could not be completed. Refresh before relying on this status.", "A kò lè parí àyẹ̀wò ẹ̀wọ̀n àkọsílẹ̀. Sọ di tuntun kí o tó gbẹ́kẹ̀lé ipò yìí.", "Enweghị ike imecha nlele yinye ndekọ. Megharịa tupu ịtụkwasị obi n'ọnọdụ a.", "Ba a iya kammala binciken jerin bayanai ba. Sabunta kafin ka dogara da wannan hali."],
    ["The service did not return a complete result", "Iṣẹ́ náà kò dá èsì pípé padà", "Ọrụ ahụ enyeghị nsonaazụ zuru ezu", "Sabis ɗin bai mayar da cikakken sakamako ba"],
    ["Integrity issue detected", "A rí ìṣòro òtítọ́", "Achọpụtara nsogbu eziokwu", "An gano matsalar gaskiyar bayanai"],
    ["The record chain did not pass verification. The affected history should be investigated.", "Ẹ̀wọ̀n àkọsílẹ̀ kò yege àyẹ̀wò. Ó yẹ kí a ṣèwádìí ìtàn tí ọ̀rọ̀ kàn.", "Yinye ndekọ agabeghị nlele. Kwesịrị inyocha akụkọ metụtara.", "Jerin bayanai bai ci binciken tabbatarwa ba. Ya kamata a bincika tarihin da abin ya shafa."],
    ["A broken record link was reported", "A ròyìn ìjápọ̀ àkọsílẹ̀ tó ti já", "A kọrọ njikọ ndekọ gbajiri agbaji", "An ba da rahoton karyewar mahaɗin bayanai"],
    ["Checkpoint mismatch", "Ibùdó ìjẹ́rìí kò bá mu", "Ebe nyocha adịghị kwekọrịta", "Wurin tabbatarwa bai yi daidai ba"],
    ["A stored signed checkpoint does not match the current record history. Investigate before relying on it.", "Ibùdó ìjẹ́rìí tí a fọwọ́ sí tí a fi pamọ́ kò bá ìtàn àkọsílẹ̀ lọ́wọ́lọ́wọ́ mu. Ṣèwádìí kí o tó gbẹ́kẹ̀lé e.", "Ebe nyocha e binyere aka echekwara adịghị kwekọ na akụkọ ndekọ ugbu a. Nyochaa tupu ịtụkwasị ya obi.", "Wurin tabbatarwa mai sa hannu da aka ajiye bai dace da tarihin bayanai na yanzu ba. A bincika kafin a dogara da shi."],
    ["A local checkpoint comparison failed", "Ìfiwéra ibùdó ìjẹ́rìí inú ẹ̀rọ kùnà", "Ntụnyere ebe nyocha n'ime ya dara", "Kwatanta wurin tabbatarwa na cikin gida ya gaza"],
    ["Public checkpoint mismatch", "Ibùdó ìjẹ́rìí gbangba kò bá mu", "Ebe nyocha ọha adịghị kwekọrịta", "Wurin tabbatarwa na jama'a bai yi daidai ba"],
    ["The published checkpoint history did not pass comparison with the current record chain.", "Ìtàn ibùdó ìjẹ́rìí tí a tẹ̀jáde kò yege ìfiwéra pẹ̀lú ẹ̀wọ̀n àkọsílẹ̀ lọ́wọ́lọ́wọ́.", "Akụkọ ebe nyocha ekwupụtara agabeghị ntụnyere na yinye ndekọ ugbu a.", "Tarihin wuraren tabbatarwa da aka wallafa bai ci kwatance da jerin bayanai na yanzu ba."],
    ["A public checkpoint comparison failed", "Ìfiwéra ibùdó ìjẹ́rìí gbangba kùnà", "Ntụnyere ebe nyocha ọha dara", "Kwatanta wurin tabbatarwa na jama'a ya gaza"],
    ["Records intact", "Àwọn àkọsílẹ̀ wà láìyípadà", "Ndekọ ka zuru oke", "Bayanai ba su canja ba"],
    ["The record chain and available signed public checkpoints match. No alteration was detected by these checks.", "Ẹ̀wọ̀n àkọsílẹ̀ àti àwọn ibùdó ìjẹ́rìí gbangba tí a fọwọ́ sí bá ara wọn mu. Àyẹ̀wò wọ̀nyí kò rí ìyípadà kankan.", "Yinye ndekọ na ebe nyocha ọha e binyere aka dị kwekọrọ. Nlele ndị a ahụghị mgbanwe ọ bụla.", "Jerin bayanai da wuraren tabbatarwa na jama'a masu sa hannu da ake da su sun yi daidai. Waɗannan binciken ba su gano wani sauyi ba."],
    ["Both integrity checks passed", "Àyẹ̀wò òtítọ́ méjèèjì yege", "Nlele eziokwu abụọ gafere", "Binciken gaskiya biyu duka sun ci"],
    ["Chain intact · Partial verification", "Ẹ̀wọ̀n wà láìyípadà · Ìjẹ́rìísí díẹ̀", "Yinye zuru oke · Nkwenye akụkụ", "Jerin bai canja ba · Tabbatarwa ta ɓangare"],
    ["The local record chain passes. A usable public checkpoint history was not confirmed, so independent verification is incomplete.", "Ẹ̀wọ̀n àkọsílẹ̀ inú ẹ̀rọ yege. A kò jẹ́rìí ìtàn ibùdó ìjẹ́rìí gbangba tó ṣeé lò, nítorí náà ìjẹ́rìísí àdáyébá kò pé.", "Yinye ndekọ n'ime ya gafere. Akwenyeghị akụkọ ebe nyocha ọha ọ bụla nwere ike iji, ya mere nkwenye nnọọ onwe ya erughị eru.", "Jerin bayanai na cikin gida ya ci. Ba a tabbatar da tarihin wuraren tabbatarwa na jama'a da za a iya amfani da shi ba, don haka tabbatarwa mai zaman kanta ba ta cika ba."],
    ["The local check passed; public verification remains incomplete", "Àyẹ̀wò inú ẹ̀rọ yege; ìjẹ́rìísí gbangba kò tíì pé", "Nlele n'ime ya gafere; nkwenye ọha erughị eru", "Binciken cikin gida ya ci; tabbatarwar jama'a ba ta cika ba"],
    ["Status unknown", "Ipò kò ṣe é mọ̀", "Amaghị ọnọdụ", "Ba a san halin ba"],
    ["The available information is not sufficient to determine the integrity of the records.", "Ìròyìn tó wà kò tó láti pinnu òtítọ́ àwọn àkọsílẹ̀.", "Ozi dị adịghị ezu iji chọpụta eziokwu ndekọ.", "Bayanan da ake da su ba su isa a tantance gaskiyar bayanan ba."],
    ["Verification did not return a conclusive result", "Ìjẹ́rìísí kò mú èsì tó dájú wá", "Nkwenye enyeghị nsonaazụ doro anya", "Tabbatarwar ba ta bayar da sakamako ƙwaƙƙwara ba"],
    ["Checking live integrity…", "Ń ṣàyẹ̀wò òtítọ́ lọ́wọ́lọ́wọ́…", "Na-elele eziokwu ugbu a…", "Ana duba gaskiyar bayanai kai tsaye…"],
    ["Live checks in progress", "Àyẹ̀wò ń lọ lọ́wọ́", "Nlele na-aga n'ihu", "Ana ci gaba da bincike"],
    ["Could not load result sheets.", "A kò lè gbé àwọn ìwé àbájáde wọlé.", "Enweghị ike ibufe mpempe akwụkwọ nsonaazụ.", "Ba a iya ɗora takardun sakamako ba."],
    ["Arithmetic check unavailable", "Àyẹ̀wò ìṣírò kò sí", "Nlele mgbakọ adịghị", "Binciken lissafi bai samu ba"],
    ["Figures differ", "Àwọn nọ́ńbà yàtọ̀", "Ọnụọgụ dị iche", "Alƙaluma sun bambanta"],
    ["Needs review", "Nílò àtúnyẹ̀wò", "Chọrọ nyochaghachi", "Yana buƙatar sake dubawa"],
    ["Totals match", "Àpapọ̀ bá ara mu", "Ngụkọta kwekọrọ", "Jimloli sun yi daidai"],
    ["Not checked", "A kò tíì ṣàyẹ̀wò", "Elebeghị anya", "Ba a duba ba"],
    ["No result sheets match that search.", "Kò sí ìwé àbájáde tó bá ìwádìí yẹn mu.", "Enweghị mpempe akwụkwọ nsonaazụ kwekọrọ n'ọchụchọ ahụ.", "Babu takardar sakamako da ta dace da wannan nema."],
    ["No result sheets have been recorded yet.", "A kò tíì kọ ìwé àbájáde kankan sílẹ̀.", "Edebeghị mpempe akwụkwọ nsonaazụ ọ bụla.", "Ba a riga an rubuta takardar sakamako ba."],
    ["No records currently need attention based on the available arithmetic and AI checks.", "Kò sí àkọsílẹ̀ tó nílò àfiyèsí báyìí ní ìbámu pẹ̀lú àyẹ̀wò ìṣírò àti AI tó wà.", "Enweghị ndekọ chọrọ nlebara anya ugbu a dabere na nlele mgbakọ na AI dị.", "Babu wani bayani da ke buƙatar kulawa yanzu bisa binciken lissafi da AI da ake da su."],
    ["The automated check requested human review.", "Àyẹ̀wò aládàáṣiṣẹ́ béèrè fún àtúnyẹ̀wò ènìyàn.", "Nlele akpaaka rịọrọ nyochaghachi mmadụ.", "Binciken na'ura ya nemi sake dubawar ɗan adam."],
    ["Awaiting a human review decision.", "Ń dúró de ìpinnu àtúnyẹ̀wò ènìyàn.", "Na-eche mkpebi nyochaghachi mmadụ.", "Ana jiran hukuncin sake dubawar ɗan adam."],
    ["Awaiting review", "Ń dúró àtúnyẹ̀wò", "Na-eche nyochaghachi", "Ana jiran dubawa"],
    ["Review suggested", "A dámọ̀ràn àtúnyẹ̀wò", "A na-atụ aro nyochaghachi", "An ba da shawarar dubawa"],
    ["A stored signed checkpoint does not match the current chain.", "Ibùdó ìjẹ́rìí tí a fọwọ́ sí tí a fi pamọ́ kò bá ẹ̀wọ̀n lọ́wọ́lọ́wọ́ mu.", "Ebe nyocha e binyere aka echekwara adịghị kwekọ na yinye ugbu a.", "Wurin tabbatarwa mai sa hannu da aka ajiye bai dace da jerin na yanzu ba."],
    ["No usable public checkpoints were confirmed. Independent verification is incomplete.", "A kò jẹ́rìí ibùdó ìjẹ́rìí gbangba tó ṣeé lò. Ìjẹ́rìísí àdáyébá kò pé.", "Akwenyeghị ebe nyocha ọha ọ bụla nwere ike iji. Nkwenye nnọọ onwe ya erughị eru.", "Ba a tabbatar da wani wurin tabbatarwa na jama'a da za a iya amfani da shi ba. Tabbatarwa mai zaman kanta ba ta cika ba."],
    ["The published checkpoint comparison did not pass.", "Ìfiwéra ibùdó ìjẹ́rìí tí a tẹ̀jáde kò yege.", "Ntụnyere ebe nyocha ekwupụtara agabeghị.", "Kwatancen wuraren tabbatarwa da aka wallafa bai ci ba."],

    ["← Back to overview", "← Padà sí àkópọ̀", "← Laghachi na nchịkọta", "← Koma taƙaitawa"],
    ["Field operations / Evidence intake", "Iṣẹ́ pápá / Gbígba ẹ̀rí", "Ọrụ ubi / Nnata akaebe", "Ayyukan fili / Karɓar shaida"],
    ["Submit a result sheet", "Fi ìwé àbájáde ránṣẹ́", "Zipu mpempe akwụkwọ nsonaazụ", "Aika takardar sakamako"],
    ["Send a result record and its supporting image to the audit history. If the connection drops, the submission can remain queued on this device.", "Fi àkọsílẹ̀ àbájáde àti àwòrán rẹ̀ ránṣẹ́ sí ìtàn àyẹ̀wò. Bí ìkànpọ̀ bá já, ìfisílẹ̀ náà lè dúró nínú ìlà lórí ẹ̀rọ yìí.", "Zipu ndekọ nsonaazụ na foto na-akwado ya n'akụkọ nyocha. Ọ bụrụ na njikọ ịntanetị bipụrụ, ntinye ahụ nwere ike ịnọgide na-eche n'ngwaọrụ a.", "Aika bayanin sakamako da hotonsa zuwa tarihin bincike. Idan haɗin intanet ya katse, za a iya barin abin da aka aika a layi a wannan na'ura."],
    ["Prototype only.", "Àdánwò nìkan.", "Nnwale naanị.", "Gwaji kawai."],
    ["Use synthetic test data. Do not submit real election records or personal information. Offline items remain on this device until sent or deleted.", "Lo àwọn dátà àdánwò àdàkọ. Má ṣe fi àwọn àkọsílẹ̀ ìdìbò gidi tàbí ìròyìn ara ẹni ránṣẹ́. Àwọn nǹkan aláìsopọ̀ yóò wà lórí ẹ̀rọ yìí títí a ó fi fi wọ́n ránṣẹ́ tàbí paárẹ́.", "Jiri data nnwale e mepụtara. Ezila ndekọ ntuli aka n'ezie ma ọ bụ ozi nkeonwe. Ihe ndị anọghị n'ịntanetị ga-anọgide n'ngwaọrụ a ruo mgbe e zitere ma ọ bụ hichapụ ha.", "Yi amfani da bayanan gwaji na ƙirƙira. Kada ka aika ainihin bayanan zaɓe ko bayanan sirri. Abubuwan da ba a aika ba za su kasance a wannan na'ura har sai an aika ko an goge su."],
    ["Submission details", "Àlàyé ìfisílẹ̀", "Nkọwa ntinye", "Bayanan aikawa"],
    ["Fields marked required must be completed before sending.", "Gbọ́dọ̀ parí àwọn ààyè tí a fi àmì “pàtàkì” sí kí a tó fi ránṣẹ́.", "Ebe ndị akara dị mkpa ga-emerịrị tupu izipu.", "Dole a cika filayen da aka yi wa alamar wajibi kafin aikawa."],
    ["Officer API key", "Kọ́kọ́rọ́ API òṣìṣẹ́", "Igodo API onye ọrụ", "Makullin API na jami'i"],
    ["Used to authenticate this submission. Kept in memory only and never saved.", "A lò ó láti jẹ́rìí ìfisílẹ̀ yìí. A pa á mọ́ sínú ìrántí nìkan, a kì í fi pamọ́.", "A na-eji ya enyocha ntinye a. Echekwa ya naanị n'ebe nchekwa nwa oge, ọ dịghị echekwa ya mgbe niile.", "Ana amfani da shi don tabbatar da wannan aikawa. Ana riƙe shi a ƙwaƙwalwa kawai, ba a adana shi."],
    ["Enter officer API key", "Tẹ kọ́kọ́rọ́ API òṣìṣẹ́ sí i", "Tinye igodo API onye ọrụ", "Shigar da makullin API na jami'i"],
    ["Polling unit code", "Kóòdù ibùdó ìdìbò", "Koodu ebe ntuli aka", "Lambar rumfar zaɓe"],
    ["Use the synthetic location identifier for this test record.", "Lo àmì ibùdó àdàkọ fún àkọsílẹ̀ àdánwò yìí.", "Jiri akara ebe e mepụtara maka ndekọ nnwale a.", "Yi amfani da lambar wuri ta gwaji don wannan bayani."],
    ["e.g. PU-001", "àpẹẹrẹ: PU-001", "dịka: PU-001", "misali: PU-001"],
    ["Result figures", "Àwọn nọ́ńbà àbájáde", "Ọnụọgụ nsonaazụ", "Alƙaluman sakamako"],
    ["Enter valid JSON. The declared total can use totalVotes, total_votes, or votes_cast.", "Tẹ JSON tó tọ́ sí i. Àpapọ̀ tí a kéde lè lo totalVotes, total_votes, tàbí votes_cast.", "Tinye JSON ziri ezi. Ngụkọta a kwupụtara nwere ike iji totalVotes, total_votes, ma ọ bụ votes_cast.", "Shigar da JSON mai inganci. Jimillar da aka bayyana na iya amfani da totalVotes, total_votes, ko votes_cast."],
    ["Result sheet image", "Àwòrán ìwé àbájáde", "Foto mpempe akwụkwọ nsonaazụ", "Hoton takardar sakamako"],
    ["PNG or JPEG. The image is resized, compressed, and stripped of metadata before it is queued.", "PNG tàbí JPEG. A máa dín àwòrán náà kù, a pọ̀n ọ́n, a sì yọ metadata rẹ̀ kúrò kí a tó fi sínú ìlà.", "PNG ma ọ bụ JPEG. A na-agbanwe nha foto ahụ, belata ya, wepụ metadata ya tupu etinye ya n'usoro.", "PNG ko JPEG. Ana rage girman hoton, a matse shi, a cire metadata ɗinsa kafin a saka shi a layi."],
    ["Offline submission queue", "Ìlà ìfisílẹ̀ aláìsopọ̀", "Usoro ntinye na-anọghị n'ịntanetị", "Layin aikawa na kashe-intanet"],
    ["Retry saved items", "Tún àwọn nǹkan tí a fipamọ́ gbìyànjú", "Nwaa ọzọ ihe echekwara", "Sake gwada abubuwan da aka ajiye"],
    ["Delete saved items", "Pa àwọn nǹkan tí a fipamọ́ rẹ́", "Hichapụ ihe echekwara", "Goge abubuwan da aka ajiye"],
    ["Each submission uses a unique idempotency key to prevent duplicate records when retrying. The queue is stored locally in this browser. Clearing browser storage may remove unsent items.", "Ìfisílẹ̀ kọ̀ọ̀kan ń lo kọ́kọ́rọ́ idempotency àrà ọ̀tọ̀ láti dènà àkọsílẹ̀ àdàrú nígbà àtúnṣe. A ń fi ìlà náà pamọ́ sínú aṣàwákiri yìí. Pípa ibi ìpamọ́ aṣàwákiri rẹ́ lè mú àwọn nǹkan tí a kò tíì rán kúrò.", "Ntinye ọ bụla na-eji igodo idempotency pụrụ iche iji gbochie ndekọ ugboro abụọ mgbe a na-anwa ọzọ. A na-echekwa usoro ahụ n'ime ihe nchọgharị a. Ihichapụ nchekwa ihe nchọgharị nwere ike iwepụ ihe ndị e zitebeghị.", "Kowane aikawa yana amfani da maɓallin idempotency na musamman don hana kwafin bayanai yayin sake gwadawa. Ana adana layin a wannan burauza. Share ajiyar burauza na iya cire abubuwan da ba a aika ba."],
    ["Ready. Your local queue has not been checked yet.", "Ó ti ṣetán. A kò tíì ṣàyẹ̀wò ìlà inú ẹ̀rọ rẹ.", "Ọ dị njikere. Elebeghị usoro dị n'ngwaọrụ gị anya.", "A shirye. Ba a duba layinka na na'ura ba tukuna."],
    ["Enter your officer API key to retry.", "Tẹ kọ́kọ́rọ́ API òṣìṣẹ́ rẹ sí i láti tún gbìyànjú.", "Tinye igodo API onye ọrụ gị iji nwaa ọzọ.", "Shigar da makullin API na jami'i domin sake gwadawa."],
    ["Network unavailable. Submission remains queued.", "Kò sí ìkànpọ̀ nẹ́tíwọ́ọ̀kì. Ìfisílẹ̀ náà ṣì wà nínú ìlà.", "Netwọk adịghị. Ntinye ahụ ka nọ n'usoro.", "Babu hanyar sadarwa. Aikawar tana nan a layi."],
    ["Submission accepted. Receipt displayed below.", "A gba ìfisílẹ̀. A fi ìwé ẹ̀rí gbígbà hàn ní ìsàlẹ̀.", "Anabatara ntinye. Egosiri akwụkwọ nnata n'okpuru.", "An karɓi aikawar. An nuna rasidi a ƙasa."],
    ["No queued submissions.", "Kò sí ìfisílẹ̀ nínú ìlà.", "Enweghị ntinye n'usoro.", "Babu aikawa a layi."],
    ["Select a PNG or JPEG image.", "Yan àwòrán PNG tàbí JPEG.", "Họrọ foto PNG ma ọ bụ JPEG.", "Zaɓi hoton PNG ko JPEG."],
    ["Figures must be valid JSON.", "Àwọn nọ́ńbà gbọ́dọ̀ jẹ́ JSON tó tọ́.", "Ọnụọgụ ga-abụrịrị JSON ziri ezi.", "Dole alƙaluman su zama JSON mai inganci."],
    ["Removing image metadata and compressing…", "Ń yọ metadata àwòrán kúrò, a sì ń pọ̀n ọ́n…", "Na-ewepụ metadata foto na-belata ya…", "Ana cire metadata na hoto ana matse shi…"],
    ["Privacy-processed image saved to offline queue.", "A fi àwòrán tí a ti dáàbò bo ìkọ̀kọ̀ rẹ̀ pamọ́ sínú ìlà aláìsopọ̀.", "Echekwara foto e wepụrụ ihe nzuzo n'usoro anọghị n'ịntanetị.", "An adana hoton da aka kare sirrinsa a layin kashe-intanet."],
    ["Local queue deleted.", "A ti pa ìlà inú ẹ̀rọ rẹ́.", "Ehichapụla usoro dị n'ngwaọrụ.", "An goge layin na'ura."],
    ["Connection restored. Enter API key and select Retry.", "A ti dá ìkànpọ̀ padà. Tẹ kọ́kọ́rọ́ API sí i kí o sì yan “Tún gbìyànjú”.", "Njikọ alaghachila. Tinye igodo API wee họrọ “Nwaa ọzọ”.", "An dawo da haɗin. Shigar da makullin API sannan ka zaɓi “Sake gwadawa”."],
    ["Local queue unavailable in this browser.", "Ìlà inú ẹ̀rọ kò sí nínú aṣàwákiri yìí.", "Usoro dị n'ime adịghị na ihe nchọgharị a.", "Layin na'ura bai samu a wannan burauza ba."]
  ];

  // [regex, Yoruba, Igbo, Hausa]; $1..$n are captured groups
  var P = [
    [/^Last checked (.+)$/, "Àyẹ̀wò ìkẹyìn: $1", "Nlele ikpeazụ: $1", "Binciken ƙarshe: $1"],
    [/^All (\d+) records link correctly\.$/, "Gbogbo àkọsílẹ̀ $1 ni a so pọ̀ dáadáa.", "Ndekọ niile $1 jikọtara nke ọma.", "Dukkan bayanai $1 sun haɗu daidai."],
    [/^A broken link was reported at record (.+)\.$/, "A ròyìn ìjápọ̀ tó ti já ní àkọsílẹ̀ $1.", "A kọrọ njikọ gbajiri agbaji na ndekọ $1.", "An ba da rahoton karyewar mahaɗi a bayani $1."],
    [/^A broken link was reported\.$/, "A ròyìn ìjápọ̀ tó ti já.", "A kọrọ njikọ gbajiri agbaji.", "An ba da rahoton karyewar mahaɗi."],
    [/^The chain check returned no conclusive result\.$/, "Àyẹ̀wò ẹ̀wọ̀n kò mú èsì tó dájú wá.", "Nlele yinye enyeghị nsonaazụ doro anya.", "Binciken jerin bai bayar da sakamako ƙwaƙƙwara ba."],
    [/^The record-chain check is unavailable \((.*)\)\.$/, "Àyẹ̀wò ẹ̀wọ̀n àkọsílẹ̀ kò sí ($1).", "Nlele yinye ndekọ adịghị ($1).", "Binciken jerin bayanai bai samu ba ($1)."],
    [/^The public checkpoint check is unavailable \((.*)\)\.$/, "Àyẹ̀wò ibùdó ìjẹ́rìí gbangba kò sí ($1).", "Nlele ebe nyocha ọha adịghị ($1).", "Binciken wuraren tabbatarwa na jama'a bai samu ba ($1)."],
    [/^Record #(\d+) · (.*)$/, "Àkọsílẹ̀ #$1 · $2", "Ndekọ #$1 · $2", "Bayani #$1 · $2"],
    [/^(.*) · Record #(\d+)$/, "$1 · Àkọsílẹ̀ #$2", "$1 · Ndekọ #$2", "$1 · Bayani #$2"],
    [/^Party total (.+) \/ Sheet total (.+)$/, "Àpapọ̀ ẹgbẹ́ $1 / Àpapọ̀ ìwé $2", "Ngụkọta òtù $1 / Ngụkọta mpempe akwụkwọ $2", "Jimillar jam'iyyu $1 / Jimillar takarda $2"],
    [/^(\d+) result sheets? recorded$/, "$1 ìwé àbájáde tí a kọsílẹ̀", "Mpempe akwụkwọ nsonaazụ $1 edekọrọ", "Takardun sakamako $1 da aka rubuta"],
    [/^(\d+) result sheets? matching search$/, "$1 ìwé àbájáde tó bá ìwádìí mu", "Mpempe akwụkwọ nsonaazụ $1 kwekọrọ n'ọchụchọ", "Takardun sakamako $1 da suka dace da nema"],
    [/^Party figures total (.+) while the sheet declares (.+)\.$/, "Àpapọ̀ àwọn nọ́ńbà ẹgbẹ́ jẹ́ $1 nígbà tí ìwé náà sọ pé $2.", "Ngụkọta ọnụọgụ òtù bụ $1 ebe mpempe akwụkwọ ahụ kwupụtara $2.", "Jimillar alƙaluman jam'iyyu ita ce $1 alhali takardar ta bayyana $2."],
    [/^(\d+) audit entries · Latest fingerprint (.*)\.$/, "$1 àkọsílẹ̀ àyẹ̀wò · Àmì ìdánimọ̀ tuntun $2.", "Ndekọ nyocha $1 · Akara mkpisi aka kacha ọhụrụ $2.", "Bayanan bincike $1 · Sabuwar alamar yatsa $2."],
    [/^Current fingerprint: (.*)$/, "Àmì ìdánimọ̀ lọ́wọ́lọ́wọ́: $1", "Akara mkpisi aka ugbu a: $1", "Alamar yatsa ta yanzu: $1"],
    [/^(\d+) signed public checkpoint\(s\) match the current chain\.$/, "$1 ibùdó ìjẹ́rìí gbangba tí a fọwọ́ sí bá ẹ̀wọ̀n lọ́wọ́lọ́wọ́ mu.", "Ebe nyocha ọha $1 e binyere aka kwekọrọ na yinye ugbu a.", "Wuraren tabbatarwa na jama'a masu sa hannu $1 sun yi daidai da jerin na yanzu."],
    [/^(\d+) submissions? currently saved on this device\.$/, "$1 ìfisílẹ̀ wà ní ìpamọ́ lórí ẹ̀rọ yìí lọ́wọ́lọ́wọ́.", "Ntinye $1 echekwara n'ngwaọrụ a ugbu a.", "Aikawa $1 a adana a wannan na'ura yanzu."],
    [/^Server returned HTTP (\d+)\. Item retained for review\.\s*(.*)$/, "Olùpèsè dá HTTP $1 padà. A pa nǹkan náà mọ́ fún àtúnyẹ̀wò. $2", "Sava weghachitere HTTP $1. Ejigidere ihe ahụ maka nyochaghachi. $2", "Sabar ta dawo da HTTP $1. An riƙe abin don dubawa. $2"],
    [/^Unable to process or save image: (.*)$/, "Kò ṣeé ṣe láti ṣe àtúnṣe tàbí pa àwòrán mọ́: $1", "Enweghị ike ịhazi ma ọ bụ chekwaa foto: $1", "Ba a iya sarrafawa ko adana hoton ba: $1"],
    [/^Retry error: (.*)$/, "Àṣìṣe àtúnṣe: $1", "Njehie nnwaa ọzọ: $1", "Kuskuren sake gwadawa: $1"]
  ];

  // Nigerian Pidgin: [English, Pidgin]
  var PX = [
    ["Integrity console", "Console wey dey check say records no change"],
    ["Workspace", "Work place"],
    ["Workspace /", "Work place /"],
    ["Main navigation", "Main menu"],
    ["Overview", "Quick look"],
    ["Result sheets", "Result papers"],
    ["Submit evidence", "Send evidence"],
    ["Environment", "Where e dey run"],
    ["Prototype environment", "Test environment"],
    ["Synthetic records · Read-only console", "Pretend records · Na only to read"],
    ["Live checks", "Checks wey dey run now"],
    ["Integrity overview", "Quick look: records still correct?"],
    ["Monitoring / Integrity", "Watch / Correctness"],
    ["A clear view of whether recorded result sheets still match their verified history.", "Clear view of whether the result sheets wey dem write down still match their history wey dem don confirm."],
    ["Waiting for first check", "We dey wait for first check"],
    ["Current integrity status", "How the records dey right now"],
    ["Checking records…", "We dey check the records…"],
    ["The console is checking the record chain and published checkpoints.", "We dey check the record chain and the public checkpoints wey dem don publish."],
    ["Checks run directly against the audit history", "We dey check am straight against the audit history"],
    ["Verification checks", "Wetin we check"],
    ["Live status", "Status now"],
    ["Waiting for the record check.", "We dey wait for record check."],
    ["Waiting for the checkpoint check.", "We dey wait for checkpoint check."],
    ["Recorded entries", "Records wey dem don write"],
    ["Items in the audit history", "Things wey dey audit history"],
    ["Signed history snapshots found", "Signed history snapshots wey we see"],
    ["Waiting to sync", "Dey wait to sync"],
    ["Submissions waiting for delivery", "Submissions wey still dey wait to go"],
    ["Needs attention", "Need attention"],
    ["Figures or AI checks need review", "Figures or AI checks wey need make person look again"],
    ["Evidence needing attention", "Evidence wey need attention"],
    ["Arithmetic checks and AI flags are review signals, not findings of fraud.", "Arithmetic checks and AI flags na only signal say make person look again; e no mean say person thief."],
    ["Checking evidence…", "We dey check evidence…"],
    ["Recorded result sheets", "Result sheets wey dem don record"],
    ["Evidence records received by AuditChain, newest first.", "Evidence records wey AuditChain collect, the newest dey first."],
    ["Submit evidence ↗", "Send evidence ↗"],
    ["Loading result sheets…", "We dey load result sheets…"],
    ["Loading records…", "We dey load records…"],
    ["Loading technical details…", "We dey load technical details…"],
    ["Prototype scope: records shown here are synthetic test data. A successful integrity check confirms that recorded history matches its checksums and available checkpoints; it does not prove that the original figures submitted were truthful or that an election outcome is correct.", "Test version only: the records wey you see here na pretend test data. If the integrity check pass, e mean say the history wey dem record match im checksums and the checkpoints wey dey; e no prove say the first figures wey dem send na true, or say election result correct."],
    ["AuditChain · Electoral records integrity prototype", "AuditChain · Test project for election records integrity"],
    ["For demonstration and authorized testing only", "For demo and authorised testing only"],
    ["Status unavailable", "Status no dey available"],
    ["The record-chain check could not be completed. Refresh before relying on this status.", "We no fit finish the record-chain check. Refresh before you trust this status."],
    ["The service did not return a complete result", "The service no give complete result"],
    ["Integrity issue detected", "We see wahala for the records"],
    ["The record chain did not pass verification. The affected history should be investigated.", "The record chain no pass verification. Make dem investigate the history wey e affect."],
    ["A broken record link was reported", "Dem report say one record link don break"],
    ["Checkpoint mismatch", "Checkpoint no match"],
    ["A stored signed checkpoint does not match the current record history. Investigate before relying on it.", "Signed checkpoint wey dem keep no match the record history wey dey now. Investigate am before you trust am."],
    ["A local checkpoint comparison failed", "Comparison of local checkpoint fail"],
    ["Public checkpoint mismatch", "Public checkpoint no match"],
    ["The published checkpoint history did not pass comparison with the current record chain.", "The public checkpoint history no pass comparison with the record chain wey dey now."],
    ["A public checkpoint comparison failed", "Comparison of public checkpoint fail"],
    ["Records intact", "Records dey correct"],
    ["The record chain and available signed public checkpoints match. No alteration was detected by these checks.", "The record chain and the signed public checkpoints wey dey match. These checks no see any change."],
    ["Both integrity checks passed", "The two integrity checks pass"],
    ["Chain intact · Partial verification", "Chain dey correct · Verification no complete"],
    ["The local record chain passes. A usable public checkpoint history was not confirmed, so independent verification is incomplete.", "The local record chain pass. We no confirm any public checkpoint history wey we fit use, so the independent verification no complete."],
    ["The local check passed; public verification remains incomplete", "Local check pass; public verification never complete"],
    ["Status unknown", "We no know the status"],
    ["The available information is not sufficient to determine the integrity of the records.", "The information wey we get no enough to know whether the records dey correct."],
    ["Verification did not return a conclusive result", "Verification no give clear result"],
    ["Checking live integrity…", "We dey check the records now…"],
    ["Live checks in progress", "Checks dey run"],
    ["Could not load result sheets.", "We no fit load result sheets."],
    ["Arithmetic check unavailable", "Arithmetic check no dey available"],
    ["Figures differ", "Figures no match"],
    ["Needs review", "Need review"],
    ["Not checked", "We never check am"],
    ["No result sheets match that search.", "No result sheet match wetin you search."],
    ["No result sheets have been recorded yet.", "Dem never record any result sheet."],
    ["No records currently need attention based on the available arithmetic and AI checks.", "No record need attention for now based on the arithmetic and AI checks wey dey."],
    ["The automated check requested human review.", "The automatic check ask make person look am."],
    ["Awaiting a human review decision.", "We dey wait for person to decide after review."],
    ["Awaiting review", "Dey wait for review"],
    ["Review suggested", "We suggest review"],
    ["A stored signed checkpoint does not match the current chain.", "Signed checkpoint wey dem keep no match the chain wey dey now."],
    ["No usable public checkpoints were confirmed. Independent verification is incomplete.", "We no confirm any public checkpoint wey we fit use. Independent verification no complete."],
    ["The published checkpoint comparison did not pass.", "The comparison of public checkpoint no pass."],
    ["← Back to overview", "← Go back to overview"],
    ["Field operations / Evidence intake", "Field work / Collect evidence"],
    ["Submit a result sheet", "Send result sheet"],
    ["Send a result record and its supporting image to the audit history. If the connection drops, the submission can remain queued on this device.", "Send result record and the picture wey support am go the audit history. If network cut, the submission fit stay for this device until network come back."],
    ["Prototype only.", "Test only."],
    ["Use synthetic test data. Do not submit real election records or personal information. Offline items remain on this device until sent or deleted.", "Use pretend test data. No send real election records or person's private information. Items wey never go go stay for this device until dem send am or you delete am."],
    ["Fields marked required must be completed before sending.", "Make you fill the fields wey dem mark required before you send."],
    ["Used to authenticate this submission. Kept in memory only and never saved.", "We dey use am to confirm this submission. E dey only for memory, we no dey save am."],
    ["Enter officer API key", "Put officer API key"],
    ["Use the synthetic location identifier for this test record.", "Use the pretend location code for this test record."],
    ["e.g. PU-001", "like this: PU-001"],
    ["Enter valid JSON. The declared total can use totalVotes, total_votes, or votes_cast.", "Put correct JSON. You fit use totalVotes, total_votes, or votes_cast for the total wey you declare."],
    ["Result sheet image", "Picture of result sheet"],
    ["PNG or JPEG. The image is resized, compressed, and stripped of metadata before it is queued.", "PNG or JPEG. We go reduce the picture size, compress am, and comot im metadata before we put am for queue."],
    ["Offline submission queue", "Queue for when network no dey"],
    ["Retry saved items", "Try the saved items again"],
    ["Delete saved items", "Delete saved items"],
    ["Each submission uses a unique idempotency key to prevent duplicate records when retrying. The queue is stored locally in this browser. Clearing browser storage may remove unsent items.", "Every submission get im own unique idempotency key so duplicate records no go show when you try again. The queue dey saved for this browser. If you clear browser storage, you fit lose items wey never go."],
    ["Ready. Your local queue has not been checked yet.", "We ready. We never check your local queue yet."],
    ["Enter your officer API key to retry.", "Put your officer API key to try again."],
    ["Network unavailable. Submission remains queued.", "Network no dey. Submission still dey for queue."],
    ["Submission accepted. Receipt displayed below.", "Dem don accept the submission. Receipt dey below."],
    ["No queued submissions.", "No submission dey for queue."],
    ["Select a PNG or JPEG image.", "Choose PNG or JPEG picture."],
    ["Figures must be valid JSON.", "Figures must be correct JSON."],
    ["Removing image metadata and compressing…", "We dey comot picture metadata and dey compress am…"],
    ["Privacy-processed image saved to offline queue.", "Picture wey we don protect im privacy don save for offline queue."],
    ["Local queue deleted.", "Local queue don delete."],
    ["Connection restored. Enter API key and select Retry.", "Network don come back. Put API key then press Retry."],
    ["Local queue unavailable in this browser.", "Local queue no dey work for this browser."]
  ];

  // Nigerian Pidgin patterns: [regex, Pidgin]
  var PP = [
    [/^Last checked (.+)$/, "Last time we check: $1"],
    [/^All (\d+) records link correctly\.$/, "All $1 records link well."],
    [/^A broken link was reported at record (.+)\.$/, "Dem report say link break for record $1."],
    [/^A broken link was reported\.$/, "Dem report say link break."],
    [/^The chain check returned no conclusive result\.$/, "The chain check no give clear result."],
    [/^The record-chain check is unavailable \((.*)\)\.$/, "Record-chain check no dey available ($1)."],
    [/^The public checkpoint check is unavailable \((.*)\)\.$/, "Public checkpoint check no dey available ($1)."],
    [/^Party total (.+) \/ Sheet total (.+)$/, "Party total $1 / Sheet total $2"],
    [/^(\d+) result sheets? recorded$/, "$1 result sheet dem don record"],
    [/^(\d+) result sheets? matching search$/, "$1 result sheet match wetin you search"],
    [/^Party figures total (.+) while the sheet declares (.+)\.$/, "Party figures add reach $1 but the sheet talk say na $2."],
    [/^(\d+) audit entries · Latest fingerprint (.*)\.$/, "$1 audit entries · Latest fingerprint $2."],
    [/^Current fingerprint: (.*)$/, "Fingerprint now: $1"],
    [/^(\d+) signed public checkpoint\(s\) match the current chain\.$/, "$1 signed public checkpoint match the chain wey dey now."],
    [/^(\d+) submissions? currently saved on this device\.$/, "$1 submission dey saved for this device now."],
    [/^Server returned HTTP (\d+)\. Item retained for review\.\s*(.*)$/, "Server reply HTTP $1. We keep the item make dem review am. $2"],
    [/^Unable to process or save image: (.*)$/, "We no fit process or save the picture: $1"],
    [/^Retry error: (.*)$/, "Error when we try again: $1"]
  ];

  var exact = {};
  X.forEach(function (r) { exact[r[0]] = r; });
  var pexact = {};
  PX.forEach(function (r) { pexact[r[0]] = r[1]; });

  function hasDict(code) { return COL[code] !== undefined || code === "pcm"; }

  function fill(tpl, m) {
    return tpl.replace(/\$(\d)/g, function (_, n) { return m[n] === undefined ? "" : m[n]; });
  }

  function one(core, code) {
    var i, m;
    if (code === "pcm") {
      if (pexact[core]) return pexact[core];
      for (i = 0; i < PP.length; i++) {
        m = core.match(PP[i][0]);
        if (m) return fill(PP[i][1], m);
      }
      return null;
    }
    var col = COL[code];
    var e = exact[core];
    if (e && e[col]) return e[col];
    for (i = 0; i < P.length; i++) {
      m = core.match(P[i][0]);
      if (m) return fill(P[i][col], m);
    }
    return null;
  }

  function tr(s, code) {
    if (code === "en" || !hasDict(code) || !s) return s;
    var m = s.match(/^(\s*)([\s\S]*?)(\s*)$/);
    var core = m[2];
    if (!core) return s;
    var out = one(core, code);
    if (out === null) {
      // Several sentences joined together (e.g. review reasons): translate each one.
      var pieces = core.match(/[^.!?]+[.!?]*\s*/g);
      if (pieces && pieces.length > 1) {
        var changed = false;
        var parts = pieces.map(function (p) {
          var t = p.trim();
          var r = one(t, code);
          if (r !== null) { changed = true; return r; }
          return t;
        });
        if (changed) out = parts.join(" ");
      }
    }
    return out === null ? s : m[1] + out + m[3];
  }

  var lang = "en";
  var origText = new WeakMap();
  var shownText = new WeakMap();
  var origAttr = new WeakMap();
  var SKIP = { SCRIPT: 1, STYLE: 1, TEXTAREA: 1, PRE: 1, OPTION: 1, SELECT: 1 };

  function applyText(n) {
    var p = n.parentNode;
    if (!p || SKIP[p.nodeName]) return;
    if (!(shownText.has(n) && shownText.get(n) === n.data)) origText.set(n, n.data);
    var t = tr(origText.get(n), lang);
    if (t !== n.data) n.data = t;
    shownText.set(n, n.data);
  }

  function applyAttrs(el) {
    ATTRS.forEach(function (a) {
      if (!el.hasAttribute(a)) return;
      var store = origAttr.get(el) || {};
      var cur = el.getAttribute(a);
      if (store[a] === undefined || store["shown_" + a] !== cur) store[a] = cur;
      var t = tr(store[a], lang);
      if (t !== cur) el.setAttribute(a, t);
      store["shown_" + a] = t;
      origAttr.set(el, store);
    });
  }

  function walk(node) {
    if (node.nodeType === 3) { applyText(node); return; }
    if (node.nodeType !== 1) return;
    if (node.nodeName === "SCRIPT" || node.nodeName === "STYLE") return;
    applyAttrs(node);
    for (var c = node.firstChild; c; c = c.nextSibling) walk(c);
  }

  function setLang(code) {
    if (code !== "en" && !hasDict(code)) code = "en";
    lang = code;
    document.documentElement.setAttribute("lang", code === "pcm" ? "pcm" : code);
    try { localStorage.setItem("auditchain-lang", code); } catch (e) { /* storage unavailable */ }
    walk(document.body);
    var sel = document.getElementById("lang");
    if (sel) sel.value = code;
  }

  function mount() {
    var css = document.createElement("style");
    css.textContent = "#lang{height:34px;max-width:190px;border:1px solid #303946;background:#11151b;color:#dce3ec;border-radius:7px;padding:0 8px;font:12px Arial,Helvetica,sans-serif}#lang:focus-visible{outline:2px solid #9bb2ff;outline-offset:3px}";
    document.head.appendChild(css);
    var sel = document.createElement("select");
    sel.id = "lang";
    sel.setAttribute("aria-label", "Language");
    LANGS.forEach(function (l) {
      var o = document.createElement("option");
      o.value = l[0];
      o.textContent = l[1];
      if (l[0] !== "en" && !hasDict(l[0])) o.disabled = true;
      sel.appendChild(o);
    });
    sel.addEventListener("change", function () { setLang(sel.value); });
    var host = document.querySelector(".top-actions") || document.querySelector(".topbar") || document.body;
    if (host.classList.contains("top-actions")) host.insertBefore(sel, host.firstChild); else host.appendChild(sel);
  }

  function start() {
    mount();
    var saved = "en";
    try { saved = localStorage.getItem("auditchain-lang") || "en"; } catch (e) { /* ignore */ }
    setLang(saved);
    new MutationObserver(function (records) {
      records.forEach(function (r) {
        if (r.type === "childList") r.addedNodes.forEach(walk);
        else if (r.type === "characterData") applyText(r.target);
        else if (r.type === "attributes") applyAttrs(r.target);
      });
    }).observe(document.body, { childList: true, subtree: true, characterData: true, attributes: true, attributeFilter: ATTRS });
  }

  if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", start); else start();
})();
