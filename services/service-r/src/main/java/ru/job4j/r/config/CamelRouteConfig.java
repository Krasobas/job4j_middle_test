package ru.job4j.r.config;

import lombok.RequiredArgsConstructor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;
import ru.job4j.r.client.ServiceSClient;
import ru.job4j.r.service.PhotoKeyCache;
import ru.job4j.r.service.StudentRequestService;
import ru.job4j.r.service.XmlPhotoKeyExtractor;
import ru.job4j.r.service.XmlToJsonTransformer;

@Component
@RequiredArgsConstructor
public class CamelRouteConfig extends RouteBuilder {

    private final ServiceSClient serviceSClient;
    private final PhotoKeyCache photoKeyCache;
    private final StudentRequestService studentRequestService;
    private final XmlToJsonTransformer xmlToJsonTransformer;
    private final XmlPhotoKeyExtractor xmlPhotoKeyExtractor;


    @Override
    public void configure() {

        from("direct:getAllStudents")
            .routeId("get-all-students")
            .log("Sending request to Kafka for all students")
            .bean(studentRequestService, "sendAndReceive")
            .log("Got XML response, transforming to JSON")
            .bean(xmlToJsonTransformer, "xmlToJson")
            .log("JSON ready, returning to controller");

        from("direct:getStudent")
            .routeId("get-one-student")
            .log("Sending request to Kafka for student: ${body}")
            .bean(studentRequestService, "sendAndReceive")
            .log("Got XML response, transforming to JSON")
            .bean(xmlToJsonTransformer, "xmlToJson")
            .log("JSON ready, returning to controller");

        from("direct:getPhoto")
            .routeId("get-one-student-photo")
            .setHeader("recordBookNumber", body())

            .log("Checking cache for: ${header.recordBookNumber}")
            .bean(photoKeyCache, "get(${header.recordBookNumber})")

            .choice()
                .when(body().isNull())
                    .log("Cache miss, fetching from Kafka")
                    .setBody(header("recordBookNumber"))
                    .bean(studentRequestService, "sendAndReceive")
                    .bean(xmlPhotoKeyExtractor, "extract")
            .end()

            .choice()
                .when(body().isNull())
                    .log("Photo not found")
                    .setHeader("CamelHttpResponseCode", constant(404))
                    .setBody(constant(new byte[0]))
                .otherwise()
                    .log("Fetching photo: ${body}")
                    .bean(serviceSClient, "getPhoto(${body})")
            .end();
    }
}