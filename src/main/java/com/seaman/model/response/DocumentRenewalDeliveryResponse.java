package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalDeliveryResponse {
    private String trackingNo;
    private String carrier;
    private String shippedDate;
    private String shippedDateValue;
    private String deliveryStatus;
    private String shippedRecordedAt;
    private String shippedBy;
    private String shippedByUsername;
    private String shippedByFirstName;
    private String shippedByLastName;
    private String shippedByMobileNumber;
    private String deliveredAt;
}
