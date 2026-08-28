package com.helpdesk.ticket_service.dto;

import com.helpdesk.ticket_service.Enums.Category;
import com.helpdesk.ticket_service.Enums.Priority;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketCreateDto {
    private String title;
    private String description;
    private Category category;
    private Priority priority;
    private Long customerId;
}
