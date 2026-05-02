package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.FormEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.FormRq;
import com.seaman.model.response.FormRs;
import com.seaman.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;


@Service
@RequiredArgsConstructor
public class DocumentService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final HttpServletRequest httpServletRequest;
    private final DocumentRepository documentRepository;
    private  final TransactionLogsService transactionLogsService;

    @Value("${object.store.bucket}")
    private String bucketName;
    @Value("${object.store.path.documents.download}")
    private String pathUploadFile;
    private final AmazonS3 getS3;

    public FormRs getAllForm(FormRq request) {
        FormRs response = new FormRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET ALL FORM";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            Integer startNum = request.getLastNum() - request.getSize();

            if (startNum < 0){
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL," {size} can not more than {lastNum}.");
            }

            List<FormEntity> formLists = new ArrayList<>();
            formLists = documentRepository.listAllForm(startNum,request.getSize());
            log.info("last num :{} ", startNum );

            Integer num = 0;

            List<FormEntity> formRsList = new ArrayList<>();

            for(FormEntity item :formLists){
                FormEntity form = new FormEntity();
                ++num;
                form.setFormNum(startNum+num);
                form.setFormId(item.getFormId());
                form.setFormName(item.getFormName());
                form.setFormFileName(item.getFormFileName());
                form.setCreateDate(item.getCreateDate());
                form.setCreateBy(item.getCreateBy());
                form.setUpdateDate(item.getUpdateDate());
                form.setUpdateBy(item.getUpdateBy());
                formRsList.add(form);
            }

            Integer totalData = documentRepository.getTotalForm();
            response.setSize(request.getSize());
            response.setLastNum(request.getLastNum());
            response.setTotalData(totalData);
            response.setFormList(formRsList);
            response.setCountList(formRsList.size());

            log.info("Get all form is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get all form -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public FormRs insertForm(FormRq request) {

        FormRs response = new FormRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "INSERT FORM";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Insert table
            FormEntity entity  = new FormEntity();
            entity.setFormName(request.getFormName());
            entity.setFromFileId(request.getFromFileId());
            entity.setFormFileName(request.getFormFileName());
            entity.setCreateDate(new Date());
            entity.setCreateBy(username);
            log.info("entity before insert : {} ", entity );

            if(!documentRepository.listFormByFormFileName(request.getFormFileName())){
                if(documentRepository.insertForm(entity)) {
                    if(!"".equals(request.getFormFile()) || null != request.getFormFile()) {
                        String keyName =  pathUploadFile + "/" + request.getFormFileName();
                        getS3.putObject(bucketName, keyName, request.getFormFile());
                        log.info("put object {} is success.", keyName);
                    } else {
                        log.info("Not have send file 'Form'.");
                    }
                    response.setFormId(documentRepository.getMaxId());
                }
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL," Form file name already exist. ");
            }

            log.info("Create form is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Create form -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String deleteForm(Integer formId) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "DELETE FROM";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            if(documentRepository.listFormById(formId.toString())!=null){
                documentRepository.deleteForm(formId.toString());
                log.info("Delete form is success.");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("<> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public String updateCourse(FormRq request) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "UPDATE_FORM";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            if(!documentRepository.listFormByFormFileName(request.getFormFileName())){
                if(documentRepository.updateForm(request,username)) {
                    if(!"".equals(request.getFormFile()) || null != request.getFormFile()) {

                        String keyName =  pathUploadFile + "/" + request.getFormFileName();
                        getS3.putObject(bucketName, keyName, request.getFormFile());
                        log.info("put object {} is success.", keyName);
                    } else {
                        log.info("Not have send file 'Form'.");
                    }
                    //response.setFormId(documentRepository.getMaxId());
                }
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL," Form file name already exist. ");
            }

            log.info("Update form is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Update form Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public FormRs getFormById(String formId) {
        FormRs response = new FormRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET FORM BY ID";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<FormEntity> formLists = new ArrayList<>();
            FormEntity form = documentRepository.listFormById(formId);

            // TODO Download S3.
            String keyName =  pathUploadFile + "/" + form.getFormFileName();

            // วีธี Load file form BASE64
            String fileBase64 = getS3.getObjectAsString(bucketName, keyName);

            form.setFormNum(1);
            form.setFileBase64(fileBase64);
            formLists.add(form);
            response.setSize(1);
            response.setLastNum(1);
            response.setTotalData(1);
            response.setFormList(formLists);
            log.info("Process list-form by id is success.");

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error(" Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public byte[] downloadForm(String formId) {

        byte[] fileDownload;
        try {

            FormEntity entity = documentRepository.listFormById(formId);
            String keyName =  pathUploadFile + "/" + entity.getFormFileName();

            // วิธี Load file
            // S3Object s3Object = getS3.getObject(bucketName, keyName);
            // fileDownload = IOUtils.toByteArray(s3Object.getObjectContent());

            // วีธี Load file form BASE64
            String fileBase64 = getS3.getObjectAsString(bucketName, keyName);
            fileDownload = Base64.getDecoder().decode(fileBase64);

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw ex;
        }

        return fileDownload;
    }
}
