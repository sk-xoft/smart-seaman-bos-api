package com.seaman.event;

import com.seaman.entity.SendNotificationEntity;
import com.seaman.model.external.request.FcmMessageRequest;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GlobalEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishInsertDbSenderFcm(SendNotificationEntity entity){
        applicationEventPublisher.publishEvent(new InsertDbNotifactionEvent(entity));
    }

    public void publishSenderFcmNotification(FcmMessageRequest item) {
        applicationEventPublisher.publishEvent(new FcmNotiEvent(item));
    }
}
