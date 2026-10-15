package com.onlinemedicalstore.webbasedmedicineorderingsystem.service;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.dto.PaymentDTO;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Medicine;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Order;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.OrderItem;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.Payment;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.PaymentStatus;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.MedicineRepository;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MedicineRepository medicineRepository;

    /**
     * Fetches all payments and converts them to PaymentDTOs to prevent serialization issues.
     * @return A list of all payments in a safe-to-transfer format.
     */
    public List<PaymentDTO> getAllPaymentsAsDTO() {
        return paymentRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts a Payment entity to a PaymentDTO.
     * @param payment The Payment entity to convert.
     * @return The corresponding PaymentDTO.
     */
    private PaymentDTO convertToDto(Payment payment) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setStatus(payment.getStatus());
        dto.setAmount(payment.getAmount());
        dto.setCreationDate(payment.getCreationDate());
        if (payment.getOrder() != null) {
            dto.setOrderId(payment.getOrder().getId());
            if (payment.getOrder().getUser() != null) {
                dto.setCustomerUsername(payment.getOrder().getUser().getUsername());
            }
        }
        return dto;
    }

    /**
     * A scheduled task that runs every minute to check for pending payments that have expired.
     * If a payment is older than 10 minutes, it is marked as FAILED and the stock is restocked.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkPendingPayments() {
        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);
        List<Payment> pendingPayments = paymentRepository.findByStatusAndCreationDateBefore(PaymentStatus.PENDING, tenMinutesAgo);

        for (Payment payment : pendingPayments) {
            payment.setStatus(PaymentStatus.FAILED);

            Order order = payment.getOrder();
            if (order != null) {
                for (OrderItem item : order.getOrderItems()) {
                    Medicine medicine = item.getMedicine();
                    medicine.setStockQuantity(medicine.getStockQuantity() + item.getQuantity());
                    medicineRepository.save(medicine);
                }
            }
            paymentRepository.save(payment);
        }
    }

    /**
     * Allows a Finance Officer to manually mark a PENDING payment as SUCCESSFUL.
     * @param paymentId The ID of the payment to update.
     * @return The updated Payment entity.
     */
    @Transactional
    public Payment markPaymentAsSuccess(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.SUCCESS);
            return paymentRepository.save(payment);
        }
        // If status is already SUCCESS or FAILED, do nothing and return the existing state.
        return payment;
    }
}


