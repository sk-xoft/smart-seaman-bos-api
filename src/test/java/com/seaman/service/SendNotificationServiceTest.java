package com.seaman.service;

import com.seaman.entity.FcmNotificationEntity;
import com.seaman.event.GlobalEventPublisher;
import com.seaman.model.external.request.FcmMessageRequest;
import com.seaman.repository.FcmRepository;
import com.seaman.repository.SendNotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.seaman.constant.AppSys.NOTI_TYPE_DOCUMENT_RENEWAL_DELIVERED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationServiceTest {

    @Mock
    private SendNotificationRepository sendNotificationRepository;
    @Mock
    private FcmRepository fcmRepository;
    @Mock
    private GlobalEventPublisher eventPublisher;
    @InjectMocks
    private SendNotificationService service;

    @Test
    void includesTrackingNumberInDocumentDeliveryNotificationPayload() {
        FcmNotificationEntity fcm = new FcmNotificationEntity();
        fcm.setUserMobileUuid("mobile-user-uuid");
        fcm.setTokenFcm("device-token");
        when(fcmRepository.findAllAccountHaveFcmByUuid("mobile-user-uuid")).thenReturn(fcm);
        when(sendNotificationRepository.insert(org.mockito.ArgumentMatchers.any())).thenReturn(10);
        when(sendNotificationRepository.countNotificationByMUUID("mobile-user-uuid")).thenReturn(3);

        service.sendNotification(
                "mobile-user-uuid",
                NOTI_TYPE_DOCUMENT_RENEWAL_DELIVERED,
                "เอกสารของคุณจัดส่งสำเร็จแล้ว\nเลขพัสดุ: ED363095983TH",
                "260700046",
                "Smart Seaman",
                "ED363095983TH"
        );

        ArgumentCaptor<FcmMessageRequest> captor = ArgumentCaptor.forClass(FcmMessageRequest.class);
        verify(eventPublisher).publishSenderFcmNotification(captor.capture());
        FcmMessageRequest message = captor.getValue();
        assertEquals("ED363095983TH", message.getData().getTrackingNo());
        assertEquals("260700046", message.getData().getValueId());
        assertEquals("เอกสารของคุณจัดส่งสำเร็จแล้ว\nเลขพัสดุ: ED363095983TH",
                message.getNotification().getBody());
    }
}