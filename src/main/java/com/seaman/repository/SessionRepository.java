package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.SessionEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class SessionRepository extends CommonRepository {

    private final Logger logger = LoggerFactory.getLogger(SessionRepository.class);
    private static final String INSERT_SESSION =  "insert into t_session (CLIENT_SESSION_ID, USER_ID, LOGIN_TIME, LAST_UPDATE_TIME, EXPIRE_TIME,DEVICE_MODEL, TOKEN , CREATE_DATE, CREATE_BY, UPDATE_DATE, UPDATE_BY, CORRELATIONID, IS_ONLINE) "
         + " values (:CLIENT_SESSION_ID, :USER_ID , :LOGIN_TIME , :LAST_UPDATE_TIME , :EXPIRE_TIME , :DEVICE_MODEL, :TOKEN, :CREATE_DATE , :CREATE_BY , :UPDATE_DATE , :UPDATE_BY, :CORRELATIONID, :IS_ONLINE)";

    private static final String FIND_BY_SESSION_ID = "select * from t_session where CLIENT_SESSION_ID = :CLIENT_SESSION_ID";
    private static final String UPDATE_SESSION = "update t_session set TOKEN = :TOKEN where SESSION_ID = :SESSION_ID";

    public boolean insert(SessionEntity entity) {

        boolean result = false;
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("CLIENT_SESSION_ID", entity.getClientSessionId())
                    .addValue("USER_ID", entity.getUserId())
                    .addValue("LOGIN_TIME", entity.getLoginTime())
                    .addValue("LAST_UPDATE_TIME", entity.getLastUpdateTime())
                    .addValue("EXPIRE_TIME", entity.getExpireTime())
                    .addValue("DEVICE_MODEL", entity.getDeviceModel())
                    .addValue("TOKEN", entity.getToken())
                    .addValue("CREATE_DATE", entity.getCreateDate())
                    .addValue("CREATE_BY", entity.getCreateBy())
                    .addValue("UPDATE_DATE", entity.getUpdateDate())
                    .addValue("UPDATE_BY", entity.getUpdateBy())
                    .addValue("IS_ONLINE", entity.getIsOnline())
                    .addValue("CORRELATIONID", entity.getCorrelationId());

            int rowAffected = template.update(INSERT_SESSION, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public SessionEntity findById(String sessionId) {

        List<SessionEntity> listRecord = null;
        SessionEntity result = null;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("CLIENT_SESSION_ID", sessionId);

            listRecord = template.query(FIND_BY_SESSION_ID, namedParameters, new BeanPropertyRowMapper(SessionEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public List<SessionEntity> findAll(String sessionId) {

        List<SessionEntity> listAll = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("SESSION_ID", sessionId);

            listAll = template.query(FIND_BY_SESSION_ID, namedParameters, new BeanPropertyRowMapper(SessionEntity.class));

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return listAll;
    }

    public boolean update(SessionEntity entity) {
        boolean result = false;

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("SESSION_ID", entity.getSessionId())
                    .addValue("TOKEN", entity.getToken());

            int rowAffected = template.update(UPDATE_SESSION, namedParameters);
            if(rowAffected > 0){
                result= true;
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean updateStatus(SessionEntity entity) {
        boolean result = false;

        try {

            StringBuilder sql = new StringBuilder();
            sql.append(" update t_session set IS_ONLINE = :IS_ONLINE where TOKEN != :TOKEN and USER_ID = :USER_ID ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("TOKEN", entity.getToken())
                    .addValue("USER_ID", entity.getUserId())
                    .addValue("IS_ONLINE", "NO");

            int rowAffected = template.update(sql.toString(), namedParameters);
            if(rowAffected > 0){
                result= true;
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

}
