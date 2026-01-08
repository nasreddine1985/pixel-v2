package com.pixel.v2.referential.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_charset_encoding table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_CHARSET_ENCODING", schema = "TIB_AUDIT_TEC")
public class RefCharsetEncoding {

    @Id
    @Column(name = "CHARSET_ENCODING_ID")
    private Integer charsetEncodingId;

    @Column(name = "CHARSET_CODE")
    private String charsetCode;

    @Column(name = "CHARSET_DESC")
    private String charsetDesc;

    // One-to-many relationship
    @OneToMany(mappedBy = "charsetEncoding", fetch = FetchType.LAZY)
    private List<RefFlowPartner> partners;

    // Default constructor
    public RefCharsetEncoding() {
        // Required for JPA
    }

    // Getters and Setters
    public Integer getCharsetEncodingId() {
        return charsetEncodingId;
    }

    public void setCharsetEncodingId(Integer charsetEncodingId) {
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

    public List<RefFlowPartner> getPartners() {
        return partners;
    }

    public void setPartners(List<RefFlowPartner> partners) {
        this.partners = partners;
    }
}
