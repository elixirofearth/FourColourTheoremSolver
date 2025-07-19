package com.fourcolour.logger.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.apache.kafka.clients.admin.NewTopic;
import com.fourcolour.logger.entity.Log;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    // Topic names (equivalent to RabbitMQ exchanges)
    public static final String AUTH_LOGS_TOPIC = "auth_logs";
    public static final String MAP_COLORING_LOGS_TOPIC = "map_coloring_logs";
    public static final String MAP_STORAGE_LOGS_TOPIC = "map_storage_logs";

    @Value("${spring.kafka.bootstrap-servers:kafka:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:logger-service-group}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, Log> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        
        return new DefaultKafkaConsumerFactory<>(props, 
            new StringDeserializer(), 
            new JsonDeserializer<>(Log.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Log> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Log> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }

    @Bean
    public ProducerFactory<String, Log> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.TYPE_MAPPINGS, "log:com.fourcolour.logger.entity.Log");
        
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Log> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put("bootstrap.servers", bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic authLogsTopic() {
        return new NewTopic(AUTH_LOGS_TOPIC, 1, (short) 1);
    }

    @Bean
    public NewTopic mapColoringLogsTopic() {
        return new NewTopic(MAP_COLORING_LOGS_TOPIC, 1, (short) 1);
    }

    @Bean
    public NewTopic mapStorageLogsTopic() {
        return new NewTopic(MAP_STORAGE_LOGS_TOPIC, 1, (short) 1);
    }
} 