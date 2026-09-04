package com.helpdesk.ticket_service.ticketRepository;

import com.helpdesk.ticket_service.Enums.*;
import com.helpdesk.ticket_service.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Long>, JpaSpecificationExecutor<Ticket> {
    List<Ticket> findByCategory(Category category);
    List<Ticket> findByStatus(Status status);
    List<Ticket> findByPriority(Priority priority);

    List<Ticket> findByCustomerId(Long customerId);
    List<Ticket> findByTechnicianId(Long technicianId);

    List<Ticket> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Ticket> findByCreatedAtAfter(LocalDateTime start);

    List<Ticket> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
