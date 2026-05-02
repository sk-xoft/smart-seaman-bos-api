package com.seaman.model.request;

import lombok.Data;

@Data
public class NotificationModel {
    private String title;
    private String body;
    private String sound;
    private String badge;
}
