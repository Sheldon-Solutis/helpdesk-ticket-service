package com.helpdesk.ticket_service.controller;

import com.helpdesk.ticket_service.Enums.Status;
import com.helpdesk.ticket_service.dto.*;
import com.helpdesk.ticket_service.service.TicketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Abertura, consulta e gestão de chamados de suporte")
public class TicketController {

    private final TicketService ticketService;

    @DeleteMapping("/{id}")
    public ResponseEntity<TicketResponseDto> deleteTicket(@PathVariable Long id) {
        return ResponseEntity.ok().body(ticketService.deleteTicket(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponseDto> updateTicket(
            @PathVariable Long id,
            @RequestBody @Valid TicketUpdateDto updateDto){
        return ResponseEntity.ok().body(ticketService.updateTicket(id, updateDto));
    }

    @PatchMapping("/{id}/technician={technician}")
    public ResponseEntity<TicketResponseDto> assignTicket(
            @PathVariable Long id,
            @PathVariable("technician") Long technician ){
        return ResponseEntity.ok().body(ticketService.assignTicket(id, technician));
    }

    @PatchMapping("/{id}/status={status}")
    public ResponseEntity<TicketResponseDto> updateTicketStatus(
            @PathVariable Long id,
            @PathVariable("status") Status status){
        return ResponseEntity.ok().body(ticketService.updateTicketStatus(id, status));
    }

    @GetMapping()
    public ResponseEntity<List<TicketResponseDto>> findAllTickets() {
        return ResponseEntity.ok().body(ticketService.findAllTickets());
    }

    @GetMapping("/filter")
    public ResponseEntity<List<TicketResponseDto>> findAllTicketsStatus(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "priority", required = false) String priority
            ) {
        return ResponseEntity.ok().body(ticketService.TicketFilter(status, category, priority));
    }

    @GetMapping("/search")
    public ResponseEntity<List<TicketResponseDto>> searchTickets(
            @RequestParam(name = "word")
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
        return ResponseEntity.ok().body(ticketService.findTicketByCustomerId(id));
    }

    @PostMapping
    public ResponseEntity<TicketResponseDto> createTicket(
            @RequestBody @Valid TicketCreateDto dto,
            @RequestHeader(name = "Customer-Id") Long id) {
        TicketResponseDto responseDto = ticketService.createTicket(dto, id);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(responseDto.getId())
                .toUri();

        return ResponseEntity.created(location).body(responseDto);
    }

}
