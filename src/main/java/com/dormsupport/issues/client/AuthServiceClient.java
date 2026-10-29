package com.dormsupport.issues.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.UUID;

@Service
public class AuthServiceClient {

    private final RestClient restClient;

    public AuthServiceClient(
            @Value("${services.auth-uri:http://auth-service:8080}") String authServiceUri) {
        this.restClient = RestClient.builder()
                .baseUrl(authServiceUri)
                .build();
    }

    public AuthUserProfile getUserProfile(UUID userId) {
        try {
            return restClient.get()
                    .uri("/internal/v1/users/{userId}", userId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        if (response.getStatusCode().value() == 404) {
                            throw new UserProfileNotFoundException();
                        }
                    })
                    .body(AuthUserProfile.class);
        } catch (UserProfileNotFoundException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new AuthServiceUnavailableException(exception);
        }
    }
}
