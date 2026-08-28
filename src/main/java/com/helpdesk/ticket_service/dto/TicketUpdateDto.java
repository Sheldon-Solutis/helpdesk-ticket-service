package com.helpdesk.ticket_service.dto;


import com.helpdesk.ticket_service.Enums.Category;
import com.helpdesk.ticket_service.Enums.Priority;
import com.helpdesk.ticket_service.Enums.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketUpdateDto {
    private Priority priority;
    private Category category;
    private String description;
    private Status status;
}
