package com.pixel.v2.referentiel.entity;

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
@Table(name = "ref_charset_encoding", schema = "tib_audit_tec")
public class RefCharsetEncoding {

    @Id
    @Column(name = "charset_encoding_id")
    private Integer charsetEncodingId;

    @Column(name = "charset_code")
    private String charsetCode;

    @Column(name = "charset_desc")
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
