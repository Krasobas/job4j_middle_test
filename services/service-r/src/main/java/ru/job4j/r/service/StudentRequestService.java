package ru.job4j.r.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Log4j2
@Service
@RequiredArgsConstructor
public class StudentRequestService {

    private static final String REQUEST_TOPIC = "student-request";

    private final ReplyingKafkaTemplate<String, String, String> replyingTemplate;

    /**
     * Отправляет запрос в Kafka и ждёт ответа.
     *
     * @param payload "ALL" для всех студентов,
     *                или recordBookNumber для одного
     * @return XML-строка с ответом от Service S
     */
    public String sendAndReceive(String payload) {
        log.info("Sending request: {}", payload);

        var record = new ProducerRecord<String, String>(REQUEST_TOPIC, payload);
        record.headers().add(KafkaHeaders.REPLY_TOPIC,
            "student-response".getBytes(StandardCharsets.UTF_8));
        try {
            RequestReplyFuture<String, String, String> future =
                replyingTemplate.sendAndReceive(record);
            ConsumerRecord<String, String> response = future.get();
            log.info("Got response from Service S");
            return response.value();
        } catch (Exception e) {
            throw new RuntimeException("Service S did not respond", e);
        }
    }
}
