package com.seaman.model.request;

import lombok.Data;

@Data
public class DocumentPickupActionRequest {
    private String requestNo;
    private String action;
    private String availablePickupDate;
    private String receivedDate;
    private String trackingNo;
    private String shippedDate;
}
