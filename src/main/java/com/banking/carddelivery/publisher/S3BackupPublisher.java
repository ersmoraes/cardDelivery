package com.banking.carddelivery.publisher;

import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.format.DateTimeFormatter;

@Component
public class S3BackupPublisher implements BackupPublisher {

    private static final Logger log = LoggerFactory.getLogger(S3BackupPublisher.class);
    private static final DateTimeFormatter PATH_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;
    private final String bucket;

    public S3BackupPublisher(S3Client s3Client,
                             ObjectMapper objectMapper,
                             @Value("${aws.s3.bucket}") String bucket) {
        this.s3Client = s3Client;
        this.objectMapper = objectMapper;
        this.bucket = bucket;
    }

    @Override
    public void publicar(DeliveryEvaluatedEvent event) {
        String key = buildKey(event);
        try {
            byte[] content = objectMapper.writeValueAsBytes(event);

            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(content));
            log.info("Backup S3 publicado: s3://{}/{}", bucket, key);
        } catch (Exception ex) {
            log.error("Falha ao publicar backup S3 para eventId={} key={}: {}",
                    event.getEventId(), key, ex.getMessage());
            throw new RuntimeException("Falha ao publicar backup em S3", ex);
        }
    }

    private String buildKey(DeliveryEvaluatedEvent event) {
        String datePath = event.getOcorridoEm() != null
                ? event.getOcorridoEm().format(PATH_DATE_FORMAT)
                : "unknown";
        return datePath + "/" + event.getEventId() + ".json";
    }
}