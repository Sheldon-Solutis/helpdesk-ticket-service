package com.helpdesk.ticket_service.messaging.publisher;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    public static final String EXCHANGE_NAME = "helpdesk.exchange";

    public void publishCreated(
            String routingKey,
            Object event
    ){

        rabbitTemplate.convertAndSend(
                EXCHANGE_NAME,
                routingKey,
                event,
                message -> {
                    message.getMessageProperties()
                            .setHeader("event.type","ticket.created");
                    return message;
                }
        );
    }

    public void publishAssigned(
            String routingKey,
            Object event
    ){

        rabbitTemplate.convertAndSend(
                EXCHANGE_NAME,
                routingKey,
                event,
                message -> {
                    message.getMessageProperties()
                            .setHeader("event.type","ticket.assigned");
                    return message;
                }
        );
    }

    public void publishStatusChanged(
            String routingKey,
            Object event
    ){

        rabbitTemplate.convertAndSend(
                EXCHANGE_NAME,
                routingKey,
                event,
                message -> {
                    message.getMessageProperties()
                            .setHeader("event.type","ticket.statuschanged");
                    return message;
                }
        );
    }

}
