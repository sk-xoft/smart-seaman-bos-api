package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalDeptSubmissionResponse {
    private String submittedToDeptDate;
    private String availableFromDate;
    private String receivedFromDeptDate;
    private String recordedAt;
}
