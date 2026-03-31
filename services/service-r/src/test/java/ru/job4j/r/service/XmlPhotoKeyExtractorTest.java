package ru.job4j.r.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XmlPhotoKeyExtractorTest {

    private final XmlPhotoKeyExtractor extractor = new XmlPhotoKeyExtractor();

    @Test
    void extractsPhotoKeyFromXml() {
        String xml = "<student><photoKey>photos/student1.jpg</photoKey></student>";

        String key = extractor.extract(xml);

        assertThat(key).isEqualTo("photos/student1.jpg");
    }

    @Test
    void returnsNullWhenPhotoKeyAbsent() {
        String xml = "<student><firstName>John</firstName></student>";

        String key = extractor.extract(xml);

        assertThat(key).isNull();
    }
}