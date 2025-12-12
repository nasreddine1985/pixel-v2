package com.pixel.v2.referentiel.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Model representing the TIB_AUDIT_TEC.REF_CHARSET_ENCODING table Contains character encoding
 * definitions
 */
public class RefCharsetEncoding {

    @JsonProperty("charsetEncodingId")
    private Long charsetEncodingId;

    @JsonProperty("charsetCode")
    private String charsetCode;

    @JsonProperty("charsetDesc")
    private String charsetDesc;

    // Default constructor
    public RefCharsetEncoding() {}

    // Constructor with all fields
    public RefCharsetEncoding(Long charsetEncodingId, String charsetCode, String charsetDesc) {
        this.charsetEncodingId = charsetEncodingId;
        this.charsetCode = charsetCode;
        this.charsetDesc = charsetDesc;
    }

    // Getters and Setters
    public Long getCharsetEncodingId() {
        return charsetEncodingId;
    }

    public void setCharsetEncodingId(Long charsetEncodingId) {
        this.charsetEncodingId = charsetEncodingId;
    }

    public String getCharsetCode() {
        return charsetCode;
    }

    public void setCharsetCode(String charsetCode) {
        this.charsetCode = charsetCode;
    }

    public String getCharsetDesc() {
        return charsetDesc;
    }

    public void setCharsetDesc(String charsetDesc) {
        this.charsetDesc = charsetDesc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RefCharsetEncoding that = (RefCharsetEncoding) o;
        return Objects.equals(charsetEncodingId, that.charsetEncodingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(charsetEncodingId);
    }

    @Override
    public String toString() {
        return "RefCharsetEncoding{" + "charsetEncodingId=" + charsetEncodingId + ", charsetCode='"
                + charsetCode + '\'' + ", charsetDesc='" + charsetDesc + '\'' + '}';
    }
}
