package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.TransactionLogsEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionLogsRepository extends CommonRepository{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String INSERT_TRANSACTION =  "insert into t_transaction_logs (TRANS_ID, REQUEST_BY, SERVICE_NAME, REQUEST_DATE_TIME,  LANGUAGE, " +
            "                                DEVICE_MODEL, DEVICE_INFO, TOKEN, REQUEST_DATA, CREATE_DATE, CREATE_BY ) " +
            " values (:TRANS_ID, :REQUEST_BY, :SERVICE_NAME, :REQUEST_DATE_TIME, :LANGUAGE, " +
            "                                :DEVICE_MODEL, :DEVICE_INFO, :TOKEN, :REQUEST_DATA, :CREATE_DATE, :CREATE_BY ) ";
    private static final String UPDATE_TRANSACTION = "update t_transaction_logs set RESPONSE_STATUS_CODE = :RESPONSE_STATUS_CODE, RESPONSE_DATA = :RESPONSE_DATA , RESPONSE_DATE_TIME = :RESPONSE_DATE_TIME , UPDATE_DATE = :UPDATE_DATE , UPDATE_BY= :UPDATE_BY where TRANS_ID = :TRANS_ID ";

    public boolean insert(TransactionLogsEntity entity) {

        boolean result = false;
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("TRANS_ID", entity.getTransId())
                    .addValue("REQUEST_BY", entity.getRequestBy())
                    .addValue("SERVICE_NAME", entity.getServiceName())
                    .addValue("LANGUAGE", entity.getLanguage())
                    .addValue("DEVICE_MODEL", entity.getDeviceModel())
                    .addValue("DEVICE_INFO", entity.getDeviceInfo())
                    .addValue("TOKEN", entity.getToken())
                    .addValue("REQUEST_DATA", entity.getRequestData())
                    .addValue("REQUEST_DATE_TIME", entity.getRequestDateTime())
                    .addValue("CREATE_DATE", entity.getCreateDate())
                    .addValue("CREATE_BY", entity.getCreateBy());

            int rowAffected = template.update(INSERT_TRANSACTION, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean update(TransactionLogsEntity entity) {
        boolean result = false;

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("RESPONSE_STATUS_CODE", entity.getResponseStatusCode())
                    .addValue("RESPONSE_DATA", entity.getResponseData())
                    .addValue("RESPONSE_DATE_TIME", entity.getResponseDateTime())
                    .addValue("UPDATE_DATE", entity.getUpdateDate())
                    .addValue("UPDATE_BY", entity.getUpdateBy())
                    .addValue("TRANS_ID", entity.getTransId());

            int rowAffected = template.update(UPDATE_TRANSACTION, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }
}
