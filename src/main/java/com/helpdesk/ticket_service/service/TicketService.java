package com.helpdesk.ticket_service.service;

import com.helpdesk.ticket_service.Enums.Category;
import com.helpdesk.ticket_service.Enums.Priority;
import com.helpdesk.ticket_service.Enums.Status;
import com.helpdesk.ticket_service.dto.TicketCreateDto;
import com.helpdesk.ticket_service.dto.TicketResponseDto;
import com.helpdesk.ticket_service.messaging.event.TicketCreatedEvent;
import com.helpdesk.ticket_service.messaging.publisher.TicketEventPublisher;
import com.helpdesk.ticket_service.model.Ticket;
import com.helpdesk.ticket_service.ticketRepository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final String CREATED_ROUTING_KEY = "ticket.created";
    private final String ASSIGNED_ROUTING_KEY = "ticket.assigned";
    private final String CHANGED_ROUTING_KEY = "ticket.changed";

    private final TicketRepository ticketRepository;
    private final TicketEventPublisher ticketEventPublisher;

    public List<TicketResponseDto> TicketFilter(String status, String category, String priority) {
        if (status == null) {
            if(category == null) {
                if(priority == null) {
                    return ticketRepository.findAll()
                            .stream()
                            .map(TicketResponseDto::new)
                            .toList();
                }

                return ticketRepository.findByPriority(Priority.valueOf(priority.toUpperCase()))
                        .stream()
                        .map(TicketResponseDto::new)
                        .toList();
            }

            return ticketRepository.findByCategory(Category.valueOf(category.toUpperCase()))
                    .stream()
                    .map(TicketResponseDto::new)
                    .toList();
        }

        return ticketRepository.findByStatus(Status.valueOf(status.toUpperCase()))
                .stream()
                .map(TicketResponseDto::new)
                .toList();
    }

    public List<TicketResponseDto> findAllTickets(){
        return ticketRepository.findAll()
                                .stream()
                                .map(TicketResponseDto::new)
                                .toList();
    }

    public List<TicketResponseDto> searchTickets(String word) {
        return ticketRepository
                .findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(word)
                .stream()
                .map(TicketResponseDto::new)
                .toList();
    }

    public TicketResponseDto findById(Long id) {
        Ticket finded = ticketRepository.findById(id)
                            .orElseThrow(() ->
                                 new EntityNotFoundException(
                                        "Ticket Not Found With Id: " + id));
        return new TicketResponseDto(finded);
    }

    public List<TicketResponseDto> findTicketByCustomerId(Long id) {
        return ticketRepository.findByCustomerId(id)
                .stream()
                .map(TicketResponseDto::new)
                .toList();
    }

    public TicketResponseDto createTicket(TicketCreateDto dto, Long id){
        Ticket newTicket = new Ticket(dto);
        newTicket.setCustomerId(id);

        ticketRepository.save(newTicket);

        ticketEventPublisher.publishCreated(
                CREATED_ROUTING_KEY,
                new TicketCreatedEvent(
                        newTicket.getId(),
                        newTicket.getTechnicianId(),
                        newTicket.getCustomerId(),
                        newTicket.getTitle(),
                        newTicket.getPriority().toString(),
                        newTicket.getCreatedAt()));

        return new TicketResponseDto(newTicket);
    }

}
