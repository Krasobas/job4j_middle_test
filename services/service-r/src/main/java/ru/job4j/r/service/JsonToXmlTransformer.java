package ru.job4j.r.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.stereotype.Service;

@Service
public class JsonToXmlTransformer {

    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();

    public String jsonToXml(String json) {
        try {
            JsonNode node = jsonMapper.readTree(json);
            return xmlMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(node);
        } catch (Exception e) {
            throw new RuntimeException("JSON to XML failed", e);
        }
    }
}
