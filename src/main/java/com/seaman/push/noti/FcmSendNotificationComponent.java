package com.seaman.push.noti;

import com.google.firebase.messaging.*;
import com.seaman.exception.CommonException;
import com.seaman.model.external.request.FcmMessageData;
import com.seaman.model.external.request.FcmMessageRequest;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class FcmSendNotificationComponent {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final FirebaseMessaging firebaseMessaging;

    public void sender(List<String> deviceTokens, FcmMessageRequest item) {

        try {

            final String title = item.getData().getTitle();
            final String body = item.getData().getBody();

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            var var1 = 0;

            try {
                var1 = Integer.valueOf(item.getData().getCountNoti());
            } catch (Exception e) {
                log.error("Cast count noti string to int. {}", e);
            }

            Map<String, Object> option = new HashMap<>();
            option.put("badge", var1);
            option.put("sound", "default");
            var apsAppConfig = Aps.builder().putAllCustomData(option).build();
            var apns = ApnsConfig.builder().setAps(apsAppConfig).build();

            var data = item.getData();
            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .setNotification(notification)
                    .addAllTokens(deviceTokens)
                    .putAllData(data2Map(data))
                    .setApnsConfig(apns)
                    .build();
            BatchResponse response = firebaseMessaging.sendEachForMulticast(multicastMessage);
            var success = response.getSuccessCount();
            var fail = response.getFailureCount();
            log.info("Send notification total -> {} is successful count -> {} and fail count -> {}.",
                    deviceTokens.size(), success, fail);

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
    }

    private Map<String, String> data2Map(FcmMessageData data) {
        Map<String, String> items = new HashMap<>();
        items.put("title", data.getTitle());
        items.put("body", data.getBody());
        items.put("countNoti", data.getCountNoti());
        items.put("notiType", data.getNotiType());
        items.put("valueId", data.getValueId());
        return items;
    }
}
