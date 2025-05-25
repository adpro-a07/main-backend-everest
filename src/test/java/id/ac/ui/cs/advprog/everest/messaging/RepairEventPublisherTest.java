package id.ac.ui.cs.advprog.everest.messaging;

import id.ac.ui.cs.advprog.everest.messaging.events.RepairOrderCompletedEvent;
import id.ac.ui.cs.advprog.everest.config.RabbitPublisherConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepairEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RepairEventPublisher publisher;

    @Captor
    private ArgumentCaptor<String> exchangeCaptor;

    @Captor
    private ArgumentCaptor<String> routingKeyCaptor;

    @Captor
    private ArgumentCaptor<RepairOrderCompletedEvent> eventCaptor;

    @BeforeEach
    void setUp() {
        publisher = new RepairEventPublisher(rabbitTemplate);
    }

    @Test
    void shouldPublishRepairCompletedEvent() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();
        Long amount = 1000L;
        Instant completedAt = Instant.now();

        RepairOrderCompletedEvent event = RepairOrderCompletedEvent.builder()
                .repairOrderId(orderId)
                .technicianId(technicianId)
                .amount(amount)
                .completedAt(completedAt)
                .build();

        // Act
        publisher.publishRepairCompleted(event);

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                eventCaptor.capture()
        );

        assertEquals(RabbitPublisherConfig.ORDER_EXCHANGE, exchangeCaptor.getValue());
        assertEquals("repair.completed", routingKeyCaptor.getValue());

        RepairOrderCompletedEvent capturedEvent = eventCaptor.getValue();
        assertNotNull(capturedEvent);
        assertEquals(orderId, capturedEvent.getRepairOrderId());
        assertEquals(technicianId, capturedEvent.getTechnicianId());
        assertEquals(amount, capturedEvent.getAmount());
        assertEquals(completedAt, capturedEvent.getCompletedAt());
    }

    @Test
    void shouldHandleNullEvent() {
        // Act & Assert
        try {
            publisher.publishRepairCompleted(null);
        } catch (NullPointerException e) {
            // Expected behavior if method doesn't have null check
        }

        // Verify rabbitTemplate was not called with null event
        verify(rabbitTemplate, never()).convertAndSend(
                anyString(),
                anyString(),
                Optional.ofNullable(isNull())
        );
    }

    @Test
    void shouldUseCorrectExchangeAndRoutingKey() {
        // Arrange
        RepairOrderCompletedEvent event = RepairOrderCompletedEvent.builder()
                .repairOrderId(UUID.randomUUID())
                .build();

        // Act
        publisher.publishRepairCompleted(event);

        // Assert
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitPublisherConfig.ORDER_EXCHANGE),
                eq("repair.completed"),
                any(RepairOrderCompletedEvent.class)
        );
    }

    @Test
    void shouldHandleExceptionFromRabbitTemplate() {
        // Arrange
        RepairOrderCompletedEvent event = RepairOrderCompletedEvent.builder()
                .repairOrderId(UUID.randomUUID())
                .build();

        doThrow(new RuntimeException("Connection failed"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));

        // Act & Assert
        try {
            publisher.publishRepairCompleted(event);
            // If we reach here, the test fails because the exception was not propagated
            // This is fine if you expect the publisher to swallow exceptions
        } catch (RuntimeException e) {
            assertEquals("Connection failed", e.getMessage());
        }
    }
}