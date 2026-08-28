package com.helpdesk.ticket_service.ticketRepository;

import com.helpdesk.ticket_service.dto.TicketCreateDto;
import com.helpdesk.ticket_service.dto.TicketResponseDto;
import com.helpdesk.ticket_service.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findByCategory(String category);
    List<Ticket> findByStatus(String status);
    List<Ticket> findByPriority(String priority);

    List<TicketResponseDto> findByCustomerId(Long customerId);
    List<Ticket> findByTechnicianId(Long technicianId);

    List<Ticket> findByCreatedAtBetween(Date start, Date end);
    List<Ticket> findByCreatedAtAfter(Date start);

    TicketResponseDto findByTitle(String title);
    List<TicketResponseDto> findByTitleContaining(String title);
    List<TicketResponseDto> findByDescriptionContaining(String description);
}
