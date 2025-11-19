package com.empresa.ecommerce_backend.service;

import com.empresa.ecommerce_backend.enums.FulfillmentType;
import com.empresa.ecommerce_backend.enums.OrderStatus;
import com.empresa.ecommerce_backend.enums.PaymentStatus;
import com.empresa.ecommerce_backend.model.Order;
import com.empresa.ecommerce_backend.model.OrderItem;
import com.empresa.ecommerce_backend.model.ProductVariant;
import com.empresa.ecommerce_backend.repository.OrderRepository;
import com.empresa.ecommerce_backend.repository.ProductVariantRepository;
import com.empresa.ecommerce_backend.service.interfaces.OrderCleanupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupServiceImpl implements OrderCleanupService {

    private final OrderRepository orderRepo;
    private final ProductVariantRepository variantRepo;

    /**
     * Scheduler que corre cada 5 minutos.
     */
    @Override
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @Transactional
    public void cancelExpiredOrders() {

        LocalDateTime now = LocalDateTime.now();

        List<Order> expiredOrders =
                orderRepo.findAllByStatusAndExpiresAtBefore(OrderStatus.PENDING, now);

        if (expiredOrders.isEmpty()) return;

        log.info("⏳ Cancelando {} órdenes expiradas...", expiredOrders.size());

        for (Order order : expiredOrders) {

            PaymentStatus ps = order.getPayment() != null
                    ? order.getPayment().getStatus()
                    : null;

            // Si ya está aprobado, ignoramos
            if (ps == PaymentStatus.APPROVED) {
                continue;
            }

            // 1. Devolver stock si corresponde
            for (OrderItem item : order.getItems()) {
                ProductVariant variant = item.getVariant();
                if (variant == null) continue;

                boolean isOnDemand =
                        variant.getFulfillmentType() == FulfillmentType.DIGITAL_ON_DEMAND;

                if (!isOnDemand) {
                    int stock = variant.getStock() == null ? 0 : variant.getStock();
                    variant.setStock(stock + item.getQuantity());
                    variantRepo.save(variant);
                }
            }

            // 2. Cancelar orden
            order.setStatus(OrderStatus.CANCELED);

            // 3. Cancelar payment si existe y no está aprobado
            if (order.getPayment() != null) {

                switch (ps) {

                    case INITIATED, PENDING -> {
                        // Expiró sin pagar
                        order.getPayment().setStatus(PaymentStatus.EXPIRED);
                    }

                    case REJECTED, CANCELED -> {
                        // Ya estaba rechazado/cancelado, no cambiar
                    }

                    default -> {
                        // null o cualquier otro caso raro
                        order.getPayment().setStatus(PaymentStatus.EXPIRED);
                    }
                }
            }

            orderRepo.save(order);

            log.info("❌ Orden {} fue cancelada por expiración.", order.getOrderNumber());
        }
    }
}
