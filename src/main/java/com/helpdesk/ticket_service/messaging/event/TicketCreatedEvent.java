package com.helpdesk.ticket_service.messaging.event;

import java.time.LocalDateTime;

public record TicketCreatedEvent (
    Long ticketId,
    Long technicianId,
    Long customerId,
    String title,
    String priority,
    LocalDateTime createdAt
) {}
