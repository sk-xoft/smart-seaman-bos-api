package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.*;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.CertificationRequest;
import com.seaman.model.request.DocumentCreateRequest;
import com.seaman.model.request.DocumentUpdateRequest;
import com.seaman.model.response.DocumentCreateResponse;
import com.seaman.model.response.DocumentUpdateResponse;
import com.seaman.model.response.PageDocumentResponse;
import com.seaman.repository.CertificationRepository;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;
    private final TransactionLogsService transactionLogsService;
    private final CertificationRepository certificationRepository;

    private final SendNotificationService sendNotificationService;

    private final FrameworkUtils frameworkUtils;

    private final DateUtil dateUtil;

    private final AmazonS3 getS3;

    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.template}")
    private String storePathTemplate;

    public PageDocumentResponse  listCot(CertificationRequest request, String docType) {

        PageDocumentResponse response = new PageDocumentResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "LIST_DOCUMENT_" + docType.toUpperCase();
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Get user mobile
            // UserMobileEntity userMobileEntity = userMobileRepository.findUserMobileByUUID(request.getMobileUserUuid());

            Integer startNum = request.getLastNum() - request.getSize();
            Integer totalRecord = certificationRepository.getTotalRecord(docType, request.getMobileUserUuid());

            if (startNum < 0){
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL," {size} can not more than {lastNum}.");
            }

            List<DocumentEntity>  documentEntityList  =  certificationRepository.listCertByTypeMobileUuid(startNum, request.getSize(), docType, request.getMobileUserUuid());
            List<DocumentEntity> certificateEntityList = new ArrayList<>();

            Integer num = 0;

            for(DocumentEntity entity : documentEntityList) {
                entity.setRowId(String.valueOf(++num));

                if(null == entity.getCertStartDate() && null == entity.getCertEndDate()) {
                    /**
                     * this case customer new register.
                     */
                    entity.setDisYear("");
                    entity.setDisMonth("");
                    entity.setDisDay("");

                } else {

                    if (null != entity.getCertEndDate()) {
                        String var1  =  dateUtil.formatDateToString(entity.getCertEndDate(), DateUtil.DATE_TIME);
                        Period certPeriod = dateUtil.calculateDisplayDateCertRemain(var1);
                        entity.setDisYear(String.valueOf(certPeriod.getYears()));
                        entity.setDisMonth(String.valueOf(certPeriod.getMonths()));
                        entity.setDisDay(String.valueOf(certPeriod.getDays()));
                    } else {
                        entity.setDisYear("-");
                        entity.setDisMonth("-");
                        entity.setDisDay("-");
                    }
                }

//                if(null != entity.getCertStartDate()) {
//                    Date varDate = dateUtil.parseStringToDate(entity.getCertStartDate(), DateUtil.DATE_TIME);
//                    String var1 = dateUtil.formatDateToString(varDate, DateUtil.DATE_MONTH_YEAR);
//                    entity.setCertStartDate(var1);
//                }
//
//                if(null != entity.getCertEndDate()) {
//                    Date varDate = dateUtil.parseStringToDate(entity.getCertEndDate(), DateUtil.DATE_TIME);
//                    String var1 = dateUtil.formatDateToString(varDate, DateUtil.DATE_MONTH_YEAR);
//                    entity.setCertEndDate(var1);
//                }

//                if(entity.getCertStartDate() != null) {
//                    entity.setCertStartDate(dateUtil.formatDateToStr(entity.getCertStartDate(), DateUtil.YEAR_MONTH_DATE));
//                }
//
//                if(entity.getCertEndDate() != null) {
//                    entity.setCertEndDate(dateUtil.formatDateToStr(entity.getCertEndDate(), DateUtil.YEAR_MONTH_DATE));
//                }

                certificateEntityList.add(entity);
            }

            Integer totalData = totalRecord;
            response.setSize(request.getSize());
            response.setLastNum(request.getLastNum());
            response.setTotalData(totalData);
            response.setDocumentEntityList(certificateEntityList);

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("List certification Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }

    public String veiwCertImage(String mobileUserUuid, String certCode) {

        String response = "";

        try {

            CertificateEntity certificateEntity = certificationRepository.findByMobileUserUuid(mobileUserUuid,  certCode);
            if(null == certificateEntity){
                throw new BusinessException(AppStatus.DATA_NOT_FOUND, certCode);
            }

            String keyName =  String.format(storePathTemplate, mobileUserUuid, certificateEntity.getCertFile());
            response = getS3.getObjectAsString(bucketName, keyName);
            log.info("Load file cert -> {} is success.", keyName);
        } catch (CommonException ce){
            throw  ce;
        } catch(Exception ex){
            log.error("Document view Exception {}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        }
        return response;
    }

    public DocumentCreateResponse documentCreate(DocumentCreateRequest request) {

        DocumentCreateResponse response = new DocumentCreateResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CERT_CREATE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            // Store trans
            username = usersEntity.getUsername();
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            String newFileName = frameworkUtils.generateUUID();

            List<CertificateEntity> certificateEntityList = certificationRepository.findByUsersAndCertCodeList(request.getUserMobileUuid(),  request.getDocumentCode());
            if(!certificateEntityList.isEmpty()) {
                throw new BusinessException(AppStatus.DATA_ALREADY, request.getDocumentCode());
            }

            // Insert table
            CertificateEntity entity  = new CertificateEntity();
            entity.setCertMobileUuid(request.getUserMobileUuid());
            entity.setCertDocumentCode(request.getDocumentCode());
            entity.setCertStartDate(request.getCertStartDate());
            entity.setCertEndDate(request.getCertEndDate());
            entity.setCertStatus("A");
            entity.setCertFile(newFileName);
            entity.setCreateDate(new Date());
            entity.setCreateBy(usersEntity.getUsername());
            entity.setUpdateDate(new Date());
            entity.setUpdateBy(usersEntity.getUsername());
            entity.setOriginalFileName(request.getFileCertName());

            // Insert cert is success
            if(certificationRepository.insert(entity)) {

                // Upload file to S3.
                if(!"".equals(request.getFileCert()) || null != request.getFileCert()) {
                    String keyName = String.format(storePathTemplate, request.getUserMobileUuid(), newFileName);
                    getS3.putObject(bucketName, keyName, request.getFileCert());
                    log.info("put object {} is success.", keyName);
                } else {
                    log.info("Not have send file 'Cert'.");
                }

                // Set response
                response.setDocumentCode(request.getDocumentCode());
                // response.setCertStartDate(request.getCertStartDate());
                response.setCertStartDate(dateUtil.convertStringToDate(request.getCertStartDate(), DateUtil.DATE_TIME));

                response.setCertEndDateType(request.getCertEndDateType());
                // response.setCertEndDate(request.getCertEndDate());
                response.setCertEndDate(dateUtil.convertStringToDate(request.getCertEndDate(), DateUtil.DATE_TIME));
                response.setFileCertName(request.getFileCertName());

                // Send noti cert create.
                String notiType = AppSys.NOTI_TYPE_CERT_UPDATED;
                String titleMessage = "Smart Seaman";
                String bodyMessage = "มีการอัปเดตประกาศนียบัตร";

                sendNotificationService.sendNotification(
                        request.getUserMobileUuid(),
                        notiType,
                        bodyMessage,
                        entity.getCertId(),
                        titleMessage
                );

                log.info("Start send noti create cert. -> update");
                log.info("Create Cert is success.");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Document create Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        }  finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }
        return response;
    }

    public DocumentUpdateResponse documentUpdate(DocumentUpdateRequest request) {

        DocumentUpdateResponse response = new DocumentUpdateResponse();

        boolean isStatusUpdate = false;

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CERT_UPDATE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            // Store trans
            username = usersEntity.getUsername();
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            String newFileName = frameworkUtils.generateUUID();

            List<CertificateEntity> certificateEntityList = certificationRepository.findByUsersAndCertCodeList(request.getUserMobileUuid(),  request.getDocumentCode());
            if(certificateEntityList.isEmpty()) {
                throw new BusinessException(AppStatus.DATA_EXITING, request.getDocumentCode());
            }

            // Update table
            CertificateEntity entity  = new CertificateEntity();
            entity.setCertMobileUuid(request.getUserMobileUuid());
            entity.setCertDocumentCode(request.getDocumentCode());
            entity.setCertStartDate(request.getCertStartDate());
            entity.setCertEndDate(request.getCertEndDate());
            entity.setCertStatus("A");
            entity.setUpdateDate(new Date());
            entity.setUpdateBy(usersEntity.getUsername());
            entity.setCertId(certificateEntityList.get(0).getCertId());

            // Is not change file cert.
            if(request.getIsChangeFile().equals("N")){
                isStatusUpdate  = certificationRepository.updateNoChangeFile(entity);
            } else {
                // is update file cert.
                entity.setCertFile(newFileName);
                entity.setOriginalFileName(request.getFileCertName());
                isStatusUpdate  = certificationRepository.update(entity);

                if(isStatusUpdate) {
                    // Upload file to S3.
                    String keyName =  String.format(storePathTemplate, request.getUserMobileUuid(), newFileName);
                    getS3.putObject(bucketName, keyName, request.getFileCert());
                    log.info("put object {} is success.", keyName);
                }
            }

            //Update cert is success
            if(isStatusUpdate) {

                // Set response
                response.setDocumentCode(request.getDocumentCode());
                response.setCertStartDate(request.getCertStartDate());
                response.setCertEndDateType(request.getCertEndDateType());
                response.setCertEndDate(request.getCertEndDate());
                response.setFileCertName(request.getFileCertName());

                final String notiType = AppSys.NOTI_TYPE_CERT_UPDATED;
                final String titleMessage = "Smart Seaman";
                final String bodyMessage = "มีการอัปเดตประกาศนียบัตร";

                sendNotificationService.sendNotification(
                        request.getUserMobileUuid(),
                        notiType,
                        bodyMessage,
                        entity.getCertId(),
                        titleMessage
                );

                log.info("Start send notification update cert. -> update");
                log.info("Update Cert is success.");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Document Update Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String documentDelete(String certCode, String userMobileUuid) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CERT_DELETE";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            // Store trans
            username = usersEntity.getUsername();
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<CertificateEntity> certificateEntityList = certificationRepository.findByUsersAndCertCodeList(userMobileUuid,  certCode);
            if(certificateEntityList.isEmpty()) {
                throw new BusinessException(AppStatus.DATA_EXITING, certCode);
            }

            if(certificationRepository.documentDelete(userMobileUuid, certCode)) {

                // Delete Object on S3.
                CertificateEntity entity =  certificateEntityList.get(0);
                String keyName = String.format(storePathTemplate, userMobileUuid, entity.getCertFile());
                getS3.deleteObject(bucketName, keyName);

                // Send noti cert create.
                String notiType = AppSys.NOTI_TYPE_CERT_DELETE;
                String titleMessage = "Smart Seaman";
                String bodyMessage = "ประกาศนียบัตรถูกลบ";

                sendNotificationService.sendNotification(
                        userMobileUuid,
                        notiType,
                        bodyMessage,
                        "0",
                        titleMessage
                );

                log.info("Start send noti create cert. -> update");
                log.info("Delete Cert is success.");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Document delete Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = frameworkUtils.toObjectToJson("success");
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";

    }

    public DocumentCreateResponse documentEdit(String certCode, String userMobileUuid) {

        DocumentCreateResponse response = new DocumentCreateResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CERT_EDIT";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            // Store trans
            username = usersEntity.getUsername();
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<CertificateForEditEntity> certificateEntityList = certificationRepository.findByUsersAndCertCodeListForEdit(userMobileUuid,  certCode);
            if(certificateEntityList.isEmpty()) {
                throw new BusinessException(AppStatus.DATA_EXITING, certCode);
            }

            CertificateForEditEntity item  =  certificateEntityList.get(0);

            // Set response
            response.setDocumentCode(item.getCertDocumentCode());
            // response.setCertStartDate(dateUtil.formatDateToStr(item.getCertStartDate(), DateUtil.YEAR_MONTH_DATE));
            response.setCertStartDate(item.getCertStartDate());

            if(null == item.getCertEndDate()) {
                response.setCertEndDateType("N");
                response.setCertEndDate(dateUtil.convertStringToDate("9999-99-99", DateUtil.DATE_TIME));
            } else {
                response.setCertEndDateType("A");
                //response.setCertEndDate(dateUtil.formatDateToStr(item.getCertEndDate(),DateUtil.YEAR_MONTH_DATE));
                response.setCertEndDate(item.getCertEndDate());
            }
            response.setFileCertName(item.getOriginalFileName());

            // Upload file to S3.
            if(null != item.getCertFile() && !"".equals(item.getCertFile())) {
                String keyName = String.format(storePathTemplate, userMobileUuid, item.getCertFile());
                String fileBase64 = getS3.getObjectAsString(bucketName, keyName);
                response.setFileBase64(fileBase64);
            } else  response.setFileBase64("");

        } catch (CommonException ce){
            throw  ce;
        } catch(Exception ex){
            log.error("Cert edit Exception {}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        }  finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

}
