package ru.job4j.s.soap;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.springframework.stereotype.Component;
import ru.job4j.s.soap.gen.*;

import java.io.StringWriter;

@Component
public class JaxbMarshaller {

    private final JAXBContext context;

    public JaxbMarshaller() throws JAXBException {
        this.context = JAXBContext.newInstance(
            GetAllStudentsResponse.class,
            GetStudentResponse.class
        );
    }

    public String marshal(Object object) {
        try {
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to marshal object", e);
        }
    }
}