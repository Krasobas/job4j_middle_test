package ru.job4j.r.soap;


import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import lombok.RequiredArgsConstructor;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.job4j.r.service.StudentRequestService;
import ru.job4j.r.soap.gen.*;

import java.io.StringReader;

@Endpoint
@RequiredArgsConstructor
public class StudentSoapEndpoint {

    private static final String NAMESPACE = "http://job4j.ru/students";

    private final StudentRequestService requestService;

    @PayloadRoot(namespace = NAMESPACE, localPart = "getAllStudentsRequest")
    @ResponsePayload
    public GetAllStudentsResponse getAll(
        @RequestPayload GetAllStudentsRequest request) {
        String xml = requestService.sendAndReceive("ALL");
        return unmarshal(xml, GetAllStudentsResponse.class);
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "getStudentRequest")
    @ResponsePayload
    public GetStudentResponse getOne(
        @RequestPayload GetStudentRequest request) {
        String xml = requestService.sendAndReceive(request.getRecordBookNumber());
        return unmarshal(xml, GetStudentResponse.class);
    }

    private <T> T unmarshal(String xml, Class<T> clazz) {
        try {
            var context = JAXBContext.newInstance(clazz);
            var unmarshaller = context.createUnmarshaller();
            return clazz.cast(unmarshaller.unmarshal(
                new StringReader(xml)));
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to unmarshal XML", e);
        }
    }
}
