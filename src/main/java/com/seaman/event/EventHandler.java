package com.seaman.event;

import com.seaman.push.noti.FcmSendNotificationComponent;
import com.seaman.repository.SendNotificationRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@EnableAsync
@Component
@AllArgsConstructor
public class EventHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final SendNotificationRepository sendNotificationRepository;
    private final FcmSendNotificationComponent fcmSendNotificationComponent;

    @Async
    @SneakyThrows
    @EventListener
    public void handleInsertNotification(InsertDbNotifactionEvent event){
        try {
            sendNotificationRepository.insert(event.getEntity());
        } catch (Exception ex) {
            log.error("Error {} Handler event insert send_notification.", ex.getMessage());
        }
    }

    @Async
    @SneakyThrows
    @EventListener
    public void handleSendFcmNotificationEvent(FcmNotiEvent event) {

        if (null != event.getRequest()) {

            if(!event.getRequest().getDeviceTokens().isEmpty()) {
                var deviceTokens = event.getRequest().getDeviceTokens();
                var req = event.getRequest();
                fcmSendNotificationComponent.sender(
                        deviceTokens,
                        req
                );
                log.info("Event send fcm is success.");
            }
        } else {

            log.info("Event send fcm data is empty.");
        }
    }
}
