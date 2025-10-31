package com.pixel.v2.referentiel.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Configuration model for flow processing parameters
 */
public class FlowConfiguration {

    @JsonProperty("cmdMapping")
    private String cmdMapping;

    @JsonProperty("rail")
    private String rail;

    @JsonProperty("mode")
    private String mode;

    @JsonProperty("needSplit")
    private Boolean needSplit;

    @JsonProperty("splitExpr")
    private String splitExpr;

    @JsonProperty("chunkSize")
    private Integer chunkSize;

    @JsonProperty("outputs")
    private List<String> outputs;

    @JsonProperty("xsltFileToCdm")
    private String xsltFileToCdm;

    @JsonProperty("xsltFileFromCdm")
    private String xsltFileFromCdm;

    @JsonProperty("xsdFlowFile")
    private String xsdFlowFile;

    @JsonProperty("xsdCdmFile")
    private String xsdCdmFile;

    @JsonProperty("kafkaBroker")
    private String kafkaBroker;

    @JsonProperty("kafkaTopic")
    private String kafkaTopic;

    // Default constructor
    public FlowConfiguration() {}

    // Constructor with original fields
    public FlowConfiguration(String cmdMapping, String rail, String mode, Boolean needSplit,
            String splitExpr, Integer chunkSize, List<String> outputs) {
        this.cmdMapping = cmdMapping;
        this.rail = rail;
        this.mode = mode;
        this.needSplit = needSplit;
        this.splitExpr = splitExpr;
        this.chunkSize = chunkSize;
        this.outputs = outputs;
    }

    // Getters and Setters
    public String getCmdMapping() {
        return cmdMapping;
    }

    public void setCmdMapping(String cmdMapping) {
        this.cmdMapping = cmdMapping;
    }

    public String getRail() {
        return rail;
    }

    public void setRail(String rail) {
        this.rail = rail;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Boolean getNeedSplit() {
        return needSplit;
    }

    public void setNeedSplit(Boolean needSplit) {
        this.needSplit = needSplit;
    }

    public String getSplitExpr() {
        return splitExpr;
    }

    public void setSplitExpr(String splitExpr) {
        this.splitExpr = splitExpr;
    }

    public Integer getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(Integer chunkSize) {
        this.chunkSize = chunkSize;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    public String getXsltFileToCdm() {
        return xsltFileToCdm;
    }

    public void setXsltFileToCdm(String xsltFileToCdm) {
        this.xsltFileToCdm = xsltFileToCdm;
    }

    public String getXsltFileFromCdm() {
        return xsltFileFromCdm;
    }

    public void setXsltFileFromCdm(String xsltFileFromCdm) {
        this.xsltFileFromCdm = xsltFileFromCdm;
    }

    public String getXsdFlowFile() {
        return xsdFlowFile;
    }

    public void setXsdFlowFile(String xsdFlowFile) {
        this.xsdFlowFile = xsdFlowFile;
    }

    public String getXsdCdmFile() {
        return xsdCdmFile;
    }

    public void setXsdCdmFile(String xsdCdmFile) {
        this.xsdCdmFile = xsdCdmFile;
    }

    public String getKafkaBroker() {
        return kafkaBroker;
    }

    public void setKafkaBroker(String kafkaBroker) {
        this.kafkaBroker = kafkaBroker;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public void setKafkaTopic(String kafkaTopic) {
        this.kafkaTopic = kafkaTopic;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FlowConfiguration that = (FlowConfiguration) o;
        return Objects.equals(cmdMapping, that.cmdMapping) && Objects.equals(rail, that.rail)
                && Objects.equals(mode, that.mode) && Objects.equals(needSplit, that.needSplit)
                && Objects.equals(splitExpr, that.splitExpr)
                && Objects.equals(chunkSize, that.chunkSize)
                && Objects.equals(outputs, that.outputs)
                && Objects.equals(xsltFileToCdm, that.xsltFileToCdm)
                && Objects.equals(xsltFileFromCdm, that.xsltFileFromCdm)
                && Objects.equals(xsdFlowFile, that.xsdFlowFile)
                && Objects.equals(xsdCdmFile, that.xsdCdmFile)
                && Objects.equals(kafkaBroker, that.kafkaBroker)
                && Objects.equals(kafkaTopic, that.kafkaTopic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cmdMapping, rail, mode, needSplit, splitExpr, chunkSize, outputs,
                xsltFileToCdm, xsltFileFromCdm, xsdFlowFile, xsdCdmFile, kafkaBroker, kafkaTopic);
    }

    @Override
    public String toString() {
        return "FlowConfiguration{" + "cmdMapping='" + cmdMapping + '\'' + ", rail='" + rail + '\''
                + ", mode='" + mode + '\'' + ", needSplit=" + needSplit + ", splitExpr='"
                + splitExpr + '\'' + ", chunkSize=" + chunkSize + ", outputs=" + outputs
                + ", xsltFileToCdm='" + xsltFileToCdm + '\'' + ", xsltFileFromCdm='"
                + xsltFileFromCdm + '\'' + ", xsdFlowFile='" + xsdFlowFile + '\'' + ", xsdCdmFile='"
                + xsdCdmFile + '\'' + ", kafkaBroker='" + kafkaBroker + '\'' + ", kafkaTopic='"
                + kafkaTopic + '\'' + '}';
    }
}
