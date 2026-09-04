package com.helpdesk.ticket_service.service;

import com.helpdesk.ticket_service.Enums.Category;
import com.helpdesk.ticket_service.Enums.Priority;
import com.helpdesk.ticket_service.Enums.Status;
import com.helpdesk.ticket_service.client.UserServiceClient;
import com.helpdesk.ticket_service.dto.TicketCreateDto;
import com.helpdesk.ticket_service.dto.TicketResponseDto;
import com.helpdesk.ticket_service.dto.TicketUpdateDto;
import com.helpdesk.ticket_service.messaging.publisher.TicketEventPublisher;
import com.helpdesk.ticket_service.model.Ticket;
import com.helpdesk.ticket_service.ticketRepository.TicketRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários das regras de negócio do ticket-service: status inicial
 * sempre OPEN, transições de status controladas, validação cruzada com o
 * user-service, e publicação de evento pra cada mudança relevante.
 *
 * Fora de um contexto Spring/@Transactional real,
 * TransactionSynchronizationManager.isActualTransactionActive() é sempre
 * false, então o TicketService publica os eventos direto (branch "else"),
 * o que permite verificar ticketEventPublisher.publish(...) normalmente
 * com Mockito puro, sem precisar de @SpringBootTest.
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private TicketEventPublisher ticketEventPublisher;
    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private TicketService ticketService;

    private TicketCreateDto createDto;

    @BeforeEach
    void setUp() {
        createDto = new TicketCreateDto();
        createDto.setTitle("Impressora não liga");
        createDto.setDescription("A impressora do 3º andar não liga");
        createDto.setPriority(Priority.HIGH);
        createDto.setCategory(Category.HARDWARE);
    }

    @Test
    void createTicket_deveNascerComStatusOpenEPublicarEvento() {
        when(userServiceClient.exists(10L)).thenReturn(true);
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        TicketResponseDto response = ticketService.createTicket(createDto, 10L);

        assertThat(response.getStatus()).isEqualTo(Status.OPEN);
        assertThat(response.getCustomerId()).isEqualTo(10L);
        verify(ticketEventPublisher).publish(eq("ticket.created"), any());
    }

    @Test
    void createTicket_deveRecusarQuandoCustomerIdInvalido() {
        when(userServiceClient.exists(999L)).thenReturn(false);

        assertThatThrownBy(() -> ticketService.createTicket(createDto, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("customerId inválido");

        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(ticketEventPublisher, never()).publish(anyString(), any());
    }

    @Test
    void updateTicketStatus_devePermitirTransicaoValidaEPublicarEvento() {
        Ticket existing = Ticket.builder().id(1L).title("t").description("d")
                .priority(Priority.LOW).category(Category.SOFTWARE)
                .customerId(10L).status(Status.OPEN).build();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        ticketService.updateTicketStatus(1L, Status.IN_PROGRESS);

        assertThat(existing.getStatus()).isEqualTo(Status.IN_PROGRESS);
        verify(ticketEventPublisher).publish(eq("ticket.changed"), any());
    }

    @Test
    void updateTicketStatus_ticketFechadoNaoPodeMudarStatus() {
        Ticket closed = Ticket.builder().id(1L).title("t").description("d")
                .priority(Priority.LOW).category(Category.SOFTWARE)
                .customerId(10L).status(Status.CLOSED).build();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(closed));

        assertThatThrownBy(() -> ticketService.updateTicketStatus(1L, Status.OPEN))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("fechado");

        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(ticketEventPublisher, never()).publish(anyString(), any());
    }

    @Test
    void updateTicketStatus_resolvidoSoPodeIrParaClosed() {
        Ticket resolved = Ticket.builder().id(1L).title("t").description("d")
                .priority(Priority.LOW).category(Category.SOFTWARE)
                .customerId(10L).status(Status.RESOLVED).build();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(resolved));

        assertThatThrownBy(() -> ticketService.updateTicketStatus(1L, Status.IN_PROGRESS))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("resolvido");

        // a transição válida (RESOLVED -> CLOSED) deve funcionar sem exceção
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        ticketService.updateTicketStatus(1L, Status.CLOSED);
        assertThat(resolved.getStatus()).isEqualTo(Status.CLOSED);
    }

    @Test
    void assignTicket_deveAtribuirTecnicoValidoEPublicarEvento() {
        Ticket existing = Ticket.builder().id(1L).title("t").description("d")
                .priority(Priority.LOW).category(Category.SOFTWARE)
                .customerId(10L).status(Status.OPEN).build();
        when(userServiceClient.exists(20L)).thenReturn(true);
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        ticketService.assignTicket(1L, 20L);

        assertThat(existing.getTechnicianId()).isEqualTo(20L);
        verify(ticketEventPublisher).publish(eq("ticket.assigned"), any());
    }

    @Test
    void assignTicket_deveRecusarTecnicoInvalido() {
        when(userServiceClient.exists(999L)).thenReturn(false);

        assertThatThrownBy(() -> ticketService.assignTicket(1L, 999L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("technicianId inválido");

        verify(ticketRepository, never()).findById(any());
    }

    @Test
    void updateTicket_naoDeveEstourarNullPointerEmCamposNaoInformados() {
        Ticket existing = Ticket.builder().id(1L).title("t").description("original")
                .priority(Priority.LOW).category(Category.SOFTWARE)
                .customerId(10L).status(Status.OPEN).build();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        // status/category/description nulos: update parcial não pode quebrar
        TicketUpdateDto dto = new TicketUpdateDto();

        TicketResponseDto response = ticketService.updateTicket(1L, dto);

        assertThat(response.getDescription()).isEqualTo("original");
        assertThat(response.getStatus()).isEqualTo(Status.OPEN);
    }

    @Test
    void deleteTicket_deveExcluirEPublicarEventoDeExclusao() {
        Ticket existing = Ticket.builder().id(1L).title("t").description("d")
                .priority(Priority.LOW).category(Category.SOFTWARE)
                .customerId(10L).status(Status.OPEN).build();
        when(ticketRepository.findById(1L)).thenReturn(Optional.of(existing));

        ticketService.deleteTicket(1L);

        verify(ticketRepository).delete(existing);
        ArgumentCaptor<String> routingKey = ArgumentCaptor.forClass(String.class);
        verify(ticketEventPublisher).publish(routingKey.capture(), any());
        assertThat(routingKey.getValue()).isEqualTo("ticket.deleted");
    }

    @Test
    void findById_deveLancarNotFoundQuandoChamadoNaoExiste() {
        when(ticketRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.findById(404L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
