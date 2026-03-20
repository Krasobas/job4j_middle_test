package ru.job4j.s.soap;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import ru.job4j.s.service.StudentService;
import ru.job4j.s.soap.gen.*;

@Endpoint
public class StudentEndpoint {

    private static final String NAMESPACE = "http://job4j.ru/s";

    private final StudentService studentService;

    public StudentEndpoint(StudentService studentService) {
        this.studentService = studentService;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "getAllStudentsRequest")
    @ResponsePayload
    public GetAllStudentsResponse getAll(
        @RequestPayload GetAllStudentsRequest request) {
        return studentService.getAllStudentsAsXml();
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "getStudentRequest")
    @ResponsePayload
    public GetStudentResponse getOne(
        @RequestPayload GetStudentRequest request) {
        return studentService.getStudentAsXml(request.getRecordBookNumber());
    }
}
