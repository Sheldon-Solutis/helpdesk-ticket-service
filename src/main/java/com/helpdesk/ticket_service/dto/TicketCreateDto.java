package com.helpdesk.ticket_service.dto;

import com.helpdesk.ticket_service.Enums.Category;
import com.helpdesk.ticket_service.Enums.Priority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCreateDto {

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotNull
    private Category category;

    @NotNull
    private Priority priority;

    // Preenchido pelo service a partir do header Customer-Id, não do corpo
    // da requisição — mas fica aqui pro Ticket(TicketCreateDto) enxergar.
    private Long customerId;
}
