package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentRenewalDeliveryResponse {
    private String trackingNo;
    private String carrier;
    private String shippedDate;
    private String deliveryStatus;
    private String shippedRecordedAt;
    private String deliveredAt;
}
