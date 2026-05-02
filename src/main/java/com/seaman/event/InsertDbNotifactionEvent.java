package com.seaman.event;

import com.seaman.entity.SendNotificationEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class InsertDbNotifactionEvent extends ApplicationEvent {

    private SendNotificationEntity entity;

    public InsertDbNotifactionEvent(Object source) {
        super(source);
        this.entity = (SendNotificationEntity) source;
    }
}
