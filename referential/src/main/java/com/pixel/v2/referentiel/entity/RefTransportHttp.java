package com.pixel.v2.referential.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * JPA Entity for ref_transport_http table in TIB_AUDIT_TEC schema
 */
@Entity
@Table(name = "REF_TRANSPORT_HTTP", schema = "TIB_AUDIT_TEC")
public class RefTransportHttp {

    @Id
    @Column(name = "TRANSPORT_ID")
    private Integer transportId;

    @Column(name = "HTTP_URL")
    private String httpUrl;

    @Column(name = "HTTP_METHOD")
    private String httpMethod;

    @Column(name = "HTTP_AUTHENTICATION_TYPE")
    private String httpAuthenticationType;

    @Column(name = "HTTP_USERNAME")
    private String httpUsername;

    @Column(name = "HTTP_PASSWORD")
    private String httpPassword;

    @Column(name = "HTTP_CERTIFICATE_PATH")
    private String httpCertificatePath;

    @Column(name = "HTTP_TIMEOUT")
    private Integer httpTimeout;

    @Column(name = "HTTP_RETRY_COUNT")
    private Integer httpRetryCount;

    @Column(name = "HTTP_RETRY_DELAY")
    private Integer httpRetryDelay;

    @Column(name = "HTTP_HEADERS")
    private String httpHeaders;

    @Column(name = "HTTP_CONTENT_TYPE")
    private String httpContentType;

    @Column(name = "HTTP_ACCEPT_TYPE")
    private String httpAcceptType;

    @Column(name = "HTTP_USER_AGENT")
    private String httpUserAgent;

    @Column(name = "HTTP_SSL_VERIFY")
    private String httpSslVerify;

    @Column(name = "HTTP_PROXY_URL")
    private String httpProxyUrl;

    @Column(name = "HTTP_PROXY_USERNAME")
    private String httpProxyUsername;

    @Column(name = "HTTP_PROXY_PASSWORD")
    private String httpProxyPassword;

    @Column(name = "CREATION_DTE")
    private LocalDateTime creationDte;

    @Column(name = "UPDATE_DTE")
    private LocalDateTime updateDte;

    // Many-to-one relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRANSPORT_ID", insertable = false, updatable = false)
    private RefTransport transport;

    // Default constructor
    public RefTransportHttp() {
        // Required for JPA
    }

    // Constructor with parameters
    public RefTransportHttp(Integer transportId, String httpUrl, String httpMethod) {
        this.transportId = transportId;
        this.httpUrl = httpUrl;
        this.httpMethod = httpMethod;
        this.httpTimeout = 30000; // Default 30 seconds
        this.httpRetryCount = 3;
        this.httpRetryDelay = 1000; // Default 1 second
        this.httpSslVerify = "Y";
        this.creationDte = LocalDateTime.now();
        this.updateDte = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getTransportId() {
        return transportId;
    }

    public void setTransportId(Integer transportId) {
        this.transportId = transportId;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHttpAuthenticationType() {
        return httpAuthenticationType;
    }

    public void setHttpAuthenticationType(String httpAuthenticationType) {
        this.httpAuthenticationType = httpAuthenticationType;
    }

    public String getHttpUsername() {
        return httpUsername;
    }

    public void setHttpUsername(String httpUsername) {
        this.httpUsername = httpUsername;
    }

    public String getHttpPassword() {
        return httpPassword;
    }

    public void setHttpPassword(String httpPassword) {
        this.httpPassword = httpPassword;
    }

    public String getHttpCertificatePath() {
        return httpCertificatePath;
    }

    public void setHttpCertificatePath(String httpCertificatePath) {
        this.httpCertificatePath = httpCertificatePath;
    }

    public Integer getHttpTimeout() {
        return httpTimeout;
    }

    public void setHttpTimeout(Integer httpTimeout) {
        this.httpTimeout = httpTimeout;
    }

    public Integer getHttpRetryCount() {
        return httpRetryCount;
    }

    public void setHttpRetryCount(Integer httpRetryCount) {
        this.httpRetryCount = httpRetryCount;
    }

    public Integer getHttpRetryDelay() {
        return httpRetryDelay;
    }

    public void setHttpRetryDelay(Integer httpRetryDelay) {
        this.httpRetryDelay = httpRetryDelay;
    }

    public String getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(String httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    public String getHttpContentType() {
        return httpContentType;
    }

    public void setHttpContentType(String httpContentType) {
        this.httpContentType = httpContentType;
    }

    public String getHttpAcceptType() {
        return httpAcceptType;
    }

    public void setHttpAcceptType(String httpAcceptType) {
        this.httpAcceptType = httpAcceptType;
    }

    public String getHttpUserAgent() {
        return httpUserAgent;
    }

    public void setHttpUserAgent(String httpUserAgent) {
        this.httpUserAgent = httpUserAgent;
    }

    public String getHttpSslVerify() {
        return httpSslVerify;
    }

    public void setHttpSslVerify(String httpSslVerify) {
        this.httpSslVerify = httpSslVerify;
    }

    public String getHttpProxyUrl() {
        return httpProxyUrl;
    }

    public void setHttpProxyUrl(String httpProxyUrl) {
        this.httpProxyUrl = httpProxyUrl;
    }

    public String getHttpProxyUsername() {
        return httpProxyUsername;
    }

    public void setHttpProxyUsername(String httpProxyUsername) {
        this.httpProxyUsername = httpProxyUsername;
    }

    public String getHttpProxyPassword() {
        return httpProxyPassword;
    }

    public void setHttpProxyPassword(String httpProxyPassword) {
        this.httpProxyPassword = httpProxyPassword;
    }

    public LocalDateTime getCreationDte() {
        return creationDte;
    }

    public void setCreationDte(LocalDateTime creationDte) {
        this.creationDte = creationDte;
    }

    public LocalDateTime getUpdateDte() {
        return updateDte;
    }

    public void setUpdateDte(LocalDateTime updateDte) {
        this.updateDte = updateDte;
    }

    public RefTransport getTransport() {
        return transport;
    }

    public void setTransport(RefTransport transport) {
        this.transport = transport;
    }

    @Override
    public String toString() {
        return "RefTransportHttp{" + "transportId=" + transportId + ", httpUrl='" + httpUrl + '\''
                + ", httpMethod='" + httpMethod + '\'' + ", httpAuthenticationType='"
                + httpAuthenticationType + '\'' + ", httpTimeout=" + httpTimeout
                + ", httpRetryCount=" + httpRetryCount + ", creationDte=" + creationDte
                + ", updateDte=" + updateDte + '}';
    }
}
