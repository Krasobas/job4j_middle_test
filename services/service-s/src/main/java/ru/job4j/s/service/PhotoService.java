package ru.job4j.s.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@RequiredArgsConstructor
public class PhotoService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    /**
     * Download a photo by its key.
     * Returns byte array or empty array if not found.
     */
    public byte[] getPhoto(String photoKey) {
        try (InputStream stream = minioClient.getObject(
            GetObjectArgs.builder()
                .bucket(bucket)
                .object(photoKey)
                .build())) {
            return stream.readAllBytes();
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                log.warn("Photo not found: {}", photoKey);
                return new byte[0];
            }
            throw new RuntimeException("MinIO error", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get photo: " + photoKey, e);
        }
    }

    /**
     * Upload a photo.
     */
    public void uploadPhoto(String photoKey, InputStream stream,
                            long size, String contentType) {
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(photoKey)
                    .stream(stream, size, -1)
                    .contentType(contentType)
                    .build());
            log.info("Uploaded photo: {}", photoKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload photo: " + photoKey, e);
        }
    }

    /**
     * Delete a photo.
     */
    public void deletePhoto(String photoKey) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucket)
                    .object(photoKey)
                    .build());
            log.info("Deleted photo: {}", photoKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete photo: " + photoKey, e);
        }
    }

    /**
     * Check if a photo exists.
     */
    public boolean exists(String photoKey) {
        try {
            minioClient.statObject(
                StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(photoKey)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if ("NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw new RuntimeException("MinIO error", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check photo: " + photoKey, e);
        }
    }

    /**
     * Generate a temporary download URL.
     * The URL expires after the specified duration.
     */
    public String getDownloadUrl(String key, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .method(Method.GET)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    /**
     * Generate a temporary upload URL.
     * Client can PUT a file directly to MinIO.
     */
    public String getUploadUrl(String key, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .method(Method.PUT)
                    .expiry(expiryMinutes, TimeUnit.MINUTES)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }
}
