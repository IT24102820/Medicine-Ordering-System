package com.onlinemedicalstore.webbasedmedicineorderingsystem.service;

import com.onlinemedicalstore.webbasedmedicineorderingsystem.entity.*;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.MedicineRepository;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.OrderRepository;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.PaymentRepository;
import com.onlinemedicalstore.webbasedmedicineorderingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private MedicineRepository medicineRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public Order createOrder(Map<Long, Integer> cartItems, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setUser(user);

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Long medicineId = entry.getKey();
            Integer quantity = entry.getValue();

            Medicine medicine = medicineRepository.findById(medicineId)
                    .orElseThrow(() -> new MedicineService.ResourceNotFoundException("Medicine not found with id: " + medicineId));

            if (medicine.getStockQuantity() < quantity) {
                throw new IllegalStateException("Not enough stock for " + medicine.getName() + ". Requested: " + quantity + ", Available: " + medicine.getStockQuantity());
            }

            medicine.setStockQuantity(medicine.getStockQuantity() - quantity);
            medicineRepository.save(medicine);

            OrderItem orderItem = new OrderItem();
            orderItem.setMedicine(medicine);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(medicine.getPrice());
            orderItem.setOrder(order);

            order.getOrderItems().add(orderItem);
            total = total.add(medicine.getPrice().multiply(new BigDecimal(quantity)));
        }

        order.setTotalPrice(total);
        // First, save the order to generate its ID. This is crucial for the relationship.
        orderRepository.save(order);

        // Then, create, link, and explicitly save the payment record.
        Payment payment = new Payment();
        payment.setCreationDate(LocalDateTime.now());
        payment.setAmount(total);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setOrder(order);
        paymentRepository.save(payment); // This explicit save guarantees the payment is created.

        order.setPayment(payment);
        return order;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersForUser(String username) {
        return orderRepository.findByUserUsername(username);
    }

    @Transactional
    public void deleteOrder(Long orderId, String username) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new MedicineService.ResourceNotFoundException("Order not found with id: " + orderId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!Objects.equals(order.getUser().getId(), user.getId()) && user.getRole() == Role.ROLE_CUSTOMER) {
            throw new AccessDeniedException("You do not have permission to cancel this order.");
        }

        for (OrderItem item : order.getOrderItems()) {
            Medicine medicine = item.getMedicine();
            medicine.setStockQuantity(medicine.getStockQuantity() + item.getQuantity());
            medicineRepository.save(medicine);
        }

        orderRepository.delete(order);
    }
}
