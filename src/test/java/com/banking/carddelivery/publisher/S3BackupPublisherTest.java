package com.banking.carddelivery.publisher;

import com.banking.carddelivery.exception.ServicoExternoIndisponivelException;
import com.banking.carddelivery.messaging.event.DeliveryEvaluatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3BackupPublisherTest {

    @Mock private S3Client s3Client;
    @Mock private ObjectMapper objectMapper;

    private S3BackupPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new S3BackupPublisher(s3Client, objectMapper, "test-bucket");
    }

    @Test
    void publicar_success_uploadsToS3() throws Exception {
        DeliveryEvaluatedEvent event = buildEvent("abc123", LocalDateTime.of(2024, 6, 15, 10, 0));
        when(objectMapper.writeValueAsBytes(event)).thenReturn("{\"eventId\":\"abc123\"}".getBytes());

        publisher.publicar(event);

        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void publicar_success_buildsCorrectKey() throws Exception {
        DeliveryEvaluatedEvent event = buildEvent("abc123", LocalDateTime.of(2024, 6, 15, 10, 0));
        when(objectMapper.writeValueAsBytes(event)).thenReturn("{}".getBytes());

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        publisher.publicar(event);

        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        PutObjectRequest req = captor.getValue();
        assertThat(req.key()).isEqualTo("2024/06/15/abc123.json");
        assertThat(req.bucket()).isEqualTo("test-bucket");
        assertThat(req.contentType()).isEqualTo("application/json");
    }

    @Test
    void publicar_nullOcorridoEm_usesUnknownPath() throws Exception {
        DeliveryEvaluatedEvent event = buildEvent("abc123", null);
        when(objectMapper.writeValueAsBytes(event)).thenReturn("{}".getBytes());

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        publisher.publicar(event);

        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertThat(captor.getValue().key()).startsWith("unknown/");
    }

    @Test
    void publicar_s3Fails_throwsServicoExternoIndisponivelException() throws Exception {
        DeliveryEvaluatedEvent event = buildEvent("abc123", LocalDateTime.of(2024, 6, 15, 10, 0));
        when(objectMapper.writeValueAsBytes(event)).thenReturn("{}".getBytes());
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(new RuntimeException("S3 connection failed"));

        assertThatThrownBy(() -> publisher.publicar(event))
                .isInstanceOf(ServicoExternoIndisponivelException.class)
                .hasMessageContaining("backup em S3");
    }

    @Test
    void publicar_serializationFails_throwsServicoExternoIndisponivelException() throws Exception {
        DeliveryEvaluatedEvent event = buildEvent("abc123", LocalDateTime.of(2024, 6, 15, 10, 0));
        when(objectMapper.writeValueAsBytes(event)).thenThrow(new JsonProcessingException("error") {});

        assertThatThrownBy(() -> publisher.publicar(event))
                .isInstanceOf(ServicoExternoIndisponivelException.class);
    }

    private DeliveryEvaluatedEvent buildEvent(String eventId, LocalDateTime ocorridoEm) {
        return DeliveryEvaluatedEvent.builder()
                .eventId(eventId)
                .ocorridoEm(ocorridoEm)
                .build();
    }
}
