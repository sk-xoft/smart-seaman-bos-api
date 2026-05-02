package com.seaman.model.external.request;

import com.seaman.model.request.NotificationModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FcmMessageRequest {
    private List<String> deviceTokens;
    private String priority;
    private boolean mutable_content;
    private NotificationModel notification;
    private FcmMessageData data;
}
