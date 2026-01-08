package com.pixel.v2.referential.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pixel.v2.referential.entity.RefFlow;

/**
 * Repository for complex referential queries that match the SQL structure
 */
@Repository
public interface RefFlowRepository extends JpaRepository<RefFlow, Integer> {

    /**
     * Complex query that matches the referential_get.sql structure Returns all referential data for
     * a specific flow code
     */
    @Query(value = """
            SELECT
                f.FLOW_ID,
                f.FLOW_CODE,
                f.FLOW_NAME,
                f.FLOW_DIRECTION,
                f.ENABLE_FLG,
                f.CREATION_DTE,
                f.UPDATE_DTE,
                f.MAX_FILE_SIZE,

                -- Application
                a.APPLICATION_ID,
                a.APPLICATION_NAME,

                -- Flow type
                ft.FLOW_TYP_ID,
                ft.FLOW_TYP_NAME,

                -- Technical process
                tp.TECH_PROCESS_ID,
                tp.TECH_PROCESS_NAME,

                -- Countries (CSV)
                (SELECT STRING_AGG(c.COUNTRY_NAME, ', ')
                 FROM TIB_AUDIT_TEC.REF_FLOW_COUNTRY fc
                 JOIN TIB_AUDIT_TEC.REF_COUNTRY c ON c.COUNTRY_ID = fc.COUNTRY_ID
                 WHERE fc.FLOW_ID = f.FLOW_ID) AS FLOW_COUNTRIES,

                -- Flow rules
                fr.FLOWCODE AS RULE_FLOWCODE,
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

                -- Functional process
                fp_main.FUNC_PROCESS_ID,
                fp_main.FUNC_PROCESS_NAME,

                -- Functional process properties
                fpp.FLOW_PRTY_VALUE,
                pf.PRTY_FLOW_NAME,
                pf.PRTY_FLOW_DESC,
                pf.PRTY_FLOW_TYP,

                -- Partner information
                p.PARTNER_ID,
                p.PARTNER_CODE,
                p.PARTNER_NAME,
                pt.PARTNER_TYPE_ID,
                pt.PARTNER_TYPE_NAME,

                fp.PARTNER_DIRECTION,
                fp.CREATION_DTE AS PARTNER_CREATION_DTE,
                fp.UPDATE_DTE AS PARTNER_UPDATE_DTE,
                fp.RULE_ID AS PARTNER_RULE_ID,
                fp.CHARSET_ENCODING_ID,
                fp.ENABLE_OUT,
                fp.ENABLE_BMSA,

                -- Transport generic
                tr.TRANSPORT_ID,
                tr.TRANSPORT_TYP,

                -- Transport specific
                tc.CFT_IDF,
                tc.CFT_PARTNER_CODE,

                te.EMAIL_NAME,
                te.EMAIL_FROM,
                te.EMAIL_RECIPIENT_TO,
                te.EMAIL_RECIPIENT_CC,
                te.EMAIL_SUBJECT,
                te.HAS_ATTACHMENT,

                th.HTTP_URI,
                th.CLIENT_METHOD,

                tj.JMS_Q_NAME,

                tm.MQS_Q_NAME,
                tm.MQS_Q_MANAGER,

                -- Charset encoding
                cs.CHARSET_ENCODING_ID,
                cs.CHARSET_CODE,
                cs.CHARSET_DESC

            FROM TIB_AUDIT_TEC.REF_FLOW f
            LEFT JOIN TIB_AUDIT_TEC.REF_APPLICATION a ON a.APPLICATION_ID = f.APPLICATION_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_FLOW_TYP ft ON ft.FLOW_TYP_ID = f.FLOW_TYP_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_TECH_PROCESS tp ON tp.TECH_PROCESS_ID = f.TECH_PROCESS_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_FLOW_RULES fr ON fr.FLOWCODE = f.FLOW_CODE
            LEFT JOIN TIB_AUDIT_TEC.REF_FUNC_PROCESS fp_main ON fp_main.FUNC_PROCESS_ID = f.FUNC_PROCESS_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_FUNC_PROCESS_PRTY fpp ON fpp.FUNC_PROCESS_ID = fp_main.FUNC_PROCESS_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_PRTY_FLOW pf ON pf.PRTY_FLOW_ID = fpp.PRTY_FLOW_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_FLOW_PARTNER fp ON fp.FLOW_ID = f.FLOW_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_PARTNER p ON p.PARTNER_ID = fp.PARTNER_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_PARTNER_TYP pt ON pt.PARTNER_TYPE_ID = p.PARTNER_TYPE_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT tr ON tr.TRANSPORT_ID = fp.TRANSPORT_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_CFT tc ON tc.TRANSPORT_ID = tr.TRANSPORT_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_EMAIL te ON te.TRANSPORT_ID = tr.TRANSPORT_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_HTTP th ON th.TRANSPORT_ID = tr.TRANSPORT_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_JMS tj ON tj.TRANSPORT_ID = tr.TRANSPORT_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_TRANSPORT_MQS tm ON tm.TRANSPORT_ID = tr.TRANSPORT_ID
            LEFT JOIN TIB_AUDIT_TEC.REF_CHARSET_ENCODING cs ON cs.CHARSET_ENCODING_ID = fp.CHARSET_ENCODING_ID
            WHERE f.FLOW_CODE = :flowCode
            ORDER BY f.FLOW_ID, p.PARTNER_ID
            """,
            nativeQuery = true)
    List<Map<String, Object>> findCompleteReferentialByFlowCode(@Param("flowCode") String flowCode);

    /**
     * Find flow by flow code with basic entity mapping
     */
    @Query("SELECT f FROM RefFlow f WHERE f.flowCode = :flowCode")
    RefFlow findByFlowCode(@Param("flowCode") String flowCode);
}
