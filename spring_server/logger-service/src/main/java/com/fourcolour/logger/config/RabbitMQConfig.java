package com.fourcolour.logger.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String AUTH_LOGS_EXCHANGE = "auth_logs";
    public static final String MAP_COLORING_LOGS_EXCHANGE = "map_coloring_logs";
    public static final String MAP_STORAGE_LOGS_EXCHANGE = "map_storage_logs";

    // Queue names
    public static final String AUTH_LOGS_QUEUE = "auth_logs_queue";
    public static final String MAP_COLORING_LOGS_QUEUE = "map_coloring_logs_queue";
    public static final String MAP_STORAGE_LOGS_QUEUE = "map_storage_logs_queue";

    // Exchanges
    @Bean
    public FanoutExchange authLogsExchange() {
        return new FanoutExchange(AUTH_LOGS_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange mapColoringLogsExchange() {
        return new FanoutExchange(MAP_COLORING_LOGS_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange mapStorageLogsExchange() {
        return new FanoutExchange(MAP_STORAGE_LOGS_EXCHANGE, true, false);
    }

    // Queues
    @Bean
    public Queue authLogsQueue() {
        return new Queue(AUTH_LOGS_QUEUE, true);
    }

    @Bean
    public Queue mapColoringLogsQueue() {
        return new Queue(MAP_COLORING_LOGS_QUEUE, true);
    }

    @Bean
    public Queue mapStorageLogsQueue() {
        return new Queue(MAP_STORAGE_LOGS_QUEUE, true);
    }

    // Bindings
    @Bean
    public Binding authLogsBinding() {
        return BindingBuilder.bind(authLogsQueue()).to(authLogsExchange());
    }

    @Bean
    public Binding mapColoringLogsBinding() {
        return BindingBuilder.bind(mapColoringLogsQueue()).to(mapColoringLogsExchange());
    }

    @Bean
    public Binding mapStorageLogsBinding() {
        return BindingBuilder.bind(mapStorageLogsQueue()).to(mapStorageLogsExchange());
    }
} 