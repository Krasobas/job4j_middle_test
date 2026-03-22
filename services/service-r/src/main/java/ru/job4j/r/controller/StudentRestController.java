package ru.job4j.r.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.camel.ProducerTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentRestController {

    private final ProducerTemplate producerTemplate;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all students",
        description = "Fetches all student records via Kafka from Service S")
    public ResponseEntity<String> getAllStudents() {
        String json = producerTemplate.requestBody(
            "direct:getAllStudents", "ALL", String.class);
        return ResponseEntity.ok(json);
    }

    @GetMapping(value = "/{recordBookNumber}",
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get a student by record book number",
        description = "Fetches a student record via Kafka from Service S")
    public ResponseEntity<String> getStudent(
        @PathVariable String recordBookNumber) {
        String json = producerTemplate.requestBody(
            "direct:getStudent", recordBookNumber, String.class);
        return ResponseEntity.ok(json);
    }

    @GetMapping(value = "/{recordBookNumber}/photo",
        produces = MediaType.IMAGE_JPEG_VALUE)
    @Operation(summary = "Get a student photo by record book number",
        description = "Fetches a student photo via HTTP from Service S")
    public ResponseEntity<byte[]> getPhoto(
        @PathVariable String recordBookNumber) {
        byte[] photo = (byte[]) producerTemplate.requestBody(
            "direct:getPhoto", recordBookNumber);
        if (photo == null || photo.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(photo);
    }
}