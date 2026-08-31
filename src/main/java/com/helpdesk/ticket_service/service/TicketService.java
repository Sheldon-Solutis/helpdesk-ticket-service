package com.helpdesk.ticket_service.service;

import com.helpdesk.ticket_service.Enums.Category;
import com.helpdesk.ticket_service.Enums.Priority;
import com.helpdesk.ticket_service.Enums.Status;
import com.helpdesk.ticket_service.dto.TicketCreateDto;
import com.helpdesk.ticket_service.dto.TicketResponseDto;
import com.helpdesk.ticket_service.dto.TicketUpdateDto;
import com.helpdesk.ticket_service.messaging.event.TicketAssignedEvent;
import com.helpdesk.ticket_service.messaging.event.TicketCreatedEvent;
import com.helpdesk.ticket_service.messaging.event.TicketDeletedEvent;
import com.helpdesk.ticket_service.messaging.event.TicketStatusChangedEvent;
import com.helpdesk.ticket_service.messaging.publisher.TicketEventPublisher;
import com.helpdesk.ticket_service.model.Ticket;
import com.helpdesk.ticket_service.ticketRepository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final String CREATED_ROUTING_KEY = "ticket.created";
    private final String ASSIGNED_ROUTING_KEY = "ticket.assigned";
    private final String CHANGED_ROUTING_KEY = "ticket.changed";
    private final String DELETED_ROUTING_KEY = "ticket.deleted";

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

    @Transactional
    public TicketResponseDto createTicket(TicketCreateDto dto, Long id){
        Ticket newTicket = new Ticket(dto);
        newTicket.setCustomerId(id);

        ticketRepository.save(newTicket);

        TicketCreatedEvent event = new TicketCreatedEvent(
                newTicket.getId(),
                newTicket.getTechnicianId(),
                newTicket.getCustomerId(),
                newTicket.getTitle(),
                newTicket.getPriority().toString(),
                newTicket.getCreatedAt());

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ticketEventPublisher.publish(CREATED_ROUTING_KEY, event);
                }
            });
        } else {
            ticketEventPublisher.publish(CREATED_ROUTING_KEY, event);
        }

        return new TicketResponseDto(newTicket);
    }

    @Transactional
    public TicketResponseDto updateTicketStatus(Long id, Status newStatus) {

        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Ticket não encontrado"));

        Status oldStatus = ticket.getStatus();

        validateStatusTransition(oldStatus, newStatus);

        ticket.setStatus(newStatus);

        Ticket updatedTicket = ticketRepository.save(ticket);

        TicketStatusChangedEvent event = new TicketStatusChangedEvent(
                updatedTicket.getId(),
                updatedTicket.getCustomerId(),
                updatedTicket.getTechnicianId(),
                oldStatus.toString(),
                newStatus.toString(),
                LocalDateTime.now()
        );

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ticketEventPublisher.publish(CHANGED_ROUTING_KEY, event);
                }
            });
        } else {
            ticketEventPublisher.publish(CHANGED_ROUTING_KEY, event);
        }

        return new TicketResponseDto(updatedTicket);
    }

    private void validateStatusTransition(Status current, Status next) {

        if (current == Status.CLOSED) {
            throw new EntityNotFoundException(
                    "Um ticket fechado não pode ter o status alterado"
            );
        }

        if (current == Status.RESOLVED && next != Status.CLOSED) {
            throw new EntityNotFoundException(
                    "Um ticket resolvido só pode ser fechado"
            );
        }
    }

    @Transactional
    public TicketResponseDto assignTicket(Long id, Long technician) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Ticket não encontrado"));
        ticket.setTechnicianId(technician);
        ticketRepository.save(ticket);

        TicketAssignedEvent event = new TicketAssignedEvent(
                id,
                technician,
                ticket.getTitle(),
                LocalDateTime.now());

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ticketEventPublisher.publish(ASSIGNED_ROUTING_KEY, event);
                }
            });
        } else {
            ticketEventPublisher.publish(ASSIGNED_ROUTING_KEY, event);
        }

        return new TicketResponseDto(ticket);
    }

    @Transactional
    public TicketResponseDto updateTicket(Long id, @Valid TicketUpdateDto dto) {
        Ticket ticket =  ticketRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Ticket Não Encontrado")
                );

        Status oldStatus = ticket.getStatus();

        if(!dto.getStatus().toString().isEmpty()){
            validateStatusTransition(ticket.getStatus(), dto.getStatus());
            ticket.setStatus(dto.getStatus());
        }

        if(!dto.getCategory().toString().isEmpty()) ticket.setCategory(dto.getCategory());

        if(!dto.getDescription().isEmpty()) ticket.setDescription(dto.getDescription());

        ticketRepository.save(ticket);

        TicketStatusChangedEvent event = new TicketStatusChangedEvent(
                ticket.getId(),
                ticket.getCustomerId(),
                ticket.getTechnicianId(),
                oldStatus.toString(),
                ticket.getStatus().toString(),
                LocalDateTime.now()
        );

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ticketEventPublisher.publish(CHANGED_ROUTING_KEY, event);
                }
            });
        } else {
            ticketEventPublisher.publish(CHANGED_ROUTING_KEY, event);
        }

        return new TicketResponseDto(ticket);
    }

    @Transactional
    public TicketResponseDto deleteTicket(Long id) {
        Ticket toDelete = ticketRepository.findById(id)
                        .orElseThrow(() ->
                                new EntityNotFoundException("Ticket Não Encontrado"));

        ticketRepository.delete(toDelete);

        TicketDeletedEvent event = new TicketDeletedEvent(
                toDelete.getId(),
                toDelete.getCustomerId(),
                toDelete.getTechnicianId(),
                toDelete.getTitle().toString(),
                LocalDateTime.now()
        );

        // para poder rodar nos testes unitários
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    ticketEventPublisher.publish(DELETED_ROUTING_KEY, event);
                }
            });
        } else {
            ticketEventPublisher.publish(DELETED_ROUTING_KEY, event);
        }

        return new TicketResponseDto(toDelete);
    }
}
