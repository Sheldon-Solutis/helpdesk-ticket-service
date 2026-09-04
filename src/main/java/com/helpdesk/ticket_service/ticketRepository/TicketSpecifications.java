package com.helpdesk.ticket_service.ticketRepository;

import com.helpdesk.ticket_service.Enums.Category;
import com.helpdesk.ticket_service.Enums.Priority;
import com.helpdesk.ticket_service.Enums.Status;
import com.helpdesk.ticket_service.model.Ticket;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TicketSpecifications {

    private TicketSpecifications() {}

    public static Specification<Ticket> withFilters(Status status, Category category, Priority priority) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (category != null) predicates.add(cb.equal(root.get("category"), category));
            if (priority != null) predicates.add(cb.equal(root.get("priority"), priority));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}