package com.helpdesk.ticket_service.service;

import com.helpdesk.ticket_service.dto.TicketCreateDto;
import com.helpdesk.ticket_service.dto.TicketResponseDto;
import com.helpdesk.ticket_service.model.Ticket;
import com.helpdesk.ticket_service.ticketRepository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public List<TicketResponseDto> findAllTickets(){
        return ticketRepository.findAll()
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
        return ticketRepository.findByCustomerId(id);
    }

    public TicketResponseDto createTicket(TicketCreateDto dto){
        ticketRepository.save(new Ticket(dto));

        return ticketRepository.findByTitle(dto.getTitle());
    }

}
