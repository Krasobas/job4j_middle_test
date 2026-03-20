package ru.job4j.s.config.minio;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MinioHealthIndicator implements HealthIndicator {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public Health health() {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build());
            if (exists) {
                return Health.up()
                    .withDetail("bucket", bucket)
                    .build();
            }
            return Health.down()
                .withDetail("bucket", bucket)
                .withDetail("error", "bucket does not exist")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("bucket", bucket)
                .withException(e)
                .build();
        }
    }
}
