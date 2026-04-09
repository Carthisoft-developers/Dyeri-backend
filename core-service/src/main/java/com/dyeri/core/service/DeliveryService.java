package com.dyeri.core.domain.services;
import com.dyeri.core.application.bean.request.LocationUpdateRequest;
import com.dyeri.core.application.bean.response.*;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

/** Inbound port for delivery driver operations. */
public interface DeliveryService {
    Flux<OrderResponse> getAvailableOrders(UUID driverId);
    Flux<OrderResponse> getHistory(UUID driverId, int page, int size);
    Mono<OrderResponse> acceptDelivery(UUID driverId, UUID orderId);
    Mono<Void> updateDriverLocation(UUID driverId, UUID orderId, LocationUpdateRequest request);
    Mono<OrderResponse> completeDelivery(UUID driverId, UUID orderId, FilePart proofPhoto);
    Mono<EarningsResponse> getEarnings(UUID driverId);
}
