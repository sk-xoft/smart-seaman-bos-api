package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.FcmNotificationEntity;
import com.seaman.entity.SendNotificationEntity;
import com.seaman.event.GlobalEventPublisher;
import com.seaman.exception.CommonException;
import com.seaman.model.external.request.FcmMessageData;
import com.seaman.model.external.request.FcmMessageRequest;
import com.seaman.model.external.response.FcmMessageResponse;
import com.seaman.model.request.NotificationModel;
import com.seaman.model.request.SendNotificationReq;
import com.seaman.repository.FcmRepository;
import com.seaman.repository.SendNotificationRepository;
import com.seaman.utils.ExternalApiUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SendNotificationService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final SendNotificationRepository sendNotificationRepository;
    private final FcmRepository fcmRepository;
    private final ExternalApiUtils externalApiUtils;
    private final GlobalEventPublisher eventPublisher;

//    public void sendNotiNews(String to, String notiType, String bodyMessage, String valueId, String titleMessage) {
//        try {
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.set("Authorization", String.format("key=%s", fcmAuth));
//
//            List<FcmNotificationEntity> items =  fcmRepository.findAllAccountHaveFCM();
//            for(FcmNotificationEntity model: items) {
//
//                // Insert log send notification
//                SendNotificationEntity entity = new SendNotificationEntity();
//                entity.setMobileUserUUID(model.getUserMobileUuid());
//                entity.setTerm("-");
//                entity.setTitleMessage(titleMessage);
//                entity.setBodyMessage(bodyMessage);
//                entity.setSuccess("0");
//                entity.setFailure("0");
//                entity.setNotiType(notiType);
//                entity.setReadStatus("NO");
//                entity.setValueId(valueId);
//                sendNotificationRepository.insert(entity);
//
//                int countNoti = sendNotificationRepository.countNotificationByMUUID(model.getUserMobileUuid());
//
//                FcmMessageData fcmMessageData = new FcmMessageData();
//                fcmMessageData.setTitle(titleMessage);
//                fcmMessageData.setBody(bodyMessage);
//                fcmMessageData.setNotiType(notiType);
//                fcmMessageData.setValueId(valueId);
//                fcmMessageData.setCountNoti(String.valueOf(countNoti));
//
//                NotificationModel notificationModel = new NotificationModel();
//                notificationModel.setTitle(titleMessage);
//                notificationModel.setBody(bodyMessage);
//                notificationModel.setSound("default");
//                notificationModel.setBadge(String.valueOf(countNoti));
//
//                // Prepare message
//                FcmMessageRequest fcmMessageRequest = new FcmMessageRequest();
//                fcmMessageRequest.setData(fcmMessageData);
//                fcmMessageRequest.setTo(model.getTokenFcm());
//                fcmMessageRequest.setPriority("high");
//                fcmMessageRequest.setMutable_content(true);
//                fcmMessageRequest.setNotification(notificationModel);
//
//                FcmMessageResponse fcmMessageResponse = (FcmMessageResponse) externalApiUtils.commonCaller(fcmNotificationUrl,
//                        HttpMethod.POST, headers, fcmMessageRequest, FcmMessageRequest.class, FcmMessageResponse.class,
//                        Object.class);
//
//                log.info("Send notification is success. To -> {},  is success -> {}", to, fcmMessageResponse.getSuccess());
//            }
//
//        } catch (CommonException ce) {
//            log.error("{}", ce.getMessage());
//
//        } catch (Exception ex) {
//            log.error("{}", ex.getMessage());
//        }
//    }

    public void sendNotificationManual(SendNotificationReq req) {

        try {

            // Create token
            var deviceToken  =  req.getDeviceToken();
            var uid = req.getUid();

            var fcm = new FcmNotificationEntity();
            fcm.setTokenFcm(deviceToken);
            fcm.setUserMobileUuid(uid);
            List<FcmNotificationEntity> items = Arrays.asList(
                    fcm
            );

            var title = req.getTitle();
            var body = req.getBody();
            var type = AppSys.NOTI_TYPE_NEWS_GENERAL;
            var valueId = "24";

            for(FcmNotificationEntity model: items) {

                // Insert log send notification
                SendNotificationEntity entity = new SendNotificationEntity();
                entity.setMobileUserUUID(model.getUserMobileUuid());
                entity.setTerm("-");
                entity.setTitleMessage(title);
                entity.setBodyMessage(body);
                entity.setSuccess("0");
                entity.setFailure("0");
                entity.setNotiType(type);
                entity.setReadStatus("NO");
                entity.setValueId(valueId);
                sendNotificationRepository.insert(entity);

                int countNoti = sendNotificationRepository.countNotificationByMUUID(model.getUserMobileUuid());

                FcmMessageData fcmMessageData = new FcmMessageData();
                fcmMessageData.setTitle(title);
                fcmMessageData.setBody(body);
                fcmMessageData.setNotiType(type);
                fcmMessageData.setValueId(valueId);
                fcmMessageData.setCountNoti(String.valueOf(countNoti));

                NotificationModel notificationModel = new NotificationModel();
                notificationModel.setTitle(title);
                notificationModel.setBody(body);
                notificationModel.setSound("default");
                notificationModel.setBadge(String.valueOf(countNoti));

                // Prepare a message
                FcmMessageRequest fcmMessageRequest = new FcmMessageRequest();
                fcmMessageRequest.setData(fcmMessageData);

                fcmMessageRequest.setDeviceTokens(
                        Arrays.asList(model.getTokenFcm())
                );
                fcmMessageRequest.setPriority("high");
                fcmMessageRequest.setMutable_content(true);
                fcmMessageRequest.setNotification(notificationModel);

                // Event sender notification
                eventPublisher.publishSenderFcmNotification(fcmMessageRequest);
            }

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
    }

    public void senderFcmNewsManual(String userMobileId, String tokenFcm, String type, String valueId, String title, String body) {

        try {
            FcmNotificationEntity u = new FcmNotificationEntity();
            u.setUserMobileUuid(userMobileId);
            u.setTokenFcm(tokenFcm);

            List<FcmNotificationEntity> items =  new ArrayList<>();
            items.add(u);

            for(FcmNotificationEntity model: items) {

                // Insert log send notification
                SendNotificationEntity entity = new SendNotificationEntity();
                entity.setMobileUserUUID(model.getUserMobileUuid());
                entity.setTerm("-");
                entity.setTitleMessage(title);
                entity.setBodyMessage(body);
                entity.setSuccess("0");
                entity.setFailure("0");
                entity.setNotiType(type);
                entity.setReadStatus("NO");
                entity.setValueId(valueId);
                sendNotificationRepository.insert(entity);

                int countNoti = sendNotificationRepository.countNotificationByMUUID(model.getUserMobileUuid());

                FcmMessageData fcmMessageData = new FcmMessageData();
                fcmMessageData.setTitle(title);
                fcmMessageData.setBody(body);
                fcmMessageData.setNotiType(type);
                fcmMessageData.setValueId(valueId);
                fcmMessageData.setCountNoti(String.valueOf(countNoti));

                NotificationModel notificationModel = new NotificationModel();
                notificationModel.setTitle(title);
                notificationModel.setBody(body);
                notificationModel.setSound("default");
                notificationModel.setBadge(String.valueOf(countNoti));

                // Prepare message
                FcmMessageRequest fcmMessageRequest = new FcmMessageRequest();
                fcmMessageRequest.setData(fcmMessageData);

                fcmMessageRequest.setDeviceTokens(
                        Arrays.asList(model.getTokenFcm())
                );
                fcmMessageRequest.setPriority("high");
                fcmMessageRequest.setMutable_content(true);
                fcmMessageRequest.setNotification(notificationModel);

                // Event sender notification
                eventPublisher.publishSenderFcmNotification(fcmMessageRequest);
            }

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
    }

    public void senderFcmNews(String type, String valueId, String title, String body) {

        try {

            List<FcmNotificationEntity> items =  fcmRepository.findAllAccountHaveFCM();

            for(FcmNotificationEntity model: items) {

                // Insert log send notification
                SendNotificationEntity entity = new SendNotificationEntity();
                entity.setMobileUserUUID(model.getUserMobileUuid());
                entity.setTerm("-");
                entity.setTitleMessage(title);
                entity.setBodyMessage(body);
                entity.setSuccess("0");
                entity.setFailure("0");
                entity.setNotiType(type);
                entity.setReadStatus("NO");
                entity.setValueId(valueId);
                sendNotificationRepository.insert(entity);

                int countNoti = sendNotificationRepository.countNotificationByMUUID(model.getUserMobileUuid());

                FcmMessageData fcmMessageData = new FcmMessageData();
                fcmMessageData.setTitle(title);
                fcmMessageData.setBody(body);
                fcmMessageData.setNotiType(type);
                fcmMessageData.setValueId(valueId);
                fcmMessageData.setCountNoti(String.valueOf(countNoti));

                NotificationModel notificationModel = new NotificationModel();
                notificationModel.setTitle(title);
                notificationModel.setBody(body);
                notificationModel.setSound("default");
                notificationModel.setBadge(String.valueOf(countNoti));

                // Prepare message
                FcmMessageRequest fcmMessageRequest = new FcmMessageRequest();
                fcmMessageRequest.setData(fcmMessageData);

                fcmMessageRequest.setDeviceTokens(
                        Arrays.asList(model.getTokenFcm())
                );
                fcmMessageRequest.setPriority("high");
                fcmMessageRequest.setMutable_content(true);
                fcmMessageRequest.setNotification(notificationModel);

                // Event sender notification
                eventPublisher.publishSenderFcmNotification(fcmMessageRequest);
            }

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
    }

    public void sendNotification(String userUuid, String notiType, String bodyMessage, String valueId, String titleMessage) {

        try {

            FcmNotificationEntity fcmNotificationEntity = fcmRepository.findAllAccountHaveFcmByUuid(userUuid);
            if(null != fcmNotificationEntity) {

                // Insert log send notification
                SendNotificationEntity entity = new SendNotificationEntity();
                entity.setMobileUserUUID(userUuid);
                entity.setTerm("-");
                entity.setTitleMessage(titleMessage);
                entity.setBodyMessage(bodyMessage);
                entity.setSuccess("0");
                entity.setFailure("0");
                entity.setNotiType(notiType);
                entity.setReadStatus("NO");
                entity.setValueId(valueId);
                int notiId = sendNotificationRepository.insert(entity);

                int countNoti = sendNotificationRepository.countNotificationByMUUID(userUuid);

                FcmMessageData fcmMessageData = new FcmMessageData();
                fcmMessageData.setTitle(titleMessage);
                fcmMessageData.setBody(bodyMessage);
                fcmMessageData.setNotiType(notiType);
                fcmMessageData.setValueId(valueId);
                fcmMessageData.setNotiId(String.valueOf(notiId));
                fcmMessageData.setCountNoti(String.valueOf(countNoti));

                NotificationModel notificationModel = new NotificationModel();
                notificationModel.setTitle(titleMessage);
                notificationModel.setBody(bodyMessage);
                notificationModel.setSound("default");
                notificationModel.setBadge(String.valueOf(countNoti));

                // Prepare message
                FcmMessageRequest fcmMessageRequest = new FcmMessageRequest();
                fcmMessageRequest.setData(fcmMessageData);

                // Prepare token fcm
                fcmMessageRequest.setDeviceTokens(
                        Arrays.asList(fcmNotificationEntity.getTokenFcm())
                );

                fcmMessageRequest.setPriority("high");
                fcmMessageRequest.setMutable_content(true);
                fcmMessageRequest.setNotification(notificationModel);

                // Event sender notification
                eventPublisher.publishSenderFcmNotification(fcmMessageRequest);
                log.info("Send notification is success. To -> {}",fcmNotificationEntity.getUserMobileUuid());
            }  else log.info("User is not account on FCM.");

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
        }
    }
}
