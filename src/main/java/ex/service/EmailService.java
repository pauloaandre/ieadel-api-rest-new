package ex.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailService {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${RESEND_API_KEY:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // Métodos de envio de e-mail podem ser adicionados aqui no futuro
}
