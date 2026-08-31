package com.helpdesk.ticket_service.messaging.event;

import java.time.LocalDateTime;

public record TicketStatusChangedEvent(
        Long ticketId,
        Long customerId,
        Long technicianId,
        String previousStatus,
        String newStatus,
        LocalDateTime updatedAt
) {
}
