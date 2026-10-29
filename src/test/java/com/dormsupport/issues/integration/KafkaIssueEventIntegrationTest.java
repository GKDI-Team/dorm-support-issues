package com.dormsupport.issues.integration;

import com.dormsupport.issues.event.IssueEvent;
import com.dormsupport.issues.event.IssueEventPublisher;
import com.dormsupport.issues.issue.IssueStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
class KafkaIssueEventIntegrationTest {

private static final String TOPIC = "issues.integration.events";

@Container
private static final KafkaContainer KAFKA =
        new KafkaContainer(DockerImageName.parse("apache/kafka-native:3.8.0"));

private static KafkaTemplate<String, IssueEvent> kafkaTemplate;
private static DefaultKafkaProducerFactory<String, IssueEvent> producerFactory;
private static KafkaConsumer<String, String> consumer;
private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

@BeforeAll
static void setUp() {
    Map<String, Object> producerProperties = Map.of(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
    );

    producerFactory = new DefaultKafkaProducerFactory<>(producerProperties);
    kafkaTemplate = new KafkaTemplate<>(producerFactory);

    Properties consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "issues-integration-test-" + UUID.randomUUID());
    consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

    consumer = new KafkaConsumer<>(consumerProperties);
    consumer.subscribe(List.of(TOPIC));
}

@AfterAll
static void tearDown() {
    if (consumer != null) {
        consumer.close();
    }
    if (producerFactory != null) {
        producerFactory.destroy();
    }
}

@Test
void shouldPublishIssueCreatedEventToConfiguredTopic() throws Exception {
    UUID issueId = UUID.randomUUID();

    IssueEvent event = new IssueEvent(
            UUID.randomUUID(),
            "issue.created",
            issueId,
            IssueStatus.NEW,
            null,
            Instant.parse("2026-01-01T10:00:00Z"),
            "Leaking tap",
            "The tap is leaking",
            UUID.randomUUID(),
            "101",
            "Bathroom",
            "Plumbing"
    );

    publishAndAssert(event);
}

@Test
void shouldPublishIssueStatusChangedEventToConfiguredTopic() throws Exception {
    UUID issueId = UUID.randomUUID();

    IssueEvent event = new IssueEvent(
            UUID.randomUUID(),
            "issue.status_changed",
            issueId,
            IssueStatus.IN_PROGRESS,
            IssueStatus.NEW,
            Instant.parse("2026-01-01T10:05:00Z"),
            "Leaking tap",
            "The tap is leaking",
            UUID.randomUUID(),
            "101",
            "Bathroom",
            "Plumbing"
    );

    publishAndAssert(event);
}

private void publishAndAssert(IssueEvent event) throws Exception {
    IssueEventPublisher publisher = new IssueEventPublisher(
            kafkaTemplate,
            ignored -> {},
            TOPIC
    );

    publisher.sendAfterCommit(event);

    ConsumerRecord<String, String> record = waitForRecord(event.issueId().toString());

    assertNotNull(record);
    assertEquals(TOPIC, record.topic());
    assertEquals(event.issueId().toString(), record.key());

    IssueEvent received = objectMapper.readValue(record.value(), IssueEvent.class);

    assertEquals(event.eventId(), received.eventId());
    assertEquals(event.eventType(), received.eventType());
    assertEquals(event.issueId(), received.issueId());
    assertEquals(event.status(), received.status());
    assertEquals(event.previousStatus(), received.previousStatus());
    assertEquals(event.title(), received.title());
    assertEquals(event.descriptionPreview(), received.descriptionPreview());
    assertEquals(event.blockId(), received.blockId());
    assertEquals(event.roomNumber(), received.roomNumber());
    assertEquals(event.locationDetails(), received.locationDetails());
    assertEquals(event.categoryName(), received.categoryName());
}

private ConsumerRecord<String, String> waitForRecord(String expectedKey) {
    long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();

    while (System.nanoTime() < deadline) {
        var records = consumer.poll(Duration.ofMillis(250));

        for (ConsumerRecord<String, String> record : records) {
            if (expectedKey.equals(record.key())) {
                return record;
            }
        }
    }

    throw new AssertionError("Kafka message was not received for key " + expectedKey);
}

}