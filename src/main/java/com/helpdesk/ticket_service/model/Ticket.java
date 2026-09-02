package com.helpdesk.ticket_service.model;

import com.helpdesk.ticket_service.Enums.*;
import com.helpdesk.ticket_service.dto.TicketCreateDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {
    
    //Data Definition Language
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private Status status = Status.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "technician_id")
    private Long technicianId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "update_at", nullable = false)
    private LocalDateTime updatedAt;

    public Ticket(TicketCreateDto dto) {
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.priority = dto.getPriority();
        this.status = Status.OPEN;
        this.category = dto.getCategory();
        this.customerId = dto.getCustomerId();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {

        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }
}
