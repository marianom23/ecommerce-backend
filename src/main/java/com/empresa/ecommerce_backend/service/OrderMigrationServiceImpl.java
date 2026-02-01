package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.model.Order;
import com.empresa.ecommerce_backend.model.User;
import com.empresa.ecommerce_backend.repository.OrderRepository;
import com.empresa.ecommerce_backend.repository.UserRepository;
import com.empresa.ecommerce_backend.service.interfaces.OrderMigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderMigrationServiceImpl implements OrderMigrationService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public int migrateGuestOrdersToUser(String email, Long userId) {
        if (email == null || userId == null)
            return 0;

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User ID {} not found for migration", userId);
            return 0;
        }

        List<Order> guestOrders = orderRepository.findByGuestEmailOrderByCreatedAtDesc(email);
        int count = 0;

        for (Order o : guestOrders) {
            if (o.getUser() == null) {
                o.setUser(user);
                // Opcional: limpiar guestEmail o dejarlo como histórico
                // o.setGuestEmail(null);
                orderRepository.save(o);
                count++;
            }
        }

        return count;
    }
}
