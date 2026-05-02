package com.banking.carddelivery.controller;

import com.banking.carddelivery.domain.dto.DeliveryEvaluationRequest;
import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;
import com.banking.carddelivery.service.DeliveryEvaluationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards/delivery")
public class DeliveryEvaluationController {

    private final DeliveryEvaluationService deliveryEvaluationService;

    public DeliveryEvaluationController(DeliveryEvaluationService deliveryEvaluationService) {
        this.deliveryEvaluationService = deliveryEvaluationService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<DeliveryEvaluationResponse> evaluate(
            @Valid @RequestBody DeliveryEvaluationRequest request) {
        return ResponseEntity.ok(deliveryEvaluationService.evaluate(request));
    }
}
