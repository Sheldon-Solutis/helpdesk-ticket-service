package com.helpdesk.ticket_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

// Chama o user-service diretamente (fora do gateway) pra confirmar que
// customerId/technicianId existem antes de criar ou atribuir um chamado.
// Requisito do desafio original que ainda não estava implementado.
@Slf4j
@Component
public class UserServiceClient {

    private final RestClient restClient;

    public UserServiceClient(@Value("${services.user.url}") String userServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(userServiceUrl).build();
    }

    public record UserSummary(Long id, String name, String role, boolean active) {}

    public boolean exists(Long userId) {
        try {
            UserSummary user = restClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .body(UserSummary.class);
            return user != null && user.active();
        } catch (HttpClientErrorException.NotFound notFound) {
            return false;
        } catch (ResourceAccessException unreachable) {
            log.error("user-service indisponível ao validar o usuário {}: {}", userId, unreachable.getMessage());
            // Tratamento gracioso: não deixamos o RestClientException cru
            // subir; devolvemos um 503 claro em vez de travar em 500.
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível validar o usuário: user-service indisponível");
        }
    }
}
