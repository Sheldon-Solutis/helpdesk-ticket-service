package com.helpdesk.ticket_service.messaging.event;

import java.time.LocalDateTime;

public record TicketAssignedEvent(
   Long ticketId,
   Long technicianId,
   String title,
   LocalDateTime updatedAt
) {}
