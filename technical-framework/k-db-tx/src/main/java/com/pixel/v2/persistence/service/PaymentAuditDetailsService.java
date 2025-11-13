package com.pixel.v2.persistence.service;

import com.pixel.v2.persistence.model.PaymentAuditDetails;
import com.pixel.v2.persistence.repository.PaymentAuditDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for PaymentAuditDetails operations
 */
@Service
@Transactional
public class PaymentAuditDetailsService {

    private final PaymentAuditDetailsRepository paymentAuditDetailsRepository;

    public PaymentAuditDetailsService(PaymentAuditDetailsRepository paymentAuditDetailsRepository) {
        this.paymentAuditDetailsRepository = paymentAuditDetailsRepository;
    }

    /**
     * Save a payment audit detail
     */
    public PaymentAuditDetails save(PaymentAuditDetails paymentAuditDetails) {
        return paymentAuditDetailsRepository.save(paymentAuditDetails);
    }

    /**
     * Find payment audit detail by ID
     */
    @Transactional(readOnly = true)
    public Optional<PaymentAuditDetails> findById(BigDecimal paymentAuditId) {
        return paymentAuditDetailsRepository.findById(paymentAuditId);
    }

    /**
     * Find payment audit details by flow occur ID
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByFlowOccurId(String flowOccurId) {
        return paymentAuditDetailsRepository.findByFlowOccurIdOrderByDatatsDesc(flowOccurId);
    }

    /**
     * Find payments by currency
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByCurrency(String currency) {
        return paymentAuditDetailsRepository.findByCurrencyOrderByDatatsDesc(currency);
    }

    /**
     * Find payments by country
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByCountry(String country) {
        return paymentAuditDetailsRepository.findByCountryOrderByDatatsDesc(country);
    }

    /**
     * Find payments by region
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByRegion(String region) {
        return paymentAuditDetailsRepository.findByRegionOrderByDatatsDesc(region);
    }

    /**
     * Find payments by message type
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByMessageType(String messageType) {
        return paymentAuditDetailsRepository.findByMessageTypeOrderByDatatsDesc(messageType);
    }

    /**
     * Find payments by date range
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByDateRange(LocalDateTime startDate,
            LocalDateTime endDate) {
        return paymentAuditDetailsRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Find payments by log day
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByLogDay(LocalDate logDay) {
        return paymentAuditDetailsRepository.findByLogDayOrderByDatatsDesc(logDay);
    }

    /**
     * Find payments by amount range
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        return paymentAuditDetailsRepository.findByAmountRange(minAmount, maxAmount);
    }

    /**
     * Find virtual account payments
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findVirtualAccountPayments() {
        return paymentAuditDetailsRepository.findVirtualAccountPayments();
    }

    /**
     * Find payments by creditor bank
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByCreditorBank(String creditorBank) {
        return paymentAuditDetailsRepository.findByCreditorBankOrderByDatatsDesc(creditorBank);
    }

    /**
     * Find payments by debtor bank
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByDebtorBank(String debtorBank) {
        return paymentAuditDetailsRepository.findByDebtorBankOrderByDatatsDesc(debtorBank);
    }

    /**
     * Find payments by recipient partner
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByRecipientPartner(String recipientPartner) {
        return paymentAuditDetailsRepository
                .findByRecipientPartnerOrderByDatatsDesc(recipientPartner);
    }

    /**
     * Find payments by UETR
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByUetr(String uetr) {
        return paymentAuditDetailsRepository.findByUetrOrderByDatatsDesc(uetr);
    }

    /**
     * Find payments by message ID
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByMessageId(String messageId) {
        return paymentAuditDetailsRepository.findByMessageIdOrderByDatatsDesc(messageId);
    }

    /**
     * Find payments by sender
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findBySender(String sender) {
        return paymentAuditDetailsRepository.findBySenderOrderByDatatsDesc(sender);
    }

    /**
     * Get payment statistics by currency
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPaymentStatisticsByCurrency(LocalDate startDate, LocalDate endDate) {
        return paymentAuditDetailsRepository.getPaymentStatisticsByCurrency(startDate, endDate);
    }

    /**
     * Get payment statistics by country
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPaymentStatisticsByCountry(LocalDate startDate, LocalDate endDate) {
        return paymentAuditDetailsRepository.getPaymentStatisticsByCountry(startDate, endDate);
    }

    /**
     * Get daily payment volume
     */
    @Transactional(readOnly = true)
    public List<Object[]> getDailyPaymentVolume(LocalDate startDate, LocalDate endDate) {
        return paymentAuditDetailsRepository.getDailyPaymentVolume(startDate, endDate);
    }

    /**
     * Find high-value payments
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findHighValuePayments(BigDecimal threshold) {
        return paymentAuditDetailsRepository.findHighValuePayments(threshold);
    }

    /**
     * Find payments by debtor account pattern
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByDebtorAccountPattern(String accountPattern) {
        return paymentAuditDetailsRepository.findByDebtorAccountPattern(accountPattern);
    }

    /**
     * Find payments by creditor account pattern
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByCreditorAccountPattern(String accountPattern) {
        return paymentAuditDetailsRepository.findByCreditorAccountPattern(accountPattern);
    }

    /**
     * Count payments by flow occur ID
     */
    @Transactional(readOnly = true)
    public Long countByFlowOccurId(String flowOccurId) {
        return paymentAuditDetailsRepository.countByFlowOccurId(flowOccurId);
    }

    /**
     * Get payment count and volume by message type
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPaymentStatisticsByMessageType(LocalDate startDate,
            LocalDate endDate) {
        return paymentAuditDetailsRepository.getPaymentStatisticsByMessageType(startDate, endDate);
    }

    /**
     * Find payments with specific execution date
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findByRequestedExecutionDate(LocalDate executionDate) {
        return paymentAuditDetailsRepository
                .findByRequestedExecutionDateOrderByDatatsDesc(executionDate);
    }

    /**
     * Get partner payment statistics
     */
    @Transactional(readOnly = true)
    public List<Object[]> getPartnerPaymentStatistics(LocalDate startDate, LocalDate endDate) {
        return paymentAuditDetailsRepository.getPartnerPaymentStatistics(startDate, endDate);
    }

    /**
     * Create a new payment audit detail
     */
    public PaymentAuditDetails createPaymentAudit(String flowOccurId, BigDecimal amount,
            String currency) {
        PaymentAuditDetails paymentAudit = new PaymentAuditDetails();
        paymentAudit.setFlowOccurId(flowOccurId);
        paymentAudit.setAmount(amount);
        paymentAudit.setCurrency(currency);
        paymentAudit.setPaymentAuditId(generatePaymentAuditId());
        return save(paymentAudit);
    }

    /**
     * Update payment audit with additional details
     */
    public PaymentAuditDetails updatePaymentAudit(BigDecimal paymentAuditId, String debtorAccount,
            String creditorAccount, String messageType) {
        Optional<PaymentAuditDetails> auditOpt = findById(paymentAuditId);
        if (auditOpt.isPresent()) {
            PaymentAuditDetails audit = auditOpt.get();
            audit.setDebtorAccount(debtorAccount);
            audit.setCreditorAccount(creditorAccount);
            audit.setMessageType(messageType);
            return save(audit);
        }
        return null;
    }

    /**
     * Delete payment audit detail
     */
    public void delete(BigDecimal paymentAuditId) {
        paymentAuditDetailsRepository.deleteById(paymentAuditId);
    }

    /**
     * Find all payment audit details
     */
    @Transactional(readOnly = true)
    public List<PaymentAuditDetails> findAll() {
        return paymentAuditDetailsRepository.findAll();
    }

    /**
     * Generate a unique payment audit ID
     */
    private BigDecimal generatePaymentAuditId() {
        return new BigDecimal(System.currentTimeMillis());
    }
}
