package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.AdminUserEntity;
import com.seaman.entity.FormEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.AdminRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminRepository extends CommonRepository {

    private final PasswordEncoder passwordEncoder;

    private static final String FIND_BY_ID = "select * from m_admin_users where admin_user_id = :ID ";

    private static final String INSERT_ADMIN = "insert into m_admin_users " +
            "(ADMIN_UUID, GROUP_ID, USERNAME,PASSWORD,FIRST_NAME,LAST_NAME,COMPANY_CODE,POSITIONS, " +
            " EMAIL, MOBILE_NUMBER, DISPLAY_TYPE,DISPLAY_NAME, PROFILE_PICTURE, USER_STATUS,CREATE_DATE, CREATE_BY) " +
            " values (:ADMIN_UUID, :GROUP_ID, :USERNAME,:PASSWORD,:FIRST_NAME,:LAST_NAME,:COMPANY_CODE,:POSITIONS, " +
            " :EMAIL, :MOBILE_NUMBER, :DISPLAY_TYPE,:DISPLAY_NAME, :PROFILE_PICTURE, :USER_STATUS,:CREATE_DATE, :CREATE_BY)";
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public AdminUserEntity findById(Integer id) {

        List<AdminUserEntity> listRecord = null;

        AdminUserEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("ID", id);

            listRecord = template.query(FIND_BY_ID, namedParameters, new BeanPropertyRowMapper(AdminUserEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    private static final String SELECT_ALL_ADMIN = "SELECT m_admin_users.*,m_groups.group_name as admin_role_name," +
            "m_companys.COMPANY_FULL_NAME_EN as admin_company  FROM m_admin_users " +
            "left outer join m_groups  on m_admin_users.GROUP_ID = m_groups.GROUP_ID " +
            "left outer join m_companys on m_admin_users.COMPANY_CODE= m_companys.COMPANY_CODE " +
            "where 1>0 ";
    public List<AdminUserEntity> listAllAdmin(Integer start, Integer row, AdminRequest request) {
        List<AdminUserEntity> listAll = null;
        String strWhere = "";
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW", row);

            if (request.getAdminUserId() != null) {
                namedParameters.addValue("ADMIN_USER_ID", request.getAdminUserId());
                strWhere = strWhere + " and m_admin_users.ADMIN_USER_ID=:ADMIN_USER_ID ";
            }
            if (request.getGroupId() != null) {
                namedParameters.addValue("GROUP_ID", request.getGroupId());
                strWhere = strWhere + " and m_admin_users.GROUP_ID=:GROUP_ID ";
            }
            if (request.getCompanyCode() != null) {
                namedParameters.addValue("COMPANY_CODE", request.getCompanyCode());
                strWhere = strWhere + " and m_admin_users.COMPANY_CODE=:COMPANY_CODE ";
            }

            if (request.getUsername() != null) {
                namedParameters.addValue("USERNAME", "%" + request.getUsername() + "%");
                strWhere = strWhere + " and m_admin_users.USERNAME like :USERNAME ";
            }

            if (request.getUserStatus() != null) {
                namedParameters.addValue("USER_STATUS", request.getUserStatus());
                strWhere = strWhere + " and m_admin_users.USER_STATUS=:USER_STATUS ";
            }

            strWhere = strWhere + " LIMIT :START,:ROW;";

            listAll = template.query(SELECT_ALL_ADMIN + strWhere, namedParameters, new BeanPropertyRowMapper(AdminUserEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    private static final String SELECT_ALL_ADMIN_COUNT = "SELECT m_admin_users.*,m_groups.group_name as admin_role_name," +
            "m_companys.COMPANY_FULL_NAME_EN as admin_company  FROM m_admin_users " +
            "left outer join m_groups  on m_admin_users.GROUP_ID = m_groups.GROUP_ID " +
            "left outer join m_companys on m_admin_users.COMPANY_CODE= m_companys.COMPANY_CODE " +
            "where 1>0 ";
    public List<AdminUserEntity> listAllAdminCount(Integer start, Integer row, AdminRequest request) {
        List<AdminUserEntity> listAll = null;
        String strWhere = "";
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW", row);

            if (request.getAdminUserId() != null) {
                namedParameters.addValue("ADMIN_USER_ID", request.getAdminUserId());
                strWhere = strWhere + " and m_admin_users.ADMIN_USER_ID=:ADMIN_USER_ID ";
            }
            if (request.getGroupId() != null) {
                namedParameters.addValue("GROUP_ID", request.getGroupId());
                strWhere = strWhere + " and m_admin_users.GROUP_ID=:GROUP_ID ";
            }
            if (request.getCompanyCode() != null) {
                namedParameters.addValue("COMPANY_CODE", request.getCompanyCode());
                strWhere = strWhere + " and m_admin_users.COMPANY_CODE=:COMPANY_CODE ";
            }

            if (request.getUsername() != null) {
                namedParameters.addValue("USERNAME", "%" + request.getUsername() + "%");
                strWhere = strWhere + " and m_admin_users.USERNAME like :USERNAME ";
            }

            if (request.getUserStatus() != null) {
                namedParameters.addValue("USER_STATUS", request.getUserStatus());
                strWhere = strWhere + " and m_admin_users.USER_STATUS=:USER_STATUS ";
            }



            listAll = template.query(SELECT_ALL_ADMIN + strWhere, namedParameters, new BeanPropertyRowMapper(AdminUserEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public Integer getTotalData() {
        Integer maxMember;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject("SELECT count(ADMIN_USER_ID) FROM m_admin_users where user_status='A' ;", namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember = (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    public AdminUserEntity listAdminById(String adminUserId) {
        List<AdminUserEntity> listRecord = null;

        AdminUserEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("ADMIN_USER_ID", adminUserId);

            listRecord = template.query(SELECT_ALL_ADMIN + " and ADMIN_USER_ID=:ADMIN_USER_ID", namedParameters, new BeanPropertyRowMapper(AdminUserEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            } else {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Admin User ID does not exist");
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean insertAdmin(AdminUserEntity entity) {
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
                    .addValue("POSITIONS", entity.getPositions())
                    .addValue("EMAIL", entity.getEmail())
                    .addValue("MOBILE_NUMBER", entity.getMobileNumber())
                    .addValue("DISPLAY_TYPE", entity.getDisplayType())
                    .addValue("DISPLAY_NAME", entity.getDisplayName())
                    .addValue("PROFILE_PICTURE", entity.getProfilePicture())
                    .addValue("USER_STATUS", entity.getUserStatus())
                    .addValue("CREATE_DATE", entity.getCreateDate())
                    .addValue("CREATE_BY", entity.getCreateBy());

            int rowAffected = template.update(INSERT_ADMIN, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public Integer getMaxId() {
        Integer maxId;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxId = template.queryForObject("select max(admin_user_id) from m_admin_users where user_status='A' ", namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxId = (maxId == null) ? 1 : maxId;

        return maxId;
    }

    public boolean deleteAdmin(String id) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("ADMIN_USER_ID", id);
            int rowAffected = template.update("delete from m_admin_users where ADMIN_USER_ID=:ADMIN_USER_ID ", namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean updateAdmin(AdminRequest request, String username) {
        boolean result = false;
        try {
            String sql = "update m_admin_users set ";
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            if (request.getAdminUUID() != null) {
                namedParameters.addValue("ADMIN_UUID", request.getAdminUUID());
                sql = sql + " ADMIN_UUID=:ADMIN_UUID, ";
            }
            if (request.getGroupId() != null) {
                namedParameters.addValue("GROUP_ID", request.getGroupId());
                sql = sql + " GROUP_ID=:GROUP_ID, ";
            }
            if (request.getUsername() != null) {
                namedParameters.addValue("USERNAME", request.getUsername());
                sql = sql + " USERNAME=:USERNAME, ";
            }

//            if (request.getPassword() != null) {
//                namedParameters.addValue("PASSWORD", passwordEncoder.encode(request.getPassword()));
//                sql = sql + " PASSWORD=:PASSWORD, ";
//            }

            if (request.getFirstName() != null) {
                namedParameters.addValue("FIRST_NAME", request.getFirstName());
                sql = sql + " FIRST_NAME=:FIRST_NAME, ";
            }
            if (request.getLastName() != null) {
                namedParameters.addValue("LAST_NAME", request.getLastName());
                sql = sql + " LAST_NAME=:LAST_NAME, ";
            }
            if (request.getCompanyCode() != null) {
                namedParameters.addValue("COMPANY_CODE", request.getCompanyCode());
                sql = sql + " COMPANY_CODE=:COMPANY_CODE, ";
            }
            if (request.getPositions() != null) {
                namedParameters.addValue("POSITIONS", request.getPositions());
                sql = sql + " POSITIONS=:POSITIONS, ";
            }
            if (request.getEmail() != null) {
                namedParameters.addValue("EMAIL", request.getEmail());
                sql = sql + " EMAIL=:EMAIL, ";
            }
            if (request.getMobileNumber() != null) {
                namedParameters.addValue("MOBILE_NUMBER", request.getMobileNumber());
                sql = sql + " MOBILE_NUMBER=:MOBILE_NUMBER, ";
            }
            if (request.getDisplayType() != null) {
                namedParameters.addValue("DISPLAY_TYPE", request.getDisplayType());
                sql = sql + " DISPLAY_TYPE=:DISPLAY_TYPE, ";
            }
            if (request.getProfilePicture() != null) {
                namedParameters.addValue("PROFILE_PICTURE", request.getProfilePicture());
                sql = sql + " PROFILE_PICTURE=:PROFILE_PICTURE, ";
            }
            if (request.getUserStatus() != null) {
                namedParameters.addValue("USER_STATUS", request.getUserStatus());
                sql = sql + " USER_STATUS=:USER_STATUS, ";
            }

            sql = sql + " UPDATE_DATE=:UPDATE_DATE, UPDATE_BY=:UPDATE_BY where ADMIN_USER_ID=:ADMIN_USER_ID";
            namedParameters.addValue("UPDATE_DATE", new Date());
            namedParameters.addValue("UPDATE_BY", username);
            namedParameters.addValue("ADMIN_USER_ID", request.getAdminUserId());
            int rowAffected = template.update(sql, namedParameters);
            if (rowAffected > 0) {
                result = true;
            } else {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{adminUserId} does not exist");
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public Boolean listAdminByUsername(String username) {
        List<AdminUserEntity> listRecord = null;

        Boolean result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME", username);

            listRecord = template.query("SELECT * FROM m_admin_users where USERNAME=:USERNAME;",
                    namedParameters, new BeanPropertyRowMapper(FormEntity.class));

            if (listRecord != null && !listRecord.isEmpty()) {
                result = true;
            } else {
                result = false;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public AdminUserEntity getAdminByUsername(String username) {
        List<AdminUserEntity> listRecord = null;

        AdminUserEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME", username);

            listRecord = template.query("SELECT * FROM m_admin_users where USERNAME=:USERNAME;",
                    namedParameters, new BeanPropertyRowMapper(FormEntity.class));

            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public Boolean listPictureByName(String profilePicture) {
        List<AdminUserEntity> listRecord = null;
        Boolean result = null;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("PROFILE_PICTURE", profilePicture);
            listRecord = template.query("SELECT * FROM m_admin_users where PROFILE_PICTURE=:PROFILE_PICTURE;",
                    namedParameters, new BeanPropertyRowMapper(FormEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = true;
            } else {
                result = false;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }


}
