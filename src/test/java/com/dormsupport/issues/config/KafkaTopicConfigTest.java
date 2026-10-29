package com.dormsupport.issues.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaTopicConfigTest {

    private final KafkaTopicConfig config = new KafkaTopicConfig();

    @Test
    void issueEventsTopic_shouldUseConfiguredName() {
        NewTopic topic = config.issueEventsTopic("issues.events");

        assertNotNull(topic);
        assertEquals("issues.events", topic.name());
    }

    @Test
    void issueEventsTopic_shouldHaveOnePartition() {
        NewTopic topic = config.issueEventsTopic("issues.events");

        assertEquals(1, topic.numPartitions());
    }

    @Test
    void issueEventsTopic_shouldHaveReplicationFactorOfOne() {
        NewTopic topic = config.issueEventsTopic("issues.events");

        assertEquals(1, topic.replicationFactor());
    }
}