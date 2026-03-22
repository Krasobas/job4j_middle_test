package ru.job4j.r.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface ServiceSClient {

    @GetExchange("/internal/photos/{key}")
    byte[] getPhoto(@PathVariable String key);
}
