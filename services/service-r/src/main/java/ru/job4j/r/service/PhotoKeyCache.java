package ru.job4j.r.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class PhotoKeyCache {

    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public void put(String recordBookNumber, String photoKey) {
        cache.put(recordBookNumber, photoKey);
    }

    public String get(String recordBookNumber) {
        return cache.get(recordBookNumber);
    }
}
