package ru.job4j.s.exception;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(String recordBookNumber) {
        super("Student not found: " + recordBookNumber);
    }
}
