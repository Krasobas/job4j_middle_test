package ru.job4j.r.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class XmlToJsonTransformer {

    private final PhotoKeyCache photoKeyCache;
    private final XmlMapper xmlMapper = new XmlMapper();
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public String xmlToJson(String xml) {
        try {
            JsonNode node = xmlMapper.readTree(xml);
            addPhotoUrls(node);
            return jsonMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException("XML to JSON failed", e);
        }
    }

    private void addPhotoUrls(JsonNode node) {
        if (node.has("student")) {
            JsonNode students = node.get("student");
            if (students.isArray()) {
                students.forEach(this::replacePhotoKey);
            } else {
                replacePhotoKey(students);
            }
        }
    }

    private void replacePhotoKey(JsonNode student) {
        if (student instanceof ObjectNode obj && obj.has("photoKey")) {
            String rbNumber = obj.get("recordBookNumber").asText();
            String photoKey = obj.get("photoKey").asText();
            photoKeyCache.put(rbNumber, photoKey);
            obj.put("photoUrl", "/api/students/" + rbNumber + "/photo");
            obj.remove("photoKey");
        }
    }
}