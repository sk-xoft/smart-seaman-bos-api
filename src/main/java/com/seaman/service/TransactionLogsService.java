package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.TransactionLogsEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.repository.TransactionLogsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TransactionLogsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TransactionLogsRepository transactionLogsRepository;

    private final HttpServletRequest httpServletRequest;

    public  boolean insert(String transId, String bodyReqJson, String serviceName, String createBy){

        TransactionLogsEntity entity  = new TransactionLogsEntity();
        boolean status  = false;

        try {
            entity.setTransId(transId);
            entity.setRequestBy("WEB_ADMIN");
            entity.setServiceName(serviceName);

            // Get header
            String language = httpServletRequest.getHeader(AppSys.HEADER_LANGUAGE);
            String deviceModel = httpServletRequest.getHeader(AppSys.HEADER_DEVICE_MODEL);
            String deviceInfo = httpServletRequest.getHeader(AppSys.HEADER_DEVICE_INFO);
            String authorization = httpServletRequest.getHeader(AppSys.HEADER_AUTHORIZATION);
            String token = "";
            if(authorization != null){
                token = authorization.substring(7);
            }

            entity.setLanguage(language);
            entity.setDeviceModel(deviceModel);
            entity.setDeviceInfo(deviceInfo);
            entity.setToken(token);
            entity.setRequestData(bodyReqJson);
            entity.setRequestDateTime(new Date());
            entity.setCreateBy(createBy);
            entity.setCreateDate(new Date());
            status = transactionLogsRepository.insert(entity);
        } catch (CommonException ce){
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex){
            throw  new BusinessException("MA9999", ex.getMessage());
        }

        return status;
    }

    public boolean update(String transId, String resJson, String statusCode, String updateBy) {
        TransactionLogsEntity entity  = new TransactionLogsEntity();
        boolean status  = false;
        try {

            entity.setTransId(transId);
            entity.setResponseData(resJson);
            entity.setResponseStatusCode(statusCode);
            entity.setResponseDateTime(new Date());
            entity.setUpdateBy(updateBy);
            entity.setUpdateDate(new Date());
            status = transactionLogsRepository.update(entity);
        } catch (CommonException ce){
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex){
            throw  new BusinessException("MA9999", ex.getMessage());
        }
        return status;
    }
}
