package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.MSendNotificationEntity;
import com.seaman.entity.SendNotificationEntity;
import com.seaman.entity.UserSendNotificationEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SendNotificationRepository extends CommonRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public int insert(SendNotificationEntity entity) {

        int result = 0;

        StringBuilder sql = new StringBuilder();
        sql.append(" insert into m_send_notifications (MOBILE_USER_UUID, TERM, TITLE_MESSAGE, BODY_MESSAGE, SUCCESS, FAILURE, RESPONSE_BODY_FCM, CREATE_DATE, NOTI_TYPE, READ_STATUS, NOTI_DATE, VALUE_ID) ");
        sql.append(" values (:MOBILE_USER_UUID, :TERM, :TITLE_MESSAGE, :BODY_MESSAGE, :SUCCESS, :FAILURE, :RESPONSE_BODY_FCM, now(), :NOTI_TYPE, :READ_STATUS , now(), :VALUE_ID) ");

        try {

            // The GeneratedKeyHolder object is used to get the auto-incrementing ID.
            GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MOBILE_USER_UUID", entity.getMobileUserUUID())
                    .addValue("TERM", entity.getTerm())
                    .addValue("TITLE_MESSAGE", entity.getTitleMessage())
                    .addValue("BODY_MESSAGE", entity.getBodyMessage())
                    .addValue("SUCCESS", entity.getSuccess())
                    .addValue("FAILURE", entity.getFailure())
                    .addValue("RESPONSE_BODY_FCM", entity.getResponseBodyFcm())
                    .addValue("NOTI_TYPE", entity.getNotiType())
                    .addValue("READ_STATUS", entity.getReadStatus())
                    .addValue("VALUE_ID", entity.getValueId());

            int rowAffected = template.update(sql.toString(), namedParameters, generatedKeyHolder);

            if(rowAffected == 0){
                result = 0;
            } else {
                result = generatedKeyHolder.getKey().intValue();
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    /**
     * List user send notification.
     * @return
     */
    public List<UserSendNotificationEntity> listUserSendNotifications() {
        List<UserSendNotificationEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select * from user_send_notifications ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(UserSendNotificationEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<MSendNotificationEntity> listMsendNotifications(String muuid) {
        List<MSendNotificationEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_send_notifications where MOBILE_USER_UUID = :MOBILE_USER_UUID order by CREATE_DATE desc ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("MOBILE_USER_UUID", muuid);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(MSendNotificationEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return listAll;
    }

    public boolean updateNotificationById(String id, SendNotificationEntity request) {
        boolean result  = false;

        StringBuilder sql = new StringBuilder();
        sql.append(" update m_send_notifications set SUCCESS = :SUCCESS,  FAILURE = :FAILURE , RESPONSE_BODY_FCM = :RESPONSE_BODY_FCM where id = :ID ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("ID", id);
            namedParameters.addValue("SUCCESS", request.getSuccess());
            namedParameters.addValue("FAILURE", request.getFailure());
            namedParameters.addValue("RESPONSE_BODY_FCM", request.getResponseBodyFcm());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean updateNotificationById(String id) {
        boolean result  = false;

        StringBuilder sql = new StringBuilder();
        sql.append(" update m_send_notifications set READ_STATUS = 'YES', READ_DATE = now() where id = :ID ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("ID", id);

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean updateNotificationByUUID(String uuid) {
        boolean result  = false;

        StringBuilder sql = new StringBuilder();
        sql.append(" update m_send_notifications set READ_STATUS = 'YES', READ_DATE = now() where MOBILE_USER_UUID = :UUID ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("UUID", uuid);

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean deleteNotiWhenNotFound(String valueId, String notiType) {
        boolean result  = false;

        StringBuilder sql = new StringBuilder();
        sql.append(" delete from m_send_notifications where VALUE_ID = :VALUE_ID and NOTI_TYPE = :NOTI_TYPE ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("VALUE_ID", valueId);
            namedParameters.addValue("NOTI_TYPE", this.getNotiType(notiType));

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public Integer countNotificationByMUUID(String muuid) {

        Integer result = 0;

        StringBuilder sql = new StringBuilder();
        sql.append(" select count(*) as counts from m_send_notifications where READ_STATUS = 'NO' and MOBILE_USER_UUID = :MOBILE_USER_UUID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("MOBILE_USER_UUID", muuid);
            result = template.queryForObject(sql.toString(), namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    private String getNotiType(String type) {
        if("GENERAL".equals(type)){
            return "NEWS_GENERAL";
        } else if("SHIP".equals(type)) {
            return "NEWS_SHIP";
        } else {
            return type;
        }
    }
}
