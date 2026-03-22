package ru.job4j.r.service;

import org.springframework.stereotype.Component;

@Component
public class XmlPhotoKeyExtractor {

    public String extract(String xml) {
        int start = xml.indexOf("<photoKey>");
        int end = xml.indexOf("</photoKey>");
        if (start == -1 || end == -1) {
            return null;
        }
        return xml.substring(start + "<photoKey>".length(), end);
    }
}
