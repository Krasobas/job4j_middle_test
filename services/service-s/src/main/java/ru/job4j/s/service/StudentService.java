package ru.job4j.s.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.job4j.s.exception.StudentNotFoundException;
import ru.job4j.s.model.Student;
import ru.job4j.s.repository.StudentRepository;
import ru.job4j.s.soap.gen.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repository;


    public List<Student> findAll() {
        return repository.findAll();
    }

    public Student findByRecordBookNumber(String number) {
        return repository.findByRecordBookNumber(number)
            .orElseThrow(() -> new StudentNotFoundException(number));
    }


    public GetAllStudentsResponse getAllStudentsAsXml() {
        var response = new GetAllStudentsResponse();
        findAll().stream()
            .map(this::toStudentType)
            .forEach(response.getStudent()::add);
        return response;
    }

    public GetStudentResponse getStudentAsXml(String recordBookNumber) {
        var response = new GetStudentResponse();
        response.setStudent(toStudentType(findByRecordBookNumber(recordBookNumber)));
        return response;
    }


    private StudentType toStudentType(Student student) {
        var type = new StudentType();
        type.setRecordBookNumber(student.getRecordBookNumber());
        type.setFaculty(student.getFaculty());
        type.setLastName(student.getLastName());
        type.setFirstName(student.getFirstName());
        type.setMiddleName(student.getMiddleName());
        type.setPhotoKey(student.getPhotoKey());
        return type;
    }
}
