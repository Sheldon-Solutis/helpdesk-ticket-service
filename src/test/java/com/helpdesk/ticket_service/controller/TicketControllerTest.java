package com.helpdesk.ticket_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.helpdesk.ticket_service.Enums.Category;
import com.helpdesk.ticket_service.Enums.Priority;
import com.helpdesk.ticket_service.Enums.Status;
import com.helpdesk.ticket_service.dto.TicketCreateDto;
import com.helpdesk.ticket_service.dto.TicketResponseDto;
import com.helpdesk.ticket_service.service.TicketService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TicketService ticketService;

    private TicketResponseDto sampleResponse() {
        return new TicketResponseDto(1L, "Rede lenta", "Rede lenta no setor B",
                Category.NETWORK, Priority.MEDIUM, Status.OPEN, 5L, null, null, null);
    }

    @Test
    void createTicket_deveRetornar400ComPrioridadeInexistenteNoEnum() throws Exception {
        String corpoComEnumInvalido = """
            {"title":"t","description":"d","priority":"URGENTE","category":"SOFTWARE"}
            """;

        mockMvc.perform(post("/api/tickets")
                        .header("Customer-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoComEnumInvalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTicket_deveRetornar201QuandoCustomerIdHeaderPresente() throws Exception {
        TicketCreateDto request = new TicketCreateDto();
        request.setTitle("Rede lenta");
        request.setDescription("Rede lenta no setor B");
        request.setPriority(Priority.MEDIUM);
        request.setCategory(Category.NETWORK);

        when(ticketService.createTicket(any(TicketCreateDto.class), eq(5L))).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/tickets")
                        .header("Customer-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void createTicket_deveRetornar400SemHeaderCustomerId() throws Exception {
        TicketCreateDto request = new TicketCreateDto();
        request.setTitle("Rede lenta");
        request.setDescription("Rede lenta no setor B");
        request.setPriority(Priority.MEDIUM);
        request.setCategory(Category.NETWORK);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTicket_deveRetornar400ComCorpoInvalido() throws Exception {
        TicketCreateDto invalid = new TicketCreateDto();
        // título e descrição em branco: violam @NotBlank

        mockMvc.perform(post("/api/tickets")
                        .header("Customer-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTicket_deveRetornar400QuandoCustomerIdInvalido() throws Exception {
        TicketCreateDto request = new TicketCreateDto();
        request.setTitle("Rede lenta");
        request.setDescription("Rede lenta no setor B");
        request.setPriority(Priority.MEDIUM);
        request.setCategory(Category.NETWORK);

        when(ticketService.createTicket(any(TicketCreateDto.class), eq(999L)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId inválido"));

        mockMvc.perform(post("/api/tickets")
                        .header("Customer-Id", "999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_deveRetornar404QuandoNaoEncontrado() throws Exception {
        when(ticketService.findById(eq(123L)))
                .thenThrow(new EntityNotFoundException("Ticket Not Found With Id: 123"));

        mockMvc.perform(get("/api/tickets/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void findAllTickets_deveRetornar200ComLista() throws Exception {
        when(ticketService.findAllTickets()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Rede lenta"));
    }

    @Test
    void updateTicketStatus_devePatchNaRotaComStatusEmbutidoNoPath() throws Exception {
        TicketResponseDto updated = new TicketResponseDto(1L, "Rede lenta", "Rede lenta no setor B",
                Category.NETWORK, Priority.MEDIUM, Status.IN_PROGRESS, 5L, null, null, null);

        when(ticketService.updateTicketStatus(1L, Status.IN_PROGRESS)).thenReturn(updated);

        mockMvc.perform(patch("/api/tickets/1/status=IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void assignTicket_devePatchNaRotaComTecnicoEmbutidoNoPath() throws Exception {
        TicketResponseDto updated = new TicketResponseDto(1L, "Rede lenta", "Rede lenta no setor B",
                Category.NETWORK, Priority.MEDIUM, Status.OPEN, 5L, 20L, null, null);

        when(ticketService.assignTicket(1L, 20L)).thenReturn(updated);

        mockMvc.perform(patch("/api/tickets/1/technician=20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.technicianId").value(20));
    }

    @Test
    void searchTickets_deveUsarQueryParamWord() throws Exception {
        when(ticketService.searchTickets("lenta")).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/tickets/search").param("word", "lenta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Rede lenta"));
    }
}
