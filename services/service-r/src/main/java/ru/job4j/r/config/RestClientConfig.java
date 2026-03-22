package ru.job4j.r.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import ru.job4j.r.client.ServiceSClient;

@Configuration
public class RestClientConfig {

    @Bean
    public ServiceSClient serviceSClient(
        @Value("${service-s.url}") String baseUrl) {
        RestClient restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();
        return HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build()
            .createClient(ServiceSClient.class);
    }
}