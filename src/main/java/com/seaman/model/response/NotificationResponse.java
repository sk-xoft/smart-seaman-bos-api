package com.seaman.model.response;

import lombok.Data;

import java.util.List;

@Data
public class NotificationResponse {
    private List<NotificationsModel> notificationsModels;
}
