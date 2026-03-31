package ru.job4j.r.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class XmlToJsonTransformerTest {

    private final PhotoKeyCache photoKeyCache = new PhotoKeyCache();
    private final XmlToJsonTransformer transformer = new XmlToJsonTransformer(photoKeyCache);

    @Test
    void transformsStudentXmlToJson() {
        String xml = "<GetStudentResponse>"
            + "<student>"
            + "<recordBookNumber>RB-001</recordBookNumber>"
            + "<lastName>Doe</lastName>"
            + "<firstName>John</firstName>"
            + "<faculty>CS</faculty>"
            + "<photoKey>photos/student1.jpg</photoKey>"
            + "</student>"
            + "</GetStudentResponse>";

        String json = transformer.xmlToJson(xml);

        assertThat(json).contains("Doe");
        assertThat(json).contains("RB-001");
        assertThat(json).contains("photoUrl");
        assertThat(json).doesNotContain("photoKey");
    }

    @Test
    void transformsStudentWithoutPhotoKey() {
        String xml = "<GetStudentResponse>"
            + "<student>"
            + "<recordBookNumber>RB-002</recordBookNumber>"
            + "<lastName>Smith</lastName>"
            + "<firstName>Jane</firstName>"
            + "<faculty>Math</faculty>"
            + "</student>"
            + "</GetStudentResponse>";

        String json = transformer.xmlToJson(xml);

        assertThat(json).contains("Smith");
        assertThat(json).contains("RB-002");
    }

    @Test
    void cachesPhotoKeyAfterTransformation() {
        String xml = "<GetStudentResponse>"
            + "<student>"
            + "<recordBookNumber>RB-003</recordBookNumber>"
            + "<lastName>Lee</lastName>"
            + "<firstName>Anna</firstName>"
            + "<faculty>Law</faculty>"
            + "<photoKey>photos/anna.jpg</photoKey>"
            + "</student>"
            + "</GetStudentResponse>";

        transformer.xmlToJson(xml);

        assertThat(photoKeyCache.get("RB-003")).isEqualTo("photos/anna.jpg");
    }
}