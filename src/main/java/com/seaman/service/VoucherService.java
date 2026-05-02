package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.NewsEntity;
import com.seaman.entity.TokenFcmEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.entity.VoucherEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.VoucherRequest;
import com.seaman.model.response.NewsRs;
import com.seaman.model.response.VoucherResponse;
import com.seaman.repository.NewsRepository;
import com.seaman.repository.SendNotificationRepository;
import com.seaman.repository.UserMobileRepository;
import com.seaman.repository.VoucherRepository;
import com.seaman.utils.FrameworkUtils;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final FrameworkUtils frameworkUtils;
    private final HttpServletRequest httpServletRequest;
    private final VoucherRepository voucherRepository;
    private final TransactionLogsService transactionLogsService;
    private final SendNotificationService sendNotificationService;
    private final UserMobileRepository userMobileRepository;
    private final SendNotificationRepository sendNotificationRepository;
    private final AmazonS3 getS3;

    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.voucher}")
    private String pathVoucher;

    @Value("${object.store.path.voucher.qr}")
    private String pathVoucherQR;

    public VoucherResponse voucherAll(VoucherRequest request) {

        VoucherResponse response = new VoucherResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET ALL VOUCHER";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            int startNum = request.getLastNum() - request.getSize();
            Integer totalRecord = voucherRepository.getTotalRecord();

            if (startNum < 0) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, " {size} can not more than {lastNum}.");
            }

            List<VoucherEntity> voucherEntities  = voucherRepository.findAll(startNum, request.getLastNum(), request);
            List<VoucherEntity> voucherEntitiesCount  = voucherRepository.findAllCount(startNum, request.getLastNum(), request);
            List<VoucherEntity> rsLists = new ArrayList<>();
            int index = 0;
            for (VoucherEntity item : voucherEntities) {
                index++;
                item.setVoucherSeq(startNum+index);
                String smartSeamanId = voucherRepository.findSmartSeamanId(item.getVoucherId());
                item.setVoucherSmartSeamanId(smartSeamanId);
                rsLists.add(item);
            }

            response.setSize(request.getSize());
            response.setLastNum(request.getLastNum());
            response.setTotalData(totalRecord);
            response.setVoucherList(rsLists);
            response.setCountList(voucherEntitiesCount.size());

            log.info("List all voucher is success.");
        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Get all voucher -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String insertVoucher(VoucherRequest request) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "VOUCHER CREATE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            String fileBanner = frameworkUtils.generateUUID();
            String fileQr = frameworkUtils.generateUUID();

            // Insert table
            VoucherEntity entity = new VoucherEntity();
            entity.setVoucherTitle(request.getVoucherTitle());
            entity.setVoucherDetails(request.getVoucherDetails());
            entity.setVoucherPicture(fileBanner);
            entity.setVoucherQrcode(fileQr);
            entity.setVoucherSmartSeamanId(request.getVoucherSmartseamanId());
            entity.setVoucherType(request.getVoucherType()); // PERSONAL, GLOBAL
            entity.setCreateDate(new Date());
            entity.setCreateBy(username);

            int voucherId = voucherRepository.insertVoucher(entity);

            if(!"".equals(request.getVoucherType()) && "PERSONAL".equals(request.getVoucherType())){
                log.info("User case PERSONAL. Voucher id -> {}, SmartSeaman ID -> {}", voucherId, request.getVoucherSmartseamanId());

                // Insert PERSONAL
                voucherRepository.insertVoucherDetail(String.valueOf(voucherId), request.getVoucherSmartseamanId());

                /** send noti voucher **/
                String notiType = AppSys.NOTI_TYPE_VOUCHER;
                String titleMessage = "สิทธิประโยชน์";
                TokenFcmEntity userTokenFcm = userMobileRepository.findTokenUserMobile(request.getVoucherSmartseamanId());

                try {

                    sendNotificationService.sendNotification(
                            userTokenFcm.getMobileUuid(),
                            notiType,
                            request.getVoucherTitle(),
                            String.valueOf(voucherId),
                            titleMessage
                    );

                    log.info("Send notification voucher is success.");
                } catch (Exception exception){
                    log.error("Can not send notification. {}", exception.getMessage());
                }
            } else {
                /** send noti voucher  all **/
                String notiType = AppSys.NOTI_TYPE_VOUCHER;
                String titleMessage = "สิทธิประโยชน์";

                /**
                 * Case send noti voucher all -> user sub script all
                 */

                // request.getNewsId()
                sendNotificationService.senderFcmNews(
                        notiType,
                        String.valueOf(voucherId),
                        titleMessage,
                        request.getVoucherTitle()
                );
            }

            String keyName = pathVoucher + "/" + fileBanner;
            getS3.putObject(bucketName, keyName, request.getVoucherPicture());
            log.info("put object {} is success. image banner.", keyName);

            String keyNameQR = pathVoucherQR + "/" + fileQr;
            getS3.putObject(bucketName, keyNameQR, request.getVoucherQrcode());
            log.info("put object {} is success. image QR.", keyName);

        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("{}", ex);
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return statusCode;
    }

    public String deleteVoucher(String voucherId) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "VOUCHER DELETE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            String fileBanner = frameworkUtils.generateUUID();
            String fileQr = frameworkUtils.generateUUID();

            // Insert table
            VoucherEntity entity = voucherRepository.findById(voucherId);
            int isDelete = voucherRepository.deleteVoucher(entity);

            if(!"".equals(entity.getVoucherType()) && "PERSONAL".equals(entity.getVoucherType())){
                log.info("User case PERSONAL.");
                voucherRepository.deleteVoucherDetails(voucherId);

                /** send noti voucher **/
//                String notiType = AppSys.NOTI_TYPE_VOUCHER;
//                String titleMessage = "สิทธิประโยชน์";
//                TokenFcmEntity userTokenFcm = userMobileRepository.findTokenUserMobile(request.getVoucherSmartseamanId());
//                sendNotificationService.sendNotiNews(userTokenFcm.getTokenFcn(), notiType, request.getVoucherTitle(), String.valueOf(voucherId), titleMessage);
            }

            /**
             * When has deleted voucher and delete m_send_notification together.
             */
            if(isDelete > 0 ){
                sendNotificationRepository.deleteNotiWhenNotFound(voucherId, "VOUCHER");
                log.info("Voucher is delete and notification is success.");
            }

            String keyName = pathVoucher + "/" + entity.getVoucherPicture();
            getS3.deleteObject(bucketName, keyName);
            log.info("Delete object {} is success. image banner.", keyName);

            String keyNameQR = pathVoucherQR + "/" + entity.getVoucherQrcode();
            getS3.deleteObject(bucketName, keyNameQR);
            log.info("Delete object {} is success. image QR.", keyName);

        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Insert news Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return statusCode;
    }

    public VoucherEntity findVoucherId(String id) {
        VoucherEntity response = new VoucherEntity();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "VOUCHER VIEW";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Insert table
            response = voucherRepository.findById(id);

            String keyName = pathVoucher + "/" + response.getVoucherPicture();
            String imageBase64 = getS3.getObjectAsString(bucketName, keyName);
            response.setVoucherPicture(imageBase64);

            String keyNameQR = pathVoucherQR + "/" + response.getVoucherQrcode();
            String imageQRBase64 = getS3.getObjectAsString(bucketName, keyNameQR);
            response.setVoucherQrcode(imageQRBase64);

        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Insert news Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }
}
