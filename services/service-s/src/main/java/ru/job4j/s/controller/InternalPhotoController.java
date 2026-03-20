package ru.job4j.s.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.job4j.s.service.PhotoService;

@RestController
@RequestMapping("/internal/photos")
public class InternalPhotoController {

    private final PhotoService photoService;

    public InternalPhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String key) {
        if (!photoService.exists(key)) {
            return ResponseEntity.notFound().build();
        }
        byte[] photo = photoService.getPhoto(key);
        return ResponseEntity.ok()
            .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
            .contentType(resolveMediaType(key))
            .contentLength(photo.length)
            .body(photo);
    }

    @PostMapping(value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadPhoto(
        @RequestParam("file") MultipartFile file,
        @RequestParam("key") String key) {
        try {
            photoService.uploadPhoto(
                key,
                file.getInputStream(),
                file.getSize(),
                file.getContentType());
            return ResponseEntity.ok("Uploaded: " + key);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("Upload failed: " + e.getMessage());
        }
    }

    private MediaType resolveMediaType(String key) {
        if (key.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (key.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.IMAGE_JPEG;
    }
}
