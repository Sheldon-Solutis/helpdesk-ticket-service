package com.helpdesk.ticket_service.controller;

import com.helpdesk.ticket_service.dto.*;
import com.helpdesk.ticket_service.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping()
    public ResponseEntity<List<TicketResponseDto>> findAllTickets() {
        return ResponseEntity.ok(ticketService.findAllTickets());
    }

    @GetMapping("/search")
    public ResponseEntity<List<TicketResponseDto>> searchTickets(
            @RequestParam(name = "word", required = false)
            String word) {
        return ResponseEntity.ok().body(ticketService.searchTickets(word));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDto> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok().body(ticketService.findById(id));
    }

    @GetMapping("/customer")
    public ResponseEntity<List<TicketResponseDto>> getTicketsOfOneClient(
            @RequestParam(name = "id") Long id) {
        return ResponseEntity.ok(ticketService.findTicketByCustomerId(id));
    }

    @PostMapping
    public ResponseEntity<TicketResponseDto> createTicket(
            @RequestBody @Valid TicketCreateDto dto) {
        TicketResponseDto responseDto = ticketService.createTicket(dto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

}