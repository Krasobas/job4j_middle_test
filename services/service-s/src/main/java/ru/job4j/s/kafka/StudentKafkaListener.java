package ru.job4j.s.kafka;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import ru.job4j.s.service.StudentService;
import ru.job4j.s.soap.gen.*;

import java.io.StringWriter;

@Log4j2
@Component
@RequiredArgsConstructor
public class StudentKafkaListener {

    private static final String RESPONSE_TOPIC = "student-response";

    private final StudentService studentService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "student-request", groupId = "service-s")
    public void handle(@Payload String payload,
                       @Header(KafkaHeaders.CORRELATION_ID) byte[] correlationId) {
        log.info("Received request: {}", payload);
        try {
            String xmlResponse;
            if ("ALL".equals(payload)) {
                xmlResponse = marshal(studentService.getAllStudentsAsXml(),
                    GetAllStudentsResponse.class);
            } else {
                xmlResponse = marshal(studentService.getStudentAsXml(payload),
                    GetStudentResponse.class);
            }
            var message = MessageBuilder.withPayload(xmlResponse)
                .setHeader(KafkaHeaders.TOPIC, RESPONSE_TOPIC)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();
            kafkaTemplate.send(message);
            log.info("Response sent to {}", RESPONSE_TOPIC);
        } catch (Exception e) {
            log.error("Failed to process request: {}", payload, e);
        }
    }

    private <T> String marshal(T object, Class<T> clazz) throws Exception {
        var context = JAXBContext.newInstance(clazz);
        var marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        var writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();
    }
}