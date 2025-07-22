package com.fourcolour.logger.config;

import com.fourcolour.logger.entity.Log;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestPropertySource(properties = {
    "spring.kafka.bootstrap-servers=localhost:9092",
    "spring.kafka.consumer.group-id=test-logger-service-group"
})
class KafkaConfigTest {

    @Test
    void kafkaConfig_ShouldCreateConsumerFactoryWithCorrectProperties() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-logger-service-group");

        ConsumerFactory<String, Log> consumerFactory = kafkaConfig.consumerFactory();

        assertNotNull(consumerFactory);
        
        // Verify the factory is properly configured
        assertTrue(consumerFactory instanceof org.springframework.kafka.core.DefaultKafkaConsumerFactory);
    }

    @Test
    void kafkaConfig_ShouldCreateProducerFactoryWithCorrectProperties() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        ProducerFactory<String, Log> producerFactory = kafkaConfig.producerFactory();

        assertNotNull(producerFactory);
        
        // Verify the factory is properly configured
        assertTrue(producerFactory instanceof org.springframework.kafka.core.DefaultKafkaProducerFactory);
    }

    @Test
    void kafkaConfig_ShouldCreateKafkaTemplate() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        KafkaTemplate<String, Log> kafkaTemplate = kafkaConfig.kafkaTemplate();

        assertNotNull(kafkaTemplate);
    }

    @Test
    void kafkaConfig_ShouldCreateKafkaListenerContainerFactory() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-logger-service-group");

        ConcurrentKafkaListenerContainerFactory<String, Log> factory = kafkaConfig.kafkaListenerContainerFactory();

        assertNotNull(factory);
    }

    @Test
    void kafkaConfig_ShouldCreateKafkaAdmin() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        KafkaAdmin kafkaAdmin = kafkaConfig.kafkaAdmin();

        assertNotNull(kafkaAdmin);
    }

    @Test
    void kafkaConfig_ShouldCreateAuthLogsTopic() {
        KafkaConfig kafkaConfig = new KafkaConfig();

        NewTopic authLogsTopic = kafkaConfig.authLogsTopic();

        assertNotNull(authLogsTopic);
        assertEquals(KafkaConfig.AUTH_LOGS_TOPIC, authLogsTopic.name());
        assertEquals(1, authLogsTopic.numPartitions());
        assertEquals((short) 1, authLogsTopic.replicationFactor());
    }

    @Test
    void kafkaConfig_ShouldCreateMapColoringLogsTopic() {
        KafkaConfig kafkaConfig = new KafkaConfig();

        NewTopic mapColoringLogsTopic = kafkaConfig.mapColoringLogsTopic();

        assertNotNull(mapColoringLogsTopic);
        assertEquals(KafkaConfig.MAP_COLORING_LOGS_TOPIC, mapColoringLogsTopic.name());
        assertEquals(1, mapColoringLogsTopic.numPartitions());
        assertEquals((short) 1, mapColoringLogsTopic.replicationFactor());
    }

    @Test
    void kafkaConfig_ShouldCreateMapStorageLogsTopic() {
        KafkaConfig kafkaConfig = new KafkaConfig();

        NewTopic mapStorageLogsTopic = kafkaConfig.mapStorageLogsTopic();

        assertNotNull(mapStorageLogsTopic);
        assertEquals(KafkaConfig.MAP_STORAGE_LOGS_TOPIC, mapStorageLogsTopic.name());
        assertEquals(1, mapStorageLogsTopic.numPartitions());
        assertEquals((short) 1, mapStorageLogsTopic.replicationFactor());
    }

    @Test
    void kafkaConfig_TopicConstants_ShouldHaveCorrectValues() {
        assertEquals("auth_logs", KafkaConfig.AUTH_LOGS_TOPIC);
        assertEquals("map_coloring_logs", KafkaConfig.MAP_COLORING_LOGS_TOPIC);
        assertEquals("map_storage_logs", KafkaConfig.MAP_STORAGE_LOGS_TOPIC);
    }

    @Test
    void kafkaConfig_ConsumerFactory_ShouldHaveCorrectDeserializers() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-logger-service-group");

        ConsumerFactory<String, Log> consumerFactory = kafkaConfig.consumerFactory();

        assertNotNull(consumerFactory);
        
        // Verify the deserializers are correctly configured
        assertNotNull(consumerFactory.getKeyDeserializer());
        assertNotNull(consumerFactory.getValueDeserializer());
    }

    @Test
    void kafkaConfig_ProducerFactory_ShouldHaveCorrectSerializers() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        ProducerFactory<String, Log> producerFactory = kafkaConfig.producerFactory();

        assertNotNull(producerFactory);
        
        // Verify the serializers are correctly configured by checking the configuration properties
        Map<String, Object> configs = producerFactory.getConfigurationProperties();
        assertNotNull(configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
        assertNotNull(configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }

    @Test
    void kafkaConfig_ConsumerFactory_ShouldHaveCorrectConfiguration() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-logger-service-group");

        ConsumerFactory<String, Log> consumerFactory = kafkaConfig.consumerFactory();

        assertNotNull(consumerFactory);
        
        // Verify the configuration properties
        Map<String, Object> configs = consumerFactory.getConfigurationProperties();
        assertEquals("localhost:9092", configs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("test-logger-service-group", configs.get(ConsumerConfig.GROUP_ID_CONFIG));
        assertEquals("earliest", configs.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));
        assertEquals("*", configs.get(JsonDeserializer.TRUSTED_PACKAGES));
        assertEquals("log:com.fourcolour.logger.entity.Log", configs.get(JsonDeserializer.TYPE_MAPPINGS));
    }

    @Test
    void kafkaConfig_ProducerFactory_ShouldHaveCorrectConfiguration() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        ProducerFactory<String, Log> producerFactory = kafkaConfig.producerFactory();

        assertNotNull(producerFactory);
        
        // Verify the configuration properties
        Map<String, Object> configs = producerFactory.getConfigurationProperties();
        assertEquals("localhost:9092", configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("log:com.fourcolour.logger.entity.Log", configs.get(JsonSerializer.TYPE_MAPPINGS));
    }

    @Test
    void kafkaConfig_KafkaAdmin_ShouldHaveCorrectConfiguration() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        KafkaAdmin kafkaAdmin = kafkaConfig.kafkaAdmin();

        assertNotNull(kafkaAdmin);
        
        // Verify the configuration
        Map<String, Object> configs = kafkaAdmin.getConfigurationProperties();
        assertEquals("localhost:9092", configs.get("bootstrap.servers"));
    }

    @Test
    void kafkaConfig_WithDifferentBootstrapServers_ShouldUseCorrectServers() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "kafka-cluster:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-group");

        ConsumerFactory<String, Log> consumerFactory = kafkaConfig.consumerFactory();
        ProducerFactory<String, Log> producerFactory = kafkaConfig.producerFactory();
        KafkaAdmin kafkaAdmin = kafkaConfig.kafkaAdmin();

        assertNotNull(consumerFactory);
        assertNotNull(producerFactory);
        assertNotNull(kafkaAdmin);
        
        // Verify the configuration uses the correct bootstrap servers
        Map<String, Object> consumerConfigs = consumerFactory.getConfigurationProperties();
        Map<String, Object> producerConfigs = producerFactory.getConfigurationProperties();
        Map<String, Object> adminConfigs = kafkaAdmin.getConfigurationProperties();
        
        assertEquals("kafka-cluster:9092", consumerConfigs.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("kafka-cluster:9092", producerConfigs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        assertEquals("kafka-cluster:9092", adminConfigs.get("bootstrap.servers"));
    }

    @Test
    void kafkaConfig_WithDifferentGroupId_ShouldUseCorrectGroupId() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "custom-group-id");

        ConsumerFactory<String, Log> consumerFactory = kafkaConfig.consumerFactory();

        assertNotNull(consumerFactory);
        
        // Verify the configuration uses the correct group ID
        Map<String, Object> configs = consumerFactory.getConfigurationProperties();
        assertEquals("custom-group-id", configs.get(ConsumerConfig.GROUP_ID_CONFIG));
    }

    @Test
    void kafkaConfig_Topics_ShouldHaveUniqueNames() {
        KafkaConfig kafkaConfig = new KafkaConfig();

        NewTopic authLogsTopic = kafkaConfig.authLogsTopic();
        NewTopic mapColoringLogsTopic = kafkaConfig.mapColoringLogsTopic();
        NewTopic mapStorageLogsTopic = kafkaConfig.mapStorageLogsTopic();

        assertNotEquals(authLogsTopic.name(), mapColoringLogsTopic.name());
        assertNotEquals(authLogsTopic.name(), mapStorageLogsTopic.name());
        assertNotEquals(mapColoringLogsTopic.name(), mapStorageLogsTopic.name());
    }

    @Test
    void kafkaConfig_Topics_ShouldHaveConsistentConfiguration() {
        KafkaConfig kafkaConfig = new KafkaConfig();

        NewTopic authLogsTopic = kafkaConfig.authLogsTopic();
        NewTopic mapColoringLogsTopic = kafkaConfig.mapColoringLogsTopic();
        NewTopic mapStorageLogsTopic = kafkaConfig.mapStorageLogsTopic();

        // All topics should have the same partition and replication configuration
        assertEquals(1, authLogsTopic.numPartitions());
        assertEquals(1, mapColoringLogsTopic.numPartitions());
        assertEquals(1, mapStorageLogsTopic.numPartitions());
        
        assertEquals((short) 1, authLogsTopic.replicationFactor());
        assertEquals((short) 1, mapColoringLogsTopic.replicationFactor());
        assertEquals((short) 1, mapStorageLogsTopic.replicationFactor());
    }

    @Test
    void kafkaConfig_JsonDeserializer_ShouldBeConfiguredForLogClass() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-group");

        ConsumerFactory<String, Log> consumerFactory = kafkaConfig.consumerFactory();

        assertNotNull(consumerFactory);
        
        // Verify the JsonDeserializer is configured for the Log class
        Map<String, Object> configs = consumerFactory.getConfigurationProperties();
        assertEquals("log:com.fourcolour.logger.entity.Log", configs.get(JsonDeserializer.TYPE_MAPPINGS));
        assertEquals("*", configs.get(JsonDeserializer.TRUSTED_PACKAGES));
    }

    @Test
    void kafkaConfig_JsonSerializer_ShouldBeConfiguredForLogClass() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        ProducerFactory<String, Log> producerFactory = kafkaConfig.producerFactory();

        assertNotNull(producerFactory);
        
        // Verify the JsonSerializer is configured for the Log class
        Map<String, Object> configs = producerFactory.getConfigurationProperties();
        assertEquals("log:com.fourcolour.logger.entity.Log", configs.get(JsonSerializer.TYPE_MAPPINGS));
    }

    @Test
    void kafkaConfig_ConsumerFactory_ShouldUseStringDeserializerForKey() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-group");

        ConsumerFactory<String, Log> consumerFactory = kafkaConfig.consumerFactory();

        assertNotNull(consumerFactory);
        
        // Verify the key deserializer is StringDeserializer
        Map<String, Object> configs = consumerFactory.getConfigurationProperties();
        assertEquals(org.apache.kafka.common.serialization.StringDeserializer.class, 
                   configs.get(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG));
    }

    @Test
    void kafkaConfig_ProducerFactory_ShouldUseStringSerializerForKey() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        ProducerFactory<String, Log> producerFactory = kafkaConfig.producerFactory();

        assertNotNull(producerFactory);
        
        // Verify the key serializer is StringSerializer
        Map<String, Object> configs = producerFactory.getConfigurationProperties();
        assertEquals(org.apache.kafka.common.serialization.StringSerializer.class, 
                   configs.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
    }

    @Test
    void kafkaConfig_ConsumerFactory_ShouldUseJsonDeserializerForValue() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
        ReflectionTestUtils.setField(kafkaConfig, "groupId", "test-group");

        ConsumerFactory<String, Log> consumerFactory = kafkaConfig.consumerFactory();

        assertNotNull(consumerFactory);
        
        // Verify the value deserializer is JsonDeserializer
        Map<String, Object> configs = consumerFactory.getConfigurationProperties();
        assertEquals(org.springframework.kafka.support.serializer.JsonDeserializer.class, 
                   configs.get(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG));
    }

    @Test
    void kafkaConfig_ProducerFactory_ShouldUseJsonSerializerForValue() {
        KafkaConfig kafkaConfig = new KafkaConfig();
        
        // Set properties using reflection
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");

        ProducerFactory<String, Log> producerFactory = kafkaConfig.producerFactory();

        assertNotNull(producerFactory);
        
        // Verify the value serializer is JsonSerializer
        Map<String, Object> configs = producerFactory.getConfigurationProperties();
        assertEquals(org.springframework.kafka.support.serializer.JsonSerializer.class, 
                   configs.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
    }
} 