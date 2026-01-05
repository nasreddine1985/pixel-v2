package com.pixelv2.techpivot;

import java.time.OffsetDateTime;
import java.util.*;

/**
 * PivotService - XSD aligned skeleton (TechnicalPivot)
 *
 * Pivot lives in exchangeProperty.tp as Map<String,Object>.
 *
 * Operations used by k-techpivot kamelet:
 * - ensure   : init skeleton if absent (does not overwrite existing pivot)
 * - putPath  : set value at dot-path
 * - addToList: append item to list at dot-path
 * - mergeInto: deep merge patch map into map at dot-path
 * - getPath  : read value at dot-path (optional)
 *
 * Notes:
 * - Imported types (func:Properties, func:Rule) are left as null / free-form Maps.
 * - For xs:choice structures, we provide a "skeleton map" for each branch to simplify later enrichment.
 */
public class PivotService {

    // ==========================================================
    // ENSURE
    // ==========================================================
    @SuppressWarnings("unchecked")
    public Map<String, Object> ensure(
            Map<String, Object> tp,
            String flowCode,
            Object occurId,
            Object fileName,
            Object kameletFlowId) {

        if (tp != null) return tp;

        tp = new LinkedHashMap<>();

        // Root (tech:TechnicalPivot)
        tp.put("FileName", fileName != null ? fileName.toString() : null);
        tp.put("FileContent", null);

        tp.put("Flow", newFlowSkeleton(flowCode));
        tp.put("Input", newPartnerSkeleton());
        tp.put("Output", new ArrayList<>());              // maxOccurs unbounded
        tp.put("Properties", null);                       // func:Properties (import)
        tp.put("Split", null);                            // tech:Split
        tp.put("Generic", null);                          // xs:anyType
        tp.put("ParentFlow", null);                       // recursion => keep null
        tp.put("Parking", null);                          // tech:Parking
        tp.put("FlowRules", newFlowRulesSkeleton());      // tech:FlowRules

        // Meta (non-XSD but useful)
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("occurId", occurId);
        meta.put("kameletFlowId", kameletFlowId);
        meta.put("ts", OffsetDateTime.now().toString());
        meta.put("schema", "TechnicalPivot");
        tp.put("_meta", meta);

        return tp;
    }

    // ==========================================================
    // PUT / ADD / MERGE / GET
    // ==========================================================
    public void putPath(Map<String, Object> tp, String path, String value, String valueType) {
        if (tp == null || isBlank(path)) return;
        Object converted = convert(value, valueType);
        setByPath(tp, path, converted);
    }

    @SuppressWarnings("unchecked")
    public void addToList(Map<String, Object> tp, String path, Object item) {
        if (tp == null || isBlank(path)) return;
        Object target = getOrCreateByPath(tp, path, List.class);
        if (target instanceof List<?>) ((List<Object>) target).add(item);
    }

    @SuppressWarnings("unchecked")
    public void mergeInto(Map<String, Object> tp, String path, Object patch) {
        if (tp == null || isBlank(path) || !(patch instanceof Map)) return;
        Object target = getOrCreateByPath(tp, path, Map.class);
        if (target instanceof Map<?, ?>) deepMerge((Map<String, Object>) target, (Map<String, Object>) patch);
    }

    public Object getPath(Map<String, Object> tp, String path) {
        if (tp == null || isBlank(path)) return null;
        String[] parts = path.split("\\.");
        Object current = tp;
        for (String p : parts) {
            if (!(current instanceof Map)) return null;
            current = ((Map<?, ?>) current).get(p);
            if (current == null) return null;
        }
        return current;
    }

    // ==========================================================
    // XSD Skeleton Builders
    // ==========================================================

    private static Map<String, Object> newFlowSkeleton(String flowCode) {
        Map<String, Object> flow = new LinkedHashMap<>();
        flow.put("FlowID", null);
        flow.put("FlowCode", isBlank(flowCode) ? null : flowCode);
        flow.put("FlowName", null);
        flow.put("FlowTypeName", null);
        flow.put("FlowDirection", null);
        flow.put("FlowEnabled", null);

        // FlowFuncPrty* => list of {Key, Value}
        flow.put("FlowFuncPrty", new ArrayList<>());

        // Countries => { Country: [ ... ] }
        flow.put("Countries", new LinkedHashMap<>(Map.of("Country", new ArrayList<>())));

        // ExternalServices => { ExternalService: [ ... ] }
        // ExternalService skeleton includes Request/Reply/LateReply/FeedbackReply (RequestReply)
        flow.put("ExternalServices", new LinkedHashMap<>(Map.of("ExternalService", new ArrayList<>())));

        return flow;
    }

    /**
     * ExternalService skeleton (tech:ExternalService) as Map.
     * Not inserted by default in ensure(), but useful when you add items to Flow.ExternalServices.ExternalService list.
     */
    public Map<String, Object> newExternalServiceSkeleton() {
        Map<String, Object> es = new LinkedHashMap<>();
        es.put("ServiceCode", null);
        es.put("SnapshotKey", null);
        es.put("Request", newRequestReplySkeleton());
        es.put("Reply", null);
        es.put("LateReply", null);
        es.put("FeedbackReply", null);
        es.put("TimeoutSoft", null);
        es.put("TimeoutHard", null);
        return es;
    }

    private static Map<String, Object> newRequestReplySkeleton() {
        Map<String, Object> rr = new LinkedHashMap<>();
        rr.put("Transport", newJmsSkeleton()); // tech:JMS (QName)
        rr.put("Content", null);               // xs:anyType
        return rr;
    }

    private static Map<String, Object> newJmsSkeleton() {
        return new LinkedHashMap<>(Map.of("QName", null));
    }

    /**
     * Partner skeleton (tech:Partner)
     * Transport is xs:choice. We keep all branches available (easier enrichment),
     * and you can set only the relevant branch during processing.
     */
    private static Map<String, Object> newPartnerSkeleton() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("FileName", null);
        p.put("PartnerCode", null);
        p.put("PartnerName", null);
        p.put("PartnerTypeName", null);
        p.put("CharsetCode", null);

        // Transport (choice)
        Map<String, Object> transport = new LinkedHashMap<>();

        transport.put("CFT", new LinkedHashMap<>(Map.of(
                "IDF", null,
                "PartnerCode", null,
                "DirectoryName", null,
                "PrefFileName", null
        )));

        transport.put("MQS", new LinkedHashMap<>(Map.of(
                "QManager", null,
                "QName", null
        )));

        transport.put("JMS", newJmsSkeleton());

        transport.put("HTTP", new LinkedHashMap<>(Map.of(
                "URI", null,
                "PartnerCode", null,
                "ClientPORT", null,
                "ClientHOST", null,
                "ClientMETHOD", null,
                "ClientBODY", null,
                "isSecure", null,
                "ExpectedCodeSucces", null,
                "ApigeeHOST", null,
                "ApigeePORT", null,
                "ApigeeURI", null,
                "ApigeeMETHOD", null,
                "throughput_Max", null,
                "nb_retry", null
        )));

        // EMAIL + ReplacementContent (Properties + attachments)
        Map<String, Object> email = new LinkedHashMap<>();
        email.put("email_name", null);
        email.put("from", null);
        email.put("to", null);
        email.put("cc", null);
        email.put("subject", null);
        email.put("body", null);
        email.put("signature", null);
        email.put("attachement", null); // xs:boolean in XSD, stored as Boolean when you set it
        email.put("OwnerApp", null);
        email.put("Param", null);

        Map<String, Object> replacementContent = new LinkedHashMap<>();

        // Properties
        Map<String, Object> replProps = new LinkedHashMap<>();
        // SimpleProperty* => list of {Value, Key}
        replProps.put("SimpleProperty", new ArrayList<>());
        // ComplexProperty* => list of {lines[], key}
        replProps.put("ComplexProperty", new ArrayList<>());
        replacementContent.put("Properties", replProps);

        // attachments.attachment* => list of attachment entries
        // each attachment: attachmentName?, contentType, (textAttachmentContent? | binaryAttachmentContent?)
        Map<String, Object> attachments = new LinkedHashMap<>();
        attachments.put("attachment", new ArrayList<>());
        replacementContent.put("attachments", attachments);

        email.put("ReplacementContent", replacementContent);

        transport.put("EMAIL", email);

        transport.put("SFTP", new LinkedHashMap<>(Map.of(
                "DirectoryName", null,
                "PrefFileName", null
        )));

        p.put("Transport", transport);

        // func:Rule (import) - not defined here
        p.put("Rule", null);

        p.put("PartnerEnabled", null);
        p.put("BMSAEnabled", null);

        return p;
    }

    /**
     * Split skeleton (tech:Split). Not set by default (tp.Split=null), but provided for convenience.
     */
    public Map<String, Object> newSplitSkeleton() {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("ElementIndex", null);
        s.put("IsLast", null);
        s.put("ParentIndex", null);
        s.put("Type", null);
        s.put("PayLoad", null);
        s.put("FuncKey", null);
        s.put("ParentSplit", null); // recursion
        s.put("IsDummy", null);
        return s;
    }

    /**
     * Parking skeleton (tech:Parking). Not set by default (tp.Parking=null), but provided for convenience.
     */
    public Map<String, Object> newParkingSkeleton() {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("SnapshotKey", null);

        // Restart choice: either Delay* or Timestamp
        Map<String, Object> restart = new LinkedHashMap<>();
        restart.put("Delay", new ArrayList<>());     // list of tech:Delay
        restart.put("Timestamp", null);              // xs:dateTime as String/OffsetDateTime
        p.put("Restart", restart);

        p.put("IgnoreSnapshotNotFound", null);

        Map<String, Object> postParking = new LinkedHashMap<>();
        postParking.put("UnparkWithNewOccurrenceId", null);
        postParking.put("RemoveProperties", null);   // func:Properties (import)
        postParking.put("AddProperties", null);      // func:Properties (import)
        p.put("PostParking", postParking);

        return p;
    }

    /**
     * Delay skeleton (tech:Delay) - choice of one of DelayInSeconds/Minutes/Hours/Days.
     * Not inserted by default.
     */
    public Map<String, Object> newDelaySkeleton() {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("DelayInSeconds", null);
        d.put("DelayInMinutes", null);
        d.put("DelayInHours", null);
        d.put("DelayInDays", null);
        return d;
    }

    private static Map<String, Object> newFlowRulesSkeleton() {
        Map<String, Object> flowRules = new LinkedHashMap<>();

        flowRules.put("FlowSeparation", new LinkedHashMap<>(Map.of(
                "JMSPriority", null,
                "UsePayload", null,
                "Urgency", null
        )));

        flowRules.put("FlowControl", new LinkedHashMap<>(Map.of(
                "FlowControlEnabled", null,
                "FlowMaximum", null
        )));

        flowRules.put("FlowRetention", new LinkedHashMap<>(Map.of(
                "FlowRetentionEnabled", null,
                "RetentionCyclePeriod", null,
                "FlowStatus", null
        )));

        return flowRules;
    }

    // ==========================================================
    // Helpers
    // ==========================================================

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static Object convert(String value, String type) {
        if (value == null) return null;
        if (type == null) type = "string";
        return switch (type) {
            case "int" -> value.isBlank() ? null : Integer.valueOf(value);
            case "long" -> value.isBlank() ? null : Long.valueOf(value);
            case "bool" -> value.isBlank() ? null : Boolean.valueOf(value);
            default -> value;
        };
    }

    @SuppressWarnings("unchecked")
    private static void setByPath(Map<String, Object> root, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            current = (Map<String, Object>) current.computeIfAbsent(parts[i], k -> new LinkedHashMap<String, Object>());
        }
        current.put(parts[parts.length - 1], value);
    }

    @SuppressWarnings("unchecked")
    private static Object getOrCreateByPath(Map<String, Object> root, String path, Class<?> leafType) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = root;

        for (int i = 0; i < parts.length; i++) {
            String key = parts[i];
            boolean last = (i == parts.length - 1);

            Object next = current.get(key);

            if (last) {
                if (next == null) {
                    next = (leafType == List.class)
                            ? new ArrayList<>()
                            : new LinkedHashMap<String, Object>();
                    current.put(key, next);
                }
                return next;
            }

            if (!(next instanceof Map)) {
                next = new LinkedHashMap<String, Object>();
                current.put(key, next);
            }
            current = (Map<String, Object>) next;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static void deepMerge(Map<String, Object> target, Map<String, Object> patch) {
        for (Map.Entry<String, Object> e : patch.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            Object existing = target.get(key);

            if (value instanceof Map && existing instanceof Map) {
                deepMerge((Map<String, Object>) existing, (Map<String, Object>) value);
            } else {
                target.put(key, value);
            }
        }
    }
}
