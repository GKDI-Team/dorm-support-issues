package com.dormsupport.issues.event;

import com.dormsupport.issues.issue.Issue;
import com.dormsupport.issues.issue.IssueStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class IssueEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(IssueEventPublisher.class);

    private final KafkaTemplate<String, IssueEvent> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String issueEventsTopic;

    public IssueEventPublisher(KafkaTemplate<String, IssueEvent> kafkaTemplate,
                               ApplicationEventPublisher applicationEventPublisher,
                               @Value("${events.issue-topic}") String issueEventsTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.applicationEventPublisher = applicationEventPublisher;
        this.issueEventsTopic = issueEventsTopic;
    }

    public void publishCreated(Issue issue) {
        applicationEventPublisher.publishEvent(IssueEvent.created(issue));
    }

    public void publishStatusChanged(Issue issue, IssueStatus previousStatus) {
        applicationEventPublisher.publishEvent(IssueEvent.statusChanged(issue, previousStatus));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterCommit(IssueEvent event) {
        try {
            kafkaTemplate.send(issueEventsTopic, event.issueId().toString(), event)
                    .get(10, TimeUnit.SECONDS);
            log.info("Published issue event {} for issue {} to topic {}",
                    event.eventType(), event.issueId(), issueEventsTopic);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while publishing issue event {} for issue {}",
                    event.eventType(), event.issueId(), exception);
        } catch (Exception exception) {
            log.error("Failed to publish issue event {} for issue {}",
                    event.eventType(), event.issueId(), exception);
        }
    }
}
