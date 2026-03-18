package com.dyeri.notification.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
@Configuration @EnableR2dbcAuditing
public class R2dbcConfig {
    @Bean public ReactiveTransactionManager txManager(ConnectionFactory cf) { return new R2dbcTransactionManager(cf); }
    @Bean public TransactionalOperator txOp(ReactiveTransactionManager tm) { return TransactionalOperator.create(tm); }
}