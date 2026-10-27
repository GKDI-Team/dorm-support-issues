package com.dormsupport.issues.event;

import com.dormsupport.issues.category.IssueCategory;
import com.dormsupport.issues.issue.Issue;
import com.dormsupport.issues.issue.IssueStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IssueEventPublisherTest {

    private static final String TOPIC = "issues.events";

    private KafkaTemplate<String, IssueEvent> kafkaTemplate;
    private ApplicationEventPublisher applicationEventPublisher;
    private IssueEventPublisher publisher;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock();
        applicationEventPublisher = mock();

        publisher = new IssueEventPublisher(
                kafkaTemplate,
                applicationEventPublisher,
                TOPIC
        );
    }

    @AfterEach
    void clearInterruptFlag() {
        // Прерывание восстанавливается тестируемым методом.
        // Очищаем флаг, чтобы он не влиял на другие тесты.
        Thread.interrupted();
    }

    @Test
    void publishCreated_shouldPublishCreatedEvent() {
        Issue issue = createIssue("Water leak");

        publisher.publishCreated(issue);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertInstanceOf(IssueEvent.class, captor.getValue());
        IssueEvent event = (IssueEvent) captor.getValue();

        assertEquals("issue.created", event.eventType());
        assertEquals(issue.getId(), event.issueId());
        assertNull(event.previousStatus());
    }

    @Test
    void publishStatusChanged_shouldPublishStatusChangedEvent() {
        Issue issue = createIssue("Water leak");
        IssueStatus previousStatus = IssueStatus.NEW;

        when(issue.getStatus()).thenReturn(IssueStatus.IN_PROGRESS);

        publisher.publishStatusChanged(issue, previousStatus);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        assertInstanceOf(IssueEvent.class, captor.getValue());
        IssueEvent event = (IssueEvent) captor.getValue();

        assertEquals("issue.status_changed", event.eventType());
        assertEquals(issue.getId(), event.issueId());
        assertEquals(IssueStatus.IN_PROGRESS, event.status());
        assertEquals(IssueStatus.NEW, event.previousStatus());
    }

    @Test
    void sendAfterCommit_shouldSendEventToConfiguredTopic() throws Exception {
        IssueEvent event = createEvent();
        CompletableFuture<SendResult<String, IssueEvent>> future =
                mock();

        when(kafkaTemplate.send(TOPIC, event.issueId().toString(), event))
                .thenReturn(future);

        publisher.sendAfterCommit(event);

        verify(kafkaTemplate).send(TOPIC, event.issueId().toString(), event);
        verify(future).get(10, TimeUnit.SECONDS);
    }

    @Test
    void sendAfterCommit_shouldHandleKafkaFailure() throws Exception {
        IssueEvent event = createEvent();
        CompletableFuture<SendResult<String, IssueEvent>> future =
                mock();

        when(kafkaTemplate.send(TOPIC, event.issueId().toString(), event))
                .thenReturn(future);
        when(future.get(10, TimeUnit.SECONDS))
                .thenThrow(new RuntimeException("Kafka unavailable"));

        assertDoesNotThrow(() -> publisher.sendAfterCommit(event));
    }

    @Test
    void sendAfterCommit_shouldHandleTimeout() throws Exception {
        IssueEvent event = createEvent();
        CompletableFuture<SendResult<String, IssueEvent>> future =
                mock();

        when(kafkaTemplate.send(TOPIC, event.issueId().toString(), event))
                .thenReturn(future);
        when(future.get(10, TimeUnit.SECONDS))
                .thenThrow(new TimeoutException("Kafka send timed out"));

        assertDoesNotThrow(() -> publisher.sendAfterCommit(event));
    }

    @Test
    void sendAfterCommit_shouldRestoreInterruptFlagWhenInterrupted()
            throws Exception {
        IssueEvent event = createEvent();
        CompletableFuture<SendResult<String, IssueEvent>> future =
                mock();

        when(kafkaTemplate.send(TOPIC, event.issueId().toString(), event))
                .thenReturn(future);
        when(future.get(10, TimeUnit.SECONDS))
                .thenThrow(new InterruptedException("Interrupted"));

        try {
            assertDoesNotThrow(() -> publisher.sendAfterCommit(event));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    private Issue createIssue(String description) {
        Issue issue = mock();
        IssueCategory category = mock();

        when(issue.getId()).thenReturn(UUID.randomUUID());
        when(issue.getStatus()).thenReturn(IssueStatus.NEW);
        when(issue.getTitle()).thenReturn("Water leak");
        when(issue.getDescription()).thenReturn(description);
        when(issue.getBlockId()).thenReturn(UUID.randomUUID());
        when(issue.getRoomNumber()).thenReturn("101");
        when(issue.getLocationDetails()).thenReturn("Bathroom");
        when(issue.getCategory()).thenReturn(category);
        when(category.getName()).thenReturn("Plumbing");

        return issue;
    }

    private IssueEvent createEvent() {
        return new IssueEvent(
                UUID.randomUUID(),
                "issue.created",
                UUID.randomUUID(),
                IssueStatus.NEW,
                null,
                java.time.Instant.now(),
                "Water leak",
                "Water is leaking",
                UUID.randomUUID(),
                "101",
                "Bathroom",
                "Plumbing"
        );
    }
}