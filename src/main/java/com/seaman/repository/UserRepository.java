package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.GroupEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepository extends CommonRepository {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String FIND_BY_USERNAME = "select * from m_admin_users where USERNAME = :USERNAME ";
    private static final String FIND_BY_EMAIL = "select * from m_admin_users where EMAIL = :EMAIL ";
    private static final String MAX_MEMBER_CODE =  "select MAX(CAST(SMART_SEAMAN_ID as DECIMAL) + 1) as max_member from m_admin_users";
    private static final String INSERT = "insert into m_admin_users (ADMIN_UUID, GROUP_ID , USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, COMPANY_CODE, POSITION_CODE, EMAIL, MOBILE_NUMBER, PROFILE_PICTURE, USER_STATUS, LAST_LOGON, CREATE_BY, " +
            " CREATE_DATE) " +
            " values (:ADMIN_UUID, :GROUP_ID, :USERNAME, :PASSWORD, :FIRST_NAME, :LAST_NAME,  :COMPANY_CODE, :POSITION_CODE, :EMAIL, :MOBILE_NUMBER, :PROFILE_PICTURE, :USER_STATUS, :LAST_LOGON, :CREATE_BY, :CREATE_DATE)";
    private static final String GET_GROUP_BY_USERNAME = "select group_id from m_admin_users where USERNAME = :USERNAME  ";

    private static final String GET_ALL_GROUP_BY_USERNAME = "select m_admin_users.group_id,group_name from m_admin_users left outer join m_groups on m_admin_users.group_id = m_groups.GROUP_ID where USERNAME = :USERNAME  ";
    public UsersEntity findByUsername(String username) {

        List<UsersEntity> listRecord = null;

        UsersEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME", username);

            listRecord = template.query(FIND_BY_USERNAME, namedParameters, new BeanPropertyRowMapper(UsersEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public UsersEntity findByEmail(String email) {

        List<UsersEntity> listRecord = null;
        UsersEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("EMAIL", email);

            listRecord = template.query(FIND_BY_EMAIL, namedParameters, new BeanPropertyRowMapper(UsersEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public Integer countMax() {

        Integer maxMember;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject(MAX_MEMBER_CODE, namedParameters, Integer.class);

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        maxMember  =  (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    public boolean insert(UsersEntity entity) {

        boolean result = false;
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("ADMIN_UUID", entity.getAdminUuid())
                    .addValue("GROUP_ID", entity.getGroupId())
                    .addValue("USERNAME", entity.getUsername())
                    .addValue("PASSWORD", entity.getPassword())
                    .addValue("FIRST_NAME", entity.getFirstName())
                    .addValue("LAST_NAME", entity.getLastName())
                    .addValue("COMPANY_CODE", entity.getCompanyCode())
                    .addValue("POSITION_CODE", entity.getPositions())
                    .addValue("EMAIL", entity.getEmail())
                    .addValue("MOBILE_NUMBER", entity.getMobileNumber())
                    .addValue("PROFILE_PICTURE", entity.getProfilePicture())
                    .addValue("USER_STATUS", entity.getUserStatus())
                    .addValue("LAST_LOGON", entity.getLastLogin())
                    .addValue("CREATE_BY", entity.getCreateBy())
                    .addValue("CREATE_DATE", entity.getUpdateDate());

            int rowAffected = template.update(INSERT, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public String getGroupByUsername(String username){
        String result="";



        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME",username);
            result = template.queryForObject(GET_GROUP_BY_USERNAME, namedParameters, String.class);

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;

    }

    public GroupEntity getAllGroupByUsername(String username){


        List<GroupEntity> listRecord = null;

        GroupEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME",username);


            listRecord = template.query(GET_ALL_GROUP_BY_USERNAME, namedParameters, new BeanPropertyRowMapper(GroupEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;

    }

    public boolean changePassword(UsersEntity entity) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append(" update m_admin_users set PASSWORD = :PASSWORD where ADMIN_UUID = :ADMIN_UUID ");
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("ADMIN_UUID", entity.getAdminUuid())
                    .addValue("PASSWORD", entity.getPassword());

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

    public boolean updateLastLogin(UsersEntity entity) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append(" update m_admin_users set LAST_LOGON = NOW() where ADMIN_UUID = :ADMIN_UUID ");
        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("ADMIN_UUID", entity.getAdminUuid());

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
}
