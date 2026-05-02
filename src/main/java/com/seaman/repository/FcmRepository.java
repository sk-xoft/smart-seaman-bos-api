package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.FcmNotificationEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FcmRepository extends CommonRepository {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public FcmNotificationEntity findByUserUUID(String uuid) {

        List<FcmNotificationEntity> listRecord = null;
        FcmNotificationEntity result = null;

        StringBuilder sql  = new StringBuilder();
        sql.append("select * from m_fcm_notifications where USER_MOBILE_UUID = :USER_MOBILE_UUID ");
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USER_MOBILE_UUID", uuid);

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(FcmNotificationEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public List<FcmNotificationEntity> findAllAccountHaveFCM() {

        List<FcmNotificationEntity> listRecord = null;

        StringBuilder sql  = new StringBuilder();
        sql.append(" select USER_MOBILE_UUID, TOKEN_FCM from m_mobile_users mmu ");
        sql.append(" inner join  m_fcm_notifications mfn on mmu.MOBILE_UUID  = mfn.USER_MOBILE_UUID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(FcmNotificationEntity.class));
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return listRecord;
    }

    public FcmNotificationEntity findAllAccountHaveFcmByUuid(String uuid) {

        List<FcmNotificationEntity> listRecord = null;
        FcmNotificationEntity model = null;

        StringBuilder sql  = new StringBuilder();
        sql.append(" select USER_MOBILE_UUID, TOKEN_FCM from m_mobile_users mmu ");
        sql.append(" inner join  m_fcm_notifications mfn on mmu.MOBILE_UUID  = mfn.USER_MOBILE_UUID ");
        sql.append(" where mmu.MOBILE_UUID = :MOBILE_UUID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("MOBILE_UUID", uuid);

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(FcmNotificationEntity.class));
            if(!listRecord.isEmpty()){
                model = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return model;
    }

    public boolean insert(FcmNotificationEntity entity) {

        boolean result = false;
        StringBuilder sql = new StringBuilder();
        sql.append(" insert into m_fcm_notifications (USER_MOBILE_UUID, TOKEN_FCM, CREATE_DATE, UPDATE_DATE) ");
        sql.append(" values (:USER_MOBILE_UUID, :TOKEN_FCM, now(), now())");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USER_MOBILE_UUID", entity.getUserMobileUuid())
                    .addValue("TOKEN_FCM", entity.getTokenFcm());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean update(FcmNotificationEntity entity) {

        boolean result = false;
        StringBuilder sql = new StringBuilder();
        sql.append(" update m_fcm_notifications set TOKEN_FCM = :TOKEN_FCM, UPDATE_DATE =  now() ");
        sql.append(" where USER_MOBILE_UUID = :USER_MOBILE_UUID ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USER_MOBILE_UUID", entity.getUserMobileUuid())
                    .addValue("TOKEN_FCM", entity.getTokenFcm());

            int rowAffected = template.update(sql.toString(),namedParameters);
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