package com.pixel.v2.persistence.repository;

import com.pixel.v2.persistence.model.PaymentAuditDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for PaymentAuditDetails entity
 */
@Repository
public interface PaymentAuditDetailsRepository
        extends JpaRepository<PaymentAuditDetails, BigDecimal> {

    /**
     * Find payment audit details by flow occur ID
     */
    List<PaymentAuditDetails> findByFlowOccurIdOrderByDatatsDesc(String flowOccurId);

    /**
     * Find payments by currency
     */
    List<PaymentAuditDetails> findByCurrencyOrderByDatatsDesc(String currency);

    /**
     * Find payments by country
     */
    List<PaymentAuditDetails> findByCountryOrderByDatatsDesc(String country);

    /**
     * Find payments by region
     */
    List<PaymentAuditDetails> findByRegionOrderByDatatsDesc(String region);

    /**
     * Find payments by message type
     */
    List<PaymentAuditDetails> findByMessageTypeOrderByDatatsDesc(String messageType);

    /**
     * Find payments by date range
     */
    @Query("SELECT p FROM PaymentAuditDetails p WHERE p.datats BETWEEN :startDate AND :endDate ORDER BY p.datats DESC")
    List<PaymentAuditDetails> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find payments by log day
     */
    List<PaymentAuditDetails> findByLogDayOrderByDatatsDesc(LocalDate logDay);

    /**
     * Find payments by amount range
     */
    @Query("SELECT p FROM PaymentAuditDetails p WHERE p.amount BETWEEN :minAmount AND :maxAmount ORDER BY p.amount DESC")
    List<PaymentAuditDetails> findByAmountRange(@Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount);

    /**
     * Find virtual account payments
     */
    @Query("SELECT p FROM PaymentAuditDetails p WHERE p.isVirtual = 'Y' ORDER BY p.datats DESC")
    List<PaymentAuditDetails> findVirtualAccountPayments();

    /**
     * Find payments by creditor bank
     */
    List<PaymentAuditDetails> findByCreditorBankOrderByDatatsDesc(String creditorBank);

    /**
     * Find payments by debtor bank
     */
    List<PaymentAuditDetails> findByDebtorBankOrderByDatatsDesc(String debtorBank);

    /**
     * Find payments by recipient partner
     */
    List<PaymentAuditDetails> findByRecipientPartnerOrderByDatatsDesc(String recipientPartner);

    /**
     * Find payments by UETR
     */
    List<PaymentAuditDetails> findByUetrOrderByDatatsDesc(String uetr);

    /**
     * Find payments by message ID
     */
    List<PaymentAuditDetails> findByMessageIdOrderByDatatsDesc(String messageId);

    /**
     * Find payments by sender
     */
    List<PaymentAuditDetails> findBySenderOrderByDatatsDesc(String sender);

    /**
     * Get payment statistics by currency
     */
    @Query("SELECT p.currency, COUNT(p), SUM(p.amount) FROM PaymentAuditDetails p WHERE p.logDay BETWEEN :startDate AND :endDate GROUP BY p.currency")
    List<Object[]> getPaymentStatisticsByCurrency(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get payment statistics by country
     */
    @Query("SELECT p.country, COUNT(p), SUM(p.amount) FROM PaymentAuditDetails p WHERE p.logDay BETWEEN :startDate AND :endDate GROUP BY p.country")
    List<Object[]> getPaymentStatisticsByCountry(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get daily payment volume
     */
    @Query("SELECT p.logDay, COUNT(p), SUM(p.amount) FROM PaymentAuditDetails p WHERE p.logDay BETWEEN :startDate AND :endDate GROUP BY p.logDay ORDER BY p.logDay")
    List<Object[]> getDailyPaymentVolume(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find high-value payments
     */
    @Query("SELECT p FROM PaymentAuditDetails p WHERE p.amount >= :threshold ORDER BY p.amount DESC")
    List<PaymentAuditDetails> findHighValuePayments(@Param("threshold") BigDecimal threshold);

    /**
     * Find payments by debtor account pattern
     */
    @Query("SELECT p FROM PaymentAuditDetails p WHERE p.debtorAccount LIKE %:accountPattern% ORDER BY p.datats DESC")
    List<PaymentAuditDetails> findByDebtorAccountPattern(
            @Param("accountPattern") String accountPattern);

    /**
     * Find payments by creditor account pattern
     */
    @Query("SELECT p FROM PaymentAuditDetails p WHERE p.creditorAccount LIKE %:accountPattern% ORDER BY p.datats DESC")
    List<PaymentAuditDetails> findByCreditorAccountPattern(
            @Param("accountPattern") String accountPattern);

    /**
     * Count payments by flow occur ID
     */
    Long countByFlowOccurId(String flowOccurId);

    /**
     * Get payment count and volume by message type
     */
    @Query("SELECT p.messageType, COUNT(p), SUM(p.amount) FROM PaymentAuditDetails p WHERE p.logDay BETWEEN :startDate AND :endDate GROUP BY p.messageType")
    List<Object[]> getPaymentStatisticsByMessageType(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find payments with specific execution date
     */
    List<PaymentAuditDetails> findByRequestedExecutionDateOrderByDatatsDesc(
            LocalDate executionDate);

    /**
     * Get partner payment statistics
     */
    @Query("SELECT p.recipientPartner, COUNT(p), SUM(p.amount) FROM PaymentAuditDetails p WHERE p.logDay BETWEEN :startDate AND :endDate GROUP BY p.recipientPartner")
    List<Object[]> getPartnerPaymentStatistics(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
