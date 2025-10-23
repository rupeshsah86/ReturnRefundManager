package com.ecommerce.returnmanager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ecommerce.returnmanager.model.Order;
import com.ecommerce.returnmanager.model.Order.OrderStatus;
import com.ecommerce.returnmanager.model.OrderItem;
import com.ecommerce.returnmanager.model.ReturnRequest;
import com.ecommerce.returnmanager.model.User;
import com.ecommerce.returnmanager.model.User.Role;
import com.ecommerce.returnmanager.repository.OrderItemRepository;
import com.ecommerce.returnmanager.repository.OrderRepository;
import com.ecommerce.returnmanager.repository.ReturnRequestRepository;
import com.ecommerce.returnmanager.repository.UserRepository;

/**
 * Initializes the database with sample data upon application startup.
 * This simulates a user, their order, and a pending return request.
 */
@Component
@Profile("!test") // Don't run this during tests
public class TestDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final PasswordEncoder passwordEncoder;

    public TestDataInitializer(UserRepository userRepository, OrderRepository orderRepository,
                               OrderItemRepository orderItemRepository, ReturnRequestRepository returnRequestRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.returnRequestRepository = returnRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            System.out.println("--- Database already initialized. Skipping sample data insertion. ---");
            return;
        }
        System.out.println("--- Inserting Sample Data with Encrypted Passwords ---");
        
        // 1. Create Users with ENCRYPTED passwords
        User customer = new User(null, "Alice Customer", "alice@test.com", 
                               passwordEncoder.encode("password123"), Role.CUSTOMER);
        User admin = new User(null, "Bob Admin", "bob@admin.com", 
                             passwordEncoder.encode("adminpass"), Role.ADMIN);
        customer = userRepository.save(customer);
        admin = userRepository.save(admin);

        // 2. Create Order (Delivered 10 days ago, making it eligible for return)
        Order order1 = new Order();
        order1.setUser(customer);
        order1.setOrderDate(LocalDateTime.now().minusDays(35)); // Order placed 35 days ago (TOO LATE for 30-day policy)
        order1.setStatus(OrderStatus.DELIVERED);
        order1.setTotalAmount(150.00); 
        order1 = orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUser(customer);
        order2.setOrderDate(LocalDateTime.now().minusDays(10)); // Order placed 10 days ago (ELIGIBLE)
        order2.setStatus(OrderStatus.DELIVERED);
        order2.setTotalAmount(250.00);
        order2 = orderRepository.save(order2);
        
        // 3. Create Order Items for Order 2 (ELIGIBLE)
        OrderItem itemA = new OrderItem(null, "Bluetooth Headphones", 1, new BigDecimal("100.00"), order2, null);
        OrderItem itemB = new OrderItem(null, "Charging Cable", 2, new BigDecimal("25.00"), order2, null);
        itemA.setOrder(order2);
        itemB.setOrder(order2);
        
        itemA = orderItemRepository.save(itemA);
        itemB = orderItemRepository.save(itemB);

        // Update Order 2 with items
        order2.setItems(Arrays.asList(itemA, itemB));
        orderRepository.save(order2);

        // 4. Create a Pending Return Request for itemA (VALID)
        ReturnRequest request1 = new ReturnRequest();
        request1.setOrderItem(itemA);
        request1.setUser(customer);
        request1.setQuantityReturned(1);
        request1.setReason(ReturnRequest.ReturnReason.DAMAGED);
        request1.setStatus(ReturnRequest.ReturnStatus.PENDING);
        request1.setRefundAmount(new BigDecimal("100.00")); // Will be recalculated in service layer
        request1.setRequestDate(LocalDateTime.now());
        returnRequestRepository.save(request1);
        
        System.out.println("--- Sample Data Insertion Complete ---");
        System.out.println("Test Accounts Created:");
        System.out.println("Customer: alice@test.com / password123");
        System.out.println("Admin: bob@admin.com / adminpass");
    }
}