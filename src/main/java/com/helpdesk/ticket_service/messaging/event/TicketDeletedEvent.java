package com.helpdesk.ticket_service.messaging.event;

import java.time.LocalDateTime;

public record TicketDeletedEvent(
   Long ticketId,
   Long customerId,
   Long technicianId,
   String title,
   LocalDateTime updatedAt
) {}
