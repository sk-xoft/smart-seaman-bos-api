package com.seaman.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocumentRequestStepperRs {
    private String statusCode;
    private Integer currentStep;
    private List<Integer> completedSteps;
    private Boolean isCancelled;
}