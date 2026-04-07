package com.dyeri.core.infrastructure.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.ExecutorSubscribableChannel;

@Configuration
public class WebSocketConfig {
	@Bean
	public SimpMessagingTemplate simpMessagingTemplate() {
		return new SimpMessagingTemplate(new ExecutorSubscribableChannel());
	}
}