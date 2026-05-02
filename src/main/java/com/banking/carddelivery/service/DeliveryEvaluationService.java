package com.banking.carddelivery.service;

import com.banking.carddelivery.domain.dto.DeliveryEvaluationRequest;
import com.banking.carddelivery.domain.dto.DeliveryEvaluationResponse;

public interface DeliveryEvaluationService {

    DeliveryEvaluationResponse evaluate(DeliveryEvaluationRequest request);
}
