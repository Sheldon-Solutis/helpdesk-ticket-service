package com.helpdesk.ticket_service.dto;

import com.helpdesk.ticket_service.Enums.*;
import com.helpdesk.ticket_service.model.Ticket;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponseDto {
    private Long id;
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private Status status;
    private Long customerId;
    private Long technicianId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TicketResponseDto(Ticket t) {
        this.id = t.getId();
        this.title = t.getTitle();
        this.description = t.getDescription();
        this.category = t.getCategory();
        this.priority = t.getPriority();
        this.status = t.getStatus();
        this.customerId = t.getCustomerId();
        this.technicianId = t.getTechnicianId();
        this.createdAt = t.getCreatedAt();
        this.updatedAt = t.getUpdatedAt();
    }
}
