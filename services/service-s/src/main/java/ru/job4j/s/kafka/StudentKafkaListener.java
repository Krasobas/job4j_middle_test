package ru.job4j.s.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.job4j.s.service.StudentService;
import ru.job4j.s.soap.JaxbMarshaller;
import java.nio.charset.StandardCharsets;

@Log4j2
@Component
@RequiredArgsConstructor
public class StudentKafkaListener {


    private final StudentService studentService;
    private final JaxbMarshaller marshaller;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "student-request", groupId = "service-s")
    public void handle(
        @Payload String payload,
        @Header(KafkaHeaders.REPLY_TOPIC) byte[] replyTopic,
        @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {

        log.info("Received request: {}", payload);

        Object xmlObject;
        if ("ALL".equals(payload)) {
            xmlObject = studentService.getAllStudentsAsXml();
        } else {
            xmlObject = studentService.getStudentAsXml(payload);
        }

        String xmlResponse = marshaller.marshal(xmlObject);

        var record = new ProducerRecord<String, String>(
            new String(replyTopic, StandardCharsets.UTF_8),
            xmlResponse);

        record.headers().add(KafkaHeaders.CORRELATION_ID, correlationId);

        kafkaTemplate.send(record);
        log.info("Response sent");
    }
}