package ru.job4j.s.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.job4j.s.exception.StudentNotFoundException;
import ru.job4j.s.model.Student;
import ru.job4j.s.repository.StudentRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository repository;

    @InjectMocks
    private StudentService studentService;

    @Test
    void findAllReturnsStudentList() {
        var student = new Student(1L, "RB-001", "CS", "Doe", "John", "M", "john@test.com", null);
        when(repository.findAll()).thenReturn(List.of(student));

        List<Student> result = studentService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRecordBookNumber()).isEqualTo("RB-001");
    }

    @Test
    void findByRecordBookNumberReturnsStudent() {
        var student = new Student(1L, "RB-001", "CS", "Doe", "John", "M", "john@test.com", null);
        when(repository.findByRecordBookNumber("RB-001")).thenReturn(Optional.of(student));

        Student result = studentService.findByRecordBookNumber("RB-001");

        assertThat(result.getLastName()).isEqualTo("Doe");
    }

    @Test
    void findByRecordBookNumberThrowsWhenNotFound() {
        when(repository.findByRecordBookNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findByRecordBookNumber("UNKNOWN"))
            .isInstanceOf(StudentNotFoundException.class)
            .hasMessageContaining("UNKNOWN");
    }

    @Test
    void getAllStudentsAsXmlReturnsPopulatedResponse() {
        var student = new Student(1L, "RB-001", "CS", "Doe", "John", "M", "john@test.com", "photo.jpg");
        when(repository.findAll()).thenReturn(List.of(student));

        var response = studentService.getAllStudentsAsXml();

        assertThat(response).isNotNull();
        assertThat(response.getStudent()).hasSize(1);
        assertThat(response.getStudent().get(0).getRecordBookNumber()).isEqualTo("RB-001");
    }

    @Test
    void getStudentAsXmlReturnsStudentResponse() {
        var student = new Student(1L, "RB-001", "CS", "Doe", "John", "M", "john@test.com", "photo.jpg");
        when(repository.findByRecordBookNumber("RB-001")).thenReturn(Optional.of(student));

        var response = studentService.getStudentAsXml("RB-001");

        assertThat(response).isNotNull();
        assertThat(response.getStudent().getLastName()).isEqualTo("Doe");
    }
}