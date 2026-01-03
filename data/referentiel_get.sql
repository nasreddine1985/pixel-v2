/*=====================================================================

  Query – All reference data for a flow (by FLOW_CODE)

  Master table : TIB_AUDIT_TEC.REF_FLOW

  ---------------------------------------------------------------

  Ajout :

   • Processus fonctionnel principal (via REF_FLOW.FUNC_PROCESS_ID)

   • Chaîne des fonctions du processus (REF_FUNC_PROCESS_DEF)

   • Détails de chaque fonction (REF_FUNCTION)

=====================================================================*/
 
SELECT

    /* -----------------------------------------------------------------

       1️⃣  Flow (master)

       ----------------------------------------------------------------- */

    f.FLOW_ID,

    f.FLOW_CODE,

    f.FLOW_NAME,

    f.FLOW_DIRECTION,

    f.ENABLE_FLG,

    f.CREATION_DTE,

    f.UPDATE_DTE,

    f.MAX_FILE_SIZE,
 
    /* Application */

    a.APPLICATION_ID,

    a.APPLICATION_CODE,

    a.APPLICATION_NAME,
 
    /* Flow type */

    ft.FLOW_TYP_ID,

    ft.FLOW_TYP_NAME,
 
    /* Technical process */

    tp.TECH_PROCESS_ID,

    tp.TECH_PROCESS_NAME,
 
    /* -----------------------------------------------------------------

       2️⃣  Countries attached to the flow (CSV)

       ----------------------------------------------------------------- */

    ( SELECT LISTAGG(c.COUNTRY_NAME, ', ') WITHIN GROUP (ORDER BY c.COUNTRY_NAME)

        FROM TIB_AUDIT_TEC.REF_FLOW_COUNTRY fc

        JOIN TIB_AUDIT_TEC.REF_COUNTRY c

          ON c.COUNTRY_ID = fc.COUNTRY_ID

       WHERE fc.FLOW_ID = f.FLOW_ID

    ) AS FLOW_COUNTRIES,
 
    /* -----------------------------------------------------------------

       3️⃣  Flow‑rule (REF_FLOW_RULES)

       ----------------------------------------------------------------- */

    fr.FLOWCODE                AS RULE_FLOWCODE,          -- = f.FLOW_CODE

    fr.TRANSPORTTYPE,

    fr.ISUNITARY,

    fr.PRIORITY,

    fr.URGENCY,

    fr.FLOWCONTROLLEDENABLED,

    fr.FLOWMAXIMUM,

    fr.FLOWRETENTIONENABLED,

    fr.RETENTIONCYCLEPERIOD,

    fr.WRITE_FILE,

    fr.MINREQUIREDFILESIZE,

    fr.IGNOREOUTPUTDUPCHECK,

    fr.LOGALL,
 
    /* -----------------------------------------------------------------

       4️⃣  Functional process (main) – via REF_FLOW.FUNC_PROCESS_ID

       ----------------------------------------------------------------- */

    fp_main.FUNC_PROCESS_ID,

    fp_main.FUNC_PROCESS_NAME,
 
    /* -----------------------------------------------------------------

       5️⃣  Chaîne des fonctions du processus (REF_FUNC_PROCESS_DEF)

          – on assemble les noms de fonction dans l’ordre indiqué

       ----------------------------------------------------------------- */

    ( SELECT LISTAGG(fn.FUNCTION_NAME, ', ')

               WITHIN GROUP (ORDER BY fpd.FUNCTION_PROCESS_ORDER)

        FROM TIB_AUDIT_TEC.REF_FUNC_PROCESS_DEF fpd

        JOIN TIB_AUDIT_TEC.REF_FUNCTION          fn

          ON fn.FUNCTION_ID = fpd.FUNCTION_ID

       WHERE fpd.FUNC_PROCESS_ID = f.FUNC_PROCESS_ID

    ) AS PROCESS_FUNCTION_CHAIN,
 
    /* -----------------------------------------------------------------

       6️⃣  Priorities (if any) – REF_FUNC_PROCESS_PRTY + REF_PRTY_FLOW

       ----------------------------------------------------------------- */

    ( SELECT LISTAGG(pr.PRTY_FLOW_NAME || '=' || fp_prty.FLOW_PRTY_VALUE,

                     '; ')

               WITHIN GROUP (ORDER BY fp_prty.PRTY_FLOW_ID)

        FROM TIB_AUDIT_TEC.REF_FUNC_PROCESS_PRTY fp_prty

        JOIN TIB_AUDIT_TEC.REF_PRTY_FLOW          pr

          ON pr.PRTY_FLOW_ID = fp_prty.PRTY_FLOW_ID

       WHERE fp_prty.FUNC_PROCESS_ID = f.FUNC_PROCESS_ID

    ) AS PROCESS_PRIORITIES,
 
    /* -----------------------------------------------------------------

       7️⃣  Partner (one row per partner of the flow)

       ----------------------------------------------------------------- */

    p.PARTNER_ID,

    p.PARTNER_CODE,

    p.PARTNER_NAME,

    pt.PARTNER_TYPE_ID,

    pt.PARTNER_TYPE_NAME,
 
    fp.PARTNER_DIRECTION,

    fp.CREATION_DTE            AS PARTNER_CREATION_DTE,

    fp.UPDATE_DTE              AS PARTNER_UPDATE_DTE,

    fp.RULE_ID                 AS PARTNER_RULE_ID,

    fp.CHARSET_ENCODING_ID,
 
    /* -----------------------------------------------------------------

       8️⃣  Transport – generic columns

       ----------------------------------------------------------------- */

    tr.TRANSPORT_ID,

    tr.TRANSPORT_TYP,
 
    /* -----------------------------------------------------------------

       9️⃣  Transport‑specific columns (LEFT‑JOINed)

       ----------------------------------------------------------------- */

    tc.CFT_IDF,

    tc.CFT_PARTNER_CODE,
 
    te.EMAIL_NAME,

    te.EMAIL_FROM,

    te.EMAIL_RECIPIENT_TO,

    te.EMAIL_RECIPIENT_CC,

    te.EMAIL_SUBJECT,

    te.HAS_ATTACHMENT,
 
    th.HTTP_URI,

    th.HTTP_PARTNER_CODE,

    th.CLIENT_PORT,

    th.CLIENT_HOST,

    th.CLIENT_METHOD,

    th.CLIENT_BODY,                     -- column exists in REF_TRANSPORT_HTTP

    th.ISSECURE,

    th.EXPECTEDCODE_SUCCES,

    th.APIGEE_HOST,

    th.APIGEE_PORT,

    th.APIGEE_URI,

    th.APIGEE_METHOD,

    th.THROUGHPUT_MAX,

    th.NB_RETRY,
 
    tj.JMS_Q_NAME,
 
    tm.MQS_Q_NAME,

    tm.MQS_Q_MANAGER,
 
    /* -----------------------------------------------------------------

      10️⃣  Charset encoding for the partner (if any)

      ----------------------------------------------------------------- */

    cs.CHARSET_ENCODING_ID,

    cs.CHARSET_CODE,

    cs.CHARSET_DESC,
 
    /* -----------------------------------------------------------------

      11️⃣  Replacement set (if any)

      ----------------------------------------------------------------- */

    fsr.SET_REPLACEMENT_ID,

    sr.SET_REPLACEMENT_NAME,

    sr.SET_REPLACEMENT_DESC,
 
    /* -----------------------------------------------------------------

      12️⃣  Route definition (if any)

      ----------------------------------------------------------------- */

    rd.NEXT_MODULE_ID,

    ndm.MODULE_NAME          AS NEXT_MODULE_NAME,

    rd.NEXT_FUNC_ID,

    nf.FUNCTION_NAME         AS NEXT_FUNCTION_NAME,

    rd.NEXT_FLOW_ID,

    nf2.FLOW_CODE            AS NEXT_FLOW_CODE,

    rd.RULE_ID               AS VERIFY_RULE_ID,

    vr.RULE_NAME             AS VERIFY_RULE_NAME
 
FROM TIB_AUDIT_TEC.REF_FLOW               f

/* ---------- 1️⃣  Flow master ---------- */

LEFT JOIN TIB_AUDIT_TEC.REF_APPLICATION   a  ON a.APPLICATION_ID = f.APPLICATION_ID

LEFT JOIN TIB_AUDIT_TEC.REF_FLOW_TYP      ft ON ft.FLOW_TYP_ID    = f.FLOW_TYP_ID

LEFT JOIN TIB_AUDIT_TEC.REF_TECH_PROCESS  tp ON tp.TECH_PROCESS_ID = f.TECH_PROCESS_ID
 
/* ---------- 3️⃣  Flow‑rule ---------- */

LEFT JOIN TIB_AUDIT_TEC.REF_FLOW_RULES    fr ON fr.FLOWCODE      = f.FLOW_CODE
 
/* ---------- 4️⃣  Functional process (main) ---------- */

LEFT JOIN TIB_AUDIT_TEC.REF_FUNC_PROCESS  fp_main

       ON fp_main.FUNC_PROCESS_ID = f.FUNC_PROCESS_ID
 
/* ---------- 7️⃣  Partner ---------- */

LEFT JOIN TIB_AUDIT_TEC.REF_FLOW_PARTNER  fp ON fp.FLOW_ID         = f.FLOW_ID

LEFT JOIN TIB_AUDIT_TEC.REF_PARTNER       p  ON p.PARTNER_ID       = fp.PARTNER_ID

LEFT JOIN TIB_AUDIT_TEC.REF_PARTNER_TYP   pt ON pt.PARTNER_TYPE_ID = p.PARTNER_TYPE_ID
 
/* ---------- 8️⃣  Transport (generic) ---------- */

LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT      tr  ON tr.TRANSPORT_ID = fp.TRANSPORT_ID
 
/* ---------- 9️⃣  Transport‑specific tables ---------- */

LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_CFT   tc  ON tc.TRANSPORT_ID = tr.TRANSPORT_ID

LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_EMAIL te  ON te.TRANSPORT_ID = tr.TRANSPORT_ID

LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_HTTP  th  ON th.TRANSPORT_ID = tr.TRANSPORT_ID

LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_JMS   tj  ON tj.TRANSPORT_ID = tr.TRANSPORT_ID

LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_MQS   tm  ON tm.TRANSPORT_ID = tr.TRANSPORT_ID
 
/* ---------- 10️⃣  Charset (optional) ---------- */

LEFT JOIN TIB_AUDIT_TEC.REF_CHARSET_ENCODING cs

       ON cs.CHARSET_ENCODING_ID = fp.CHARSET_ENCODING_ID
 
/* ---------- 11️⃣  Replacement set (optional) ---------- */

LEFT JOIN TIB_AUDIT_TEC.REF_FUNC_SET_RPLC   fsr ON fsr.PARTNER_ID = p.PARTNER_ID

                                             AND fsr.FLOW_ID    = f.FLOW_ID

LEFT JOIN TIB_AUDIT_TEC.REF_SET_REPLACEMENT sr  ON sr.SET_REPLACEMENT_ID = fsr.SET_REPLACEMENT_ID
 
/* ---------- 12️⃣  Route definition (optional) ---------- */

LEFT JOIN TIB_AUDIT_TEC.REF_ROUTE_DEF       rd   ON rd.TECH_PROCESS_ID = f.TECH_PROCESS_ID

                                                       -- adapt the “current module” criteria if needed

LEFT JOIN TIB_AUDIT_TEC.REF_MODULE          ndm  ON ndm.MODULE_ID = rd.NEXT_MODULE_ID

LEFT JOIN TIB_AUDIT_TEC.REF_FUNCTION        nf   ON nf.FUNCTION_ID = rd.NEXT_FUNC_ID

LEFT JOIN TIB_AUDIT_TEC.REF_FLOW            nf2  ON nf2.FLOW_ID = rd.NEXT_FLOW_ID

LEFT JOIN TIB_AUDIT_TEC.REF_ROUTE_RULE      vr   ON vr.RULE_ID = rd.RULE_ID
 
WHERE f.FLOW_CODE = :p_flow_code               -- le flow recherché

ORDER BY f.FLOW_ID,

         p.PARTNER_ID;
 
 