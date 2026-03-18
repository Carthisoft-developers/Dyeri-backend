package com.dyeri.gateway.fallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.Map;
@Slf4j @RestController @RequestMapping("/fallback")
public class FallbackController {
    @RequestMapping("/core")
    public Mono<ResponseEntity<Map<String,Object>>> coreServiceFallback() {
        log.warn("core-service circuit breaker OPEN");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error","Service temporarily unavailable","timestamp",Instant.now().toString())));
    }
    @RequestMapping("/notification")
    public Mono<ResponseEntity<Map<String,Object>>> notificationFallback() {
        return Mono.just(ResponseEntity.accepted()
                .body(Map.of("message","Notification queued","timestamp",Instant.now().toString())));
    }
    @RequestMapping("/payment")
    public Mono<ResponseEntity<Map<String,Object>>> paymentFallback() {
        log.warn("payment-service circuit breaker OPEN");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error","Payment service unavailable, retry later","timestamp",Instant.now().toString())));
    }
}