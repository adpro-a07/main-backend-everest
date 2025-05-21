package id.ac.ui.cs.advprog.everest.messaging;

import id.ac.ui.cs.advprog.everest.messaging.events.RepairOrderCompletedEvent;
import id.ac.ui.cs.advprog.everest.config.RabbitPublisherConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RepairEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishRepairCompleted(RepairOrderCompletedEvent event) {
        // Validate the event
        if (event == null) {
            throw new NullPointerException("RepairOrderCompletedEvent cannot be null");
        }

        rabbitTemplate.convertAndSend(
                RabbitPublisherConfig.ORDER_EXCHANGE,
                "repair.completed",
                event
        );
    }
}
