package com.helpdesk.ticket_service.ticketRepository;

import com.helpdesk.ticket_service.Enums.*;
import com.helpdesk.ticket_service.dto.TicketResponseDto;
import com.helpdesk.ticket_service.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findByCategory(Category category);
    List<Ticket> findByStatus(Status status);
    List<Ticket> findByPriority(Priority priority);

    List<Ticket> findByCustomerId(Long customerId);
    List<Ticket> findByTechnicianId(Long technicianId);

    List<Ticket> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Ticket> findByCreatedAtAfter(LocalDateTime start);

    TicketResponseDto findByTitle(String title);
    List<Ticket> findByTitleContaining(String title);
    List<Ticket> findByDescriptionContaining(String description);
}
