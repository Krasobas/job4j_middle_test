package ru.job4j.s.config.soap;

import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.job4j.s.exception.StudentNotFoundException;

import java.util.Properties;

@Configuration
public class SoapFaultConfig {

    @Bean
    public SoapFaultMappingExceptionResolver exceptionResolver() {
        var resolver = new SoapFaultMappingExceptionResolver();
        var defaultFault = new SoapFaultDefinition();
        defaultFault.setFaultCode(SoapFaultDefinition.SERVER);
        resolver.setDefaultFault(defaultFault);
        var props = new Properties();
        props.setProperty(
            StudentNotFoundException.class.getName(),
            SoapFaultDefinition.SERVER.toString()
        );
        resolver.setExceptionMappings(props);
        resolver.setOrder(1);
        return resolver;
    }
}