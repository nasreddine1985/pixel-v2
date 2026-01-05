package com.pixel.v2.referentiel.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO that matches the structure of referentiel-example.json Contains complete flow information
 * with partners, transports, and functional properties
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RefFlowDto {

    @JsonProperty("flow")
    private FlowInfo flow;

    @JsonProperty("partnerIn")
    private PartnerInfo partnerIn;

    @JsonProperty("partnerOut")
    private List<PartnerInfo> partnerOut;

    @JsonProperty("httpTransport")
    private HttpTransportInfo httpTransport;

    @JsonProperty("virtualAccount")
    private VirtualAccountInfo virtualAccount;

    @JsonProperty("functionalModules")
    private List<FunctionalModuleInfo> functionalModules;

    @JsonProperty("flowRules")
    private List<FlowRuleInfo> flowRules;

    // Nested classes for structured data
    public static class FlowInfo {
        @JsonProperty("FlowID")
        private String flowId;

        @JsonProperty("flowCode")
        private String flowCode;

        @JsonProperty("flowName")
        private String flowName;

        @JsonProperty("flowTypeName")
        private String flowTypeName;

        @JsonProperty("flowDirection")
        private String flowDirection;

        @JsonProperty("flowEnabled")
        private String flowEnabled;

        @JsonProperty("application")
        private ApplicationInfo application;

        @JsonProperty("flowFuncPrty")
        private List<FlowFuncPrtyInfo> flowFuncPrty;

        @JsonProperty("countries")
        private List<String> countries;

        // Getters and setters
        public String getFlowId() {
            return flowId;
        }

        public void setFlowId(String flowId) {
            this.flowId = flowId;
        }

        public String getFlowCode() {
            return flowCode;
        }

        public void setFlowCode(String flowCode) {
            this.flowCode = flowCode;
        }

        public String getFlowName() {
            return flowName;
        }

        public void setFlowName(String flowName) {
            this.flowName = flowName;
        }

        public String getFlowTypeName() {
            return flowTypeName;
        }

        public void setFlowTypeName(String flowTypeName) {
            this.flowTypeName = flowTypeName;
        }

        public String getFlowDirection() {
            return flowDirection;
        }

        public void setFlowDirection(String flowDirection) {
            this.flowDirection = flowDirection;
        }

        public String getFlowEnabled() {
            return flowEnabled;
        }

        public void setFlowEnabled(String flowEnabled) {
            this.flowEnabled = flowEnabled;
        }

        public ApplicationInfo getApplication() {
            return application;
        }

        public void setApplication(ApplicationInfo application) {
            this.application = application;
        }

        public List<FlowFuncPrtyInfo> getFlowFuncPrty() {
            return flowFuncPrty;
        }

        public void setFlowFuncPrty(List<FlowFuncPrtyInfo> flowFuncPrty) {
            this.flowFuncPrty = flowFuncPrty;
        }

        public List<String> getCountries() {
            return countries;
        }

        public void setCountries(List<String> countries) {
            this.countries = countries;
        }
    }

    public static class ApplicationInfo {
        @JsonProperty("code")
        private String code;

        @JsonProperty("name")
        private String name;

        // Getters and setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class FlowFuncPrtyInfo {
        @JsonProperty("key")
        private String key;

        @JsonProperty("type")
        private String type;

        @JsonProperty("desc")
        private String desc;

        @JsonProperty("value")
        private String value;

        // Getters and setters
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class PartnerInfo {
        @JsonProperty("partnerCode")
        private String partnerCode;

        @JsonProperty("partnerName")
        private String partnerName;

        @JsonProperty("partnerTypeName")
        private String partnerTypeName;

        @JsonProperty("charsetCode")
        private String charsetCode;

        @JsonProperty("ruleName")
        private String ruleName;

        @JsonProperty("enabled")
        private String enabled;

        @JsonProperty("bmsaEnabled")
        private String bmsaEnabled;

        @JsonProperty("transport")
        private TransportInfo transport;

        // Getters and setters
        public String getPartnerCode() {
            return partnerCode;
        }

        public void setPartnerCode(String partnerCode) {
            this.partnerCode = partnerCode;
        }

        public String getPartnerName() {
            return partnerName;
        }

        public void setPartnerName(String partnerName) {
            this.partnerName = partnerName;
        }

        public String getPartnerTypeName() {
            return partnerTypeName;
        }

        public void setPartnerTypeName(String partnerTypeName) {
            this.partnerTypeName = partnerTypeName;
        }

        public String getCharsetCode() {
            return charsetCode;
        }

        public void setCharsetCode(String charsetCode) {
            this.charsetCode = charsetCode;
        }

        public String getRuleName() {
            return ruleName;
        }

        public void setRuleName(String ruleName) {
            this.ruleName = ruleName;
        }

        public String getEnabled() {
            return enabled;
        }

        public void setEnabled(String enabled) {
            this.enabled = enabled;
        }

        public String getBmsaEnabled() {
            return bmsaEnabled;
        }

        public void setBmsaEnabled(String bmsaEnabled) {
            this.bmsaEnabled = bmsaEnabled;
        }

        public TransportInfo getTransport() {
            return transport;
        }

        public void setTransport(TransportInfo transport) {
            this.transport = transport;
        }
    }

    public static class TransportInfo {
        @JsonProperty("type")
        private String type;

        @JsonProperty("mqs")
        private MqsInfo mqs;

        @JsonProperty("cft")
        private CftInfo cft;

        @JsonProperty("jms")
        private JmsInfo jms;

        @JsonProperty("http")
        private HttpInfo http;

        @JsonProperty("email")
        private EmailInfo email;

        @JsonProperty("sftp")
        private Object sftp; // Typically null

        // Getters and setters
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public MqsInfo getMqs() {
            return mqs;
        }

        public void setMqs(MqsInfo mqs) {
            this.mqs = mqs;
        }

        public CftInfo getCft() {
            return cft;
        }

        public void setCft(CftInfo cft) {
            this.cft = cft;
        }

        public JmsInfo getJms() {
            return jms;
        }

        public void setJms(JmsInfo jms) {
            this.jms = jms;
        }

        public HttpInfo getHttp() {
            return http;
        }

        public void setHttp(HttpInfo http) {
            this.http = http;
        }

        public EmailInfo getEmail() {
            return email;
        }

        public void setEmail(EmailInfo email) {
            this.email = email;
        }

        public Object getSftp() {
            return sftp;
        }

        public void setSftp(Object sftp) {
            this.sftp = sftp;
        }
    }

    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            setterVisibility = JsonAutoDetect.Visibility.NONE)
    public static class MqsInfo {
        @JsonProperty("qName")
        private String qName;

        @JsonProperty("qManager")
        private String qManager;

        // Getters and setters
        public String getQName() {
            return qName;
        }

        public void setQName(String qName) {
            this.qName = qName;
        }

        public String getQManager() {
            return qManager;
        }

        public void setQManager(String qManager) {
            this.qManager = qManager;
        }
    }

    public static class CftInfo {
        @JsonProperty("idf")
        private String idf;

        @JsonProperty("partnerCode")
        private String partnerCode;

        // Getters and setters
        public String getIdf() {
            return idf;
        }

        public void setIdf(String idf) {
            this.idf = idf;
        }

        public String getPartnerCode() {
            return partnerCode;
        }

        public void setPartnerCode(String partnerCode) {
            this.partnerCode = partnerCode;
        }
    }

    public static class JmsInfo {
        @JsonProperty("qName")
        private String qName;

        // Getters and setters
        public String getQName() {
            return qName;
        }

        public void setQName(String qName) {
            this.qName = qName;
        }
    }

    public static class HttpInfo {
        @JsonProperty("uri")
        private String uri;

        @JsonProperty("partnerCode")
        private String partnerCode;

        // Getters and setters
        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getPartnerCode() {
            return partnerCode;
        }

        public void setPartnerCode(String partnerCode) {
            this.partnerCode = partnerCode;
        }
    }

    public static class EmailInfo {
        @JsonProperty("email_name")
        private String emailName;

        @JsonProperty("from")
        private String from;

        @JsonProperty("to")
        private String to;

        @JsonProperty("cc")
        private String cc;

        @JsonProperty("subject")
        private String subject;

        @JsonProperty("body")
        private String body;

        @JsonProperty("signature")
        private String signature;

        @JsonProperty("attachement")
        private Boolean attachment;

        // Getters and setters
        public String getEmailName() {
            return emailName;
        }

        public void setEmailName(String emailName) {
            this.emailName = emailName;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getCc() {
            return cc;
        }

        public void setCc(String cc) {
            this.cc = cc;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public Boolean getAttachment() {
            return attachment;
        }

        public void setAttachment(Boolean attachment) {
            this.attachment = attachment;
        }
    }

    public static class HttpTransportInfo {
        @JsonProperty("in")
        private HttpInfo in;

        @JsonProperty("out")
        private HttpInfo out;

        // Getters and setters
        public HttpInfo getIn() {
            return in;
        }

        public void setIn(HttpInfo in) {
            this.in = in;
        }

        public HttpInfo getOut() {
            return out;
        }

        public void setOut(HttpInfo out) {
            this.out = out;
        }
    }

    public static class VirtualAccountInfo {
        @JsonProperty("format")
        private String format;

        @JsonProperty("path")
        private String path;

        // Getters and setters
        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class FunctionalModuleInfo {
        // Placeholder for functional modules - typically empty array
        @JsonProperty("name")
        private String name;

        // Getters and setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class FlowRuleInfo {
        @JsonProperty("flowCode")
        private String flowCode;

        @JsonProperty("transportType")
        private String transportType;

        @JsonProperty("isUnitary")
        private Boolean isUnitary;

        @JsonProperty("priority")
        private Integer priority;

        @JsonProperty("urgency")
        private String urgency;

        @JsonProperty("flowControlledEnabled")
        private Boolean flowControlledEnabled;

        @JsonProperty("flowMaximum")
        private Long flowMaximum;

        @JsonProperty("flowRetentionEnabled")
        private Boolean flowRetentionEnabled;

        @JsonProperty("retentionCyclePeriod")
        private String retentionCyclePeriod;

        @JsonProperty("writeFile")
        private Boolean writeFile;

        @JsonProperty("minRequiredFileSize")
        private Long minRequiredFileSize;

        @JsonProperty("ignoreOutputDupCheck")
        private Boolean ignoreOutputDupCheck;

        @JsonProperty("logAll")
        private Boolean logAll;

        // Getters and setters
        public String getFlowCode() {
            return flowCode;
        }

        public void setFlowCode(String flowCode) {
            this.flowCode = flowCode;
        }

        public String getTransportType() {
            return transportType;
        }

        public void setTransportType(String transportType) {
            this.transportType = transportType;
        }

        public Boolean getIsUnitary() {
            return isUnitary;
        }

        public void setIsUnitary(Boolean isUnitary) {
            this.isUnitary = isUnitary;
        }

        public Integer getPriority() {
            return priority;
        }

        public void setPriority(Integer priority) {
            this.priority = priority;
        }

        public String getUrgency() {
            return urgency;
        }

        public void setUrgency(String urgency) {
            this.urgency = urgency;
        }

        public Boolean getFlowControlledEnabled() {
            return flowControlledEnabled;
        }

        public void setFlowControlledEnabled(Boolean flowControlledEnabled) {
            this.flowControlledEnabled = flowControlledEnabled;
        }

        public Long getFlowMaximum() {
            return flowMaximum;
        }

        public void setFlowMaximum(Long flowMaximum) {
            this.flowMaximum = flowMaximum;
        }

        public Boolean getFlowRetentionEnabled() {
            return flowRetentionEnabled;
        }

        public void setFlowRetentionEnabled(Boolean flowRetentionEnabled) {
            this.flowRetentionEnabled = flowRetentionEnabled;
        }

        public String getRetentionCyclePeriod() {
            return retentionCyclePeriod;
        }

        public void setRetentionCyclePeriod(String retentionCyclePeriod) {
            this.retentionCyclePeriod = retentionCyclePeriod;
        }

        public Boolean getWriteFile() {
            return writeFile;
        }

        public void setWriteFile(Boolean writeFile) {
            this.writeFile = writeFile;
        }

        public Long getMinRequiredFileSize() {
            return minRequiredFileSize;
        }

        public void setMinRequiredFileSize(Long minRequiredFileSize) {
            this.minRequiredFileSize = minRequiredFileSize;
        }

        public Boolean getIgnoreOutputDupCheck() {
            return ignoreOutputDupCheck;
        }

        public void setIgnoreOutputDupCheck(Boolean ignoreOutputDupCheck) {
            this.ignoreOutputDupCheck = ignoreOutputDupCheck;
        }

        public Boolean getLogAll() {
            return logAll;
        }

        public void setLogAll(Boolean logAll) {
            this.logAll = logAll;
        }
    }

    // Main DTO getters and setters
    public FlowInfo getFlow() {
        return flow;
    }

    public void setFlow(FlowInfo flow) {
        this.flow = flow;
    }

    public PartnerInfo getPartnerIn() {
        return partnerIn;
    }

    public void setPartnerIn(PartnerInfo partnerIn) {
        this.partnerIn = partnerIn;
    }

    public List<PartnerInfo> getPartnerOut() {
        return partnerOut;
    }

    public void setPartnerOut(List<PartnerInfo> partnerOut) {
        this.partnerOut = partnerOut;
    }

    public HttpTransportInfo getHttpTransport() {
        return httpTransport;
    }

    public void setHttpTransport(HttpTransportInfo httpTransport) {
        this.httpTransport = httpTransport;
    }

    public VirtualAccountInfo getVirtualAccount() {
        return virtualAccount;
    }

    public void setVirtualAccount(VirtualAccountInfo virtualAccount) {
        this.virtualAccount = virtualAccount;
    }

    public List<FunctionalModuleInfo> getFunctionalModules() {
        return functionalModules;
    }

    public void setFunctionalModules(List<FunctionalModuleInfo> functionalModules) {
        this.functionalModules = functionalModules;
    }

    public List<FlowRuleInfo> getFlowRules() {
        return flowRules;
    }

    public void setFlowRules(List<FlowRuleInfo> flowRules) {
        this.flowRules = flowRules;
    }
}
