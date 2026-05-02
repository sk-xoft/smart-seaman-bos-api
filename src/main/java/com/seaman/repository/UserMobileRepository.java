package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.TokenFcmEntity;
import com.seaman.entity.UserMobileEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.MobileUserModel;
import com.seaman.model.request.UserMobileRequest;
import com.seaman.model.response.MobileUserRs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserMobileRepository extends CommonRepository {

    private static final String COUNT_ALL_USER = "SELECT count(MOBILE_USER_ID) FROM m_mobile_users " +
            "where user_status='A';";
    private static final String COUNT_ALL_USER_COMPANY_CODE = "SELECT count(MOBILE_USER_ID) FROM m_mobile_users " +
            "left outer join m_companys on m_mobile_users.COMPANY_CODE=m_companys.COMPANY_CODE " +
            "where user_status='A' and m_mobile_users.COMPANY_CODE=:COMPANY_CODE";

    // Commend by BOi
//    private static final String LESS_THAN_THREE = "SELECT CERT_ID, CERT_MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, " +
//            "com.COMPANY_NAME_EN,doc.DOCUMENT_NAME_TH,CERT_START_DATE,CERT_END_DATE, " +
//            "DATEDIFF(CERT_END_DATE, CERT_START_DATE) AS days " +
//            "from m_certificates crt " +
//            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
//            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
//            "left outer join m_documents doc on crt.CERT_DOCUMENT_CODE= doc.DOCUMENT_CODE " +
//            "where CERT_STATUS='A' HAVING days<=90 " +
//            "order by days DESC ";

    // Commend By BOi
//    private static final String LESS_THAN_THREE_COMPANY_CODE = "SELECT CERT_ID, CERT_MOBILE_UUID, " +
//            "usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, " +
//            "com.COMPANY_NAME_EN,doc.DOCUMENT_NAME_TH,CERT_START_DATE,CERT_END_DATE, " +
//            "DATEDIFF(CERT_END_DATE, CERT_START_DATE) AS days " +
//            "from m_certificates crt " +
//            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
//            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
//            "left outer join m_documents doc on crt.CERT_DOCUMENT_CODE= doc.DOCUMENT_CODE " +
//            "where CERT_STATUS='A' and usr.COMPANY_CODE=:COMPANY_CODE HAVING days<=90 " +
//            "order by days DESC ";


    // Commend by BOY
//    private static final String LESS_THAN_SIX = "SELECT CERT_ID, CERT_MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, " +
//            "com.COMPANY_NAME_EN,doc.DOCUMENT_NAME_TH,CERT_START_DATE,CERT_END_DATE, " +
//            "DATEDIFF(CERT_END_DATE, CERT_START_DATE) AS days " +
//            "from m_certificates crt " +
//            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
//            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
//            "left outer join m_documents doc on crt.CERT_DOCUMENT_CODE= doc.DOCUMENT_CODE " +
//            "where CERT_STATUS='A' HAVING days>90 and days<=180 " +
//            "order by days DESC ";

    // Commend By BOi
//    private static final String LESS_THAN_SIX_COMPANY_CODE = "SELECT CERT_ID, CERT_MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, " +
//            "com.COMPANY_NAME_EN,doc.DOCUMENT_NAME_TH,CERT_START_DATE,CERT_END_DATE, " +
//            "DATEDIFF(CERT_END_DATE, CERT_START_DATE) AS days " +
//            "from m_certificates crt " +
//            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
//            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
//            "left outer join m_documents doc on crt.CERT_DOCUMENT_CODE= doc.DOCUMENT_CODE " +
//            "where CERT_STATUS='A' and usr.COMPANY_CODE=:COMPANY_CODE HAVING days>90 and days<=180 " +
//            "order by days DESC ";

    /**
     * Comment by BOi.
    private static final String LESS_THAN_YEAR = "SELECT CERT_ID, CERT_MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, " +
            "com.COMPANY_NAME_EN,doc.DOCUMENT_NAME_TH,CERT_START_DATE,CERT_END_DATE, " +
            "DATEDIFF(CERT_END_DATE, CERT_START_DATE) AS days " +
            "from m_certificates crt " +
            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "left outer join m_documents doc on crt.CERT_DOCUMENT_CODE= doc.DOCUMENT_CODE " +
            "where CERT_STATUS='A' HAVING days>180 and days<=365 " +
            "order by days DESC ";
    private static final String LESS_THAN_YEAR_COMPANY_CODE = "SELECT CERT_ID, CERT_MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, " +
            "com.COMPANY_NAME_EN,doc.DOCUMENT_NAME_TH,CERT_START_DATE,CERT_END_DATE, " +
            "DATEDIFF(CERT_END_DATE, CERT_START_DATE) AS days " +
            "from m_certificates crt " +
            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "left outer join m_documents doc on crt.CERT_DOCUMENT_CODE= doc.DOCUMENT_CODE " +
            "where CERT_STATUS='A' and usr.COMPANY_CODE=:COMPANY_CODE HAVING days>180 and days<=365 " +
            "order by days DESC ";
    private static final String LIST_MOBILE_USER = "SELECT CERT_ID, CERT_MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, " +
            "com.COMPANY_NAME_EN,doc.DOCUMENT_NAME_TH,CERT_START_DATE,CERT_END_DATE, " +
            "DATEDIFF(CERT_END_DATE, CERT_START_DATE) AS days " +
            "from m_certificates crt " +
            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "left outer join m_documents doc on crt.CERT_DOCUMENT_CODE= doc.DOCUMENT_CODE " +
            "where CERT_STATUS='A' ";
    private static final String threeMonth = " HAVING days<=90 ";
    private static final String sixMonth = " HAVING days>90 and days<=180 ";
    private static final String yearMonth = " HAVING days>180 and days<=365 ";

    **/

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<UserMobileEntity> findUserMobileAll(Integer start, Integer row, UserMobileRequest request, String companyCode, boolean isSelectNull) {

        List<UserMobileEntity> listRecord = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select m_mobile_users.*, mc.COMPANY_NAME_EN, mc.COMPANY_NAME_TH, mp.POSITION_NAME_EN, mp.POSITION_NAME_TH from m_mobile_users ");
        sql.append(" left join m_companys mc on m_mobile_users.COMPANY_CODE = mc.COMPANY_CODE ");
        sql.append(" left join m_positions mp on m_mobile_users.POSITION_CODE = mp.POSITION_CODE ");
        sql.append(" where 1 > 0 ");
        sql.append(" and m_mobile_users.USER_STATUS = 'A' ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            if("".equals(request.getFirstName()) && "".equals(request.getLastName())  && "".equals(request.getSmartSeamanId())) {
                sql.append(" and 0 > 1 ");
            }

            if (isSelectNull) {

                if (!"".equals(request.getSmartSeamanId()) && null != request.getSmartSeamanId()) {
                    sql.append(" and SMART_SEAMAN_ID like :SMART_SEAMAN_ID ");
                    namedParameters.addValue("SMART_SEAMAN_ID",  request.getSmartSeamanId());

                    // sql.append(" and m_mobile_users.COMPANY_CODE is null ");
                } else {

                    if (!"".equals(request.getFirstName())) {
                        sql.append(" and FIRST_NAME like :FIRST_NAME ");
                        namedParameters.addValue("FIRST_NAME", "%" + request.getFirstName() + "%");
                    }

                    if (!"".equals(request.getLastName()) ) {
                        sql.append(" and LAST_NAME like :LAST_NAME ");
                        namedParameters.addValue("LAST_NAME", "%" + request.getLastName() + "%");
                    }
                }

            } else {

                if (!"".equals(request.getSmartSeamanId()) && null != request.getSmartSeamanId()) {

                    if (!"".equals(companyCode) && null != companyCode) {
                        sql.append(" and m_mobile_users.COMPANY_CODE = :COMPANY_CODE ");
                        namedParameters.addValue("COMPANY_CODE", companyCode);
                    }

                    sql.append(" and SMART_SEAMAN_ID like :SMART_SEAMAN_ID ");
                    namedParameters.addValue("SMART_SEAMAN_ID", request.getSmartSeamanId() );

                }  else {

                    if (!"".equals(request.getFirstName())) {
                        sql.append(" and FIRST_NAME like :FIRST_NAME ");
                        namedParameters.addValue("FIRST_NAME", "%" + request.getFirstName() + "%");
                    }

                    if (!"".equals(request.getLastName()) ) {
                        sql.append(" and LAST_NAME like :LAST_NAME ");
                        namedParameters.addValue("LAST_NAME", "%" + request.getLastName() + "%");
                    }

                    sql.append(" and m_mobile_users.COMPANY_CODE = :COMPANY_CODE ");
                    namedParameters.addValue("COMPANY_CODE", companyCode);

                }
            }

            sql.append(" order by FIRST_NAME LIMIT :START, :ROW ");
            namedParameters.addValue("START", start);
            namedParameters.addValue("ROW", row);

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(UserMobileEntity.class));

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return listRecord;
    }


    public List<UserMobileEntity> findUserMobileAllCount(Integer start, Integer row, UserMobileRequest request, String companyCode, boolean isSelectNull) {

        List<UserMobileEntity> listRecord = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select m_mobile_users.*, mc.COMPANY_NAME_EN, mc.COMPANY_NAME_TH, mp.POSITION_NAME_EN, mp.POSITION_NAME_TH from m_mobile_users ");
        sql.append(" left join m_companys mc on m_mobile_users.COMPANY_CODE = mc.COMPANY_CODE ");
        sql.append(" left join m_positions mp on m_mobile_users.POSITION_CODE = mp.POSITION_CODE ");
        sql.append(" where 1 > 0 ");
        sql.append(" and m_mobile_users.USER_STATUS = 'A' ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            if("".equals(request.getFirstName()) && "".equals(request.getLastName())  && "".equals(request.getSmartSeamanId())) {
                sql.append(" and 0 > 1 ");
            }

            if (isSelectNull) {

                if (!"".equals(request.getSmartSeamanId()) && null != request.getSmartSeamanId()) {
                    sql.append(" and SMART_SEAMAN_ID like :SMART_SEAMAN_ID ");
                    namedParameters.addValue("SMART_SEAMAN_ID",  request.getSmartSeamanId());

                    // sql.append(" and m_mobile_users.COMPANY_CODE is null ");
                } else {

                    if (!"".equals(request.getFirstName())) {
                        sql.append(" and FIRST_NAME like :FIRST_NAME ");
                        namedParameters.addValue("FIRST_NAME", "%" + request.getFirstName() + "%");
                    }

                    if (!"".equals(request.getLastName()) ) {
                        sql.append(" and LAST_NAME like :LAST_NAME ");
                        namedParameters.addValue("LAST_NAME", "%" + request.getLastName() + "%");
                    }
                }

            } else {

                if (!"".equals(request.getSmartSeamanId()) && null != request.getSmartSeamanId()) {

                    if (!"".equals(companyCode) && null != companyCode) {
                        sql.append(" and m_mobile_users.COMPANY_CODE = :COMPANY_CODE ");
                        namedParameters.addValue("COMPANY_CODE", companyCode);
                    }

                    sql.append(" and SMART_SEAMAN_ID like :SMART_SEAMAN_ID ");
                    namedParameters.addValue("SMART_SEAMAN_ID", request.getSmartSeamanId() );

                }  else {

                    if (!"".equals(request.getFirstName())) {
                        sql.append(" and FIRST_NAME like :FIRST_NAME ");
                        namedParameters.addValue("FIRST_NAME", "%" + request.getFirstName() + "%");
                    }

                    if (!"".equals(request.getLastName()) ) {
                        sql.append(" and LAST_NAME like :LAST_NAME ");
                        namedParameters.addValue("LAST_NAME", "%" + request.getLastName() + "%");
                    }

                    sql.append(" and m_mobile_users.COMPANY_CODE = :COMPANY_CODE ");
                    namedParameters.addValue("COMPANY_CODE", companyCode);

                }
            }

            sql.append(" order by FIRST_NAME ");
            namedParameters.addValue("START", start);
            namedParameters.addValue("ROW", row);

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(UserMobileEntity.class));

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return listRecord;
    }

    public UserMobileEntity findUserMobileByUUID(String uuid) {

        List<UserMobileEntity> listRecord = null;
        UserMobileEntity result = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select m_mobile_users.*, mc.COMPANY_NAME_EN, mc.COMPANY_NAME_TH, mp.POSITION_NAME_EN, mp.POSITION_NAME_TH from m_mobile_users ");
        sql.append(" left join m_companys mc on m_mobile_users.COMPANY_CODE = mc.COMPANY_CODE ");
        sql.append(" left join m_positions mp on m_mobile_users.POSITION_CODE = mp.POSITION_CODE ");
        sql.append(" where 1 > 0 ");
        sql.append(" and MOBILE_UUID like :MOBILE_UUID ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("MOBILE_UUID", uuid);
            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(UserMobileEntity.class));

            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public TokenFcmEntity findTokenUserMobile(String smartSeamanId) {

        List<TokenFcmEntity> listRecord = null;
        TokenFcmEntity result = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select TOKEN_FCM, MOBILE_UUID from m_mobile_users mbu ");
        sql.append(" inner join m_fcm_notifications mfn on USER_MOBILE_UUID = MOBILE_UUID where SMART_SEAMAN_ID = :SMART_SEAMAN_ID ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("SMART_SEAMAN_ID", smartSeamanId);

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(TokenFcmEntity.class));

            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }


    public Integer getTotalRecord(UserMobileRequest request, String companyCode, boolean isSelectNull) {
        Integer maxMember;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select count(MOBILE_USER_ID) as count_user  from m_mobile_users ");
            sql.append(" left join m_companys mc on m_mobile_users.COMPANY_CODE = mc.COMPANY_CODE ");
            sql.append(" left join m_positions mp on m_mobile_users.POSITION_CODE = mp.POSITION_CODE ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            if (!"".equals(request.getFirstName())) {
                sql.append(" and FIRST_NAME like :FIRST_NAME ");
                namedParameters.addValue("FIRST_NAME", "%" + request.getFirstName() + "%");
            }

            if (!"".equals(request.getLastName()) ) {
                sql.append(" and LAST_NAME like :LAST_NAME ");
                namedParameters.addValue("LAST_NAME", "%" + request.getLastName() + "%");
            }

            if("".equals(request.getFirstName()) && "".equals(request.getLastName())  && "".equals(request.getSmartSeamanId())) {
                sql.append(" and 0 > 1 ");
            }

            if (isSelectNull) {

                if (!"".equals(request.getSmartSeamanId()) && null != request.getSmartSeamanId()) {
                    sql.append(" and SMART_SEAMAN_ID like :SMART_SEAMAN_ID ");
                    namedParameters.addValue("SMART_SEAMAN_ID",  request.getSmartSeamanId());

                    sql.append(" and m_mobile_users.COMPANY_CODE is null ");
                }

            } else {

                if (!"".equals(request.getSmartSeamanId()) && null != request.getSmartSeamanId()) {

                    if (!"".equals(companyCode) && null != companyCode) {
                        sql.append(" and mc.COMPANY_CODE like :COMPANY_CODE ");
                        namedParameters.addValue("COMPANY_CODE", companyCode);
                    }

                    sql.append(" and SMART_SEAMAN_ID like :SMART_SEAMAN_ID ");
                    namedParameters.addValue("SMART_SEAMAN_ID", request.getSmartSeamanId() );
                }

            }

            maxMember = template.queryForObject(sql.toString(), namedParameters, Integer.class);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember = (maxMember == null) ? 1 : maxMember;
        return maxMember;
    }

    public Integer countAllUser() {
        Integer maxMember;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject(COUNT_ALL_USER, namedParameters, Integer.class);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember = (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    public Integer countAllUserByCompanyCode(String companyCode) {
        Integer maxMember;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("COMPANY_CODE", companyCode);
            maxMember = template.queryForObject(COUNT_ALL_USER_COMPANY_CODE, namedParameters, Integer.class);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember = (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }


    private static final String LESS_THAN_THREE ="SELECT  usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL from m_certificates crt " +
            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
            "where CERT_STATUS='A' and usr.USER_STATUS='A' " +
            "and DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') <=  DATE_FORMAT((NOW() + INTERVAL 3 MONTH), '%Y-%m-%d') " +
            "group by usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL  order by SMART_SEAMAN_ID";
    public Long getLessThanThreeMonth(){
        List<MobileUserRs> listRecord = null;

        Long result;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            listRecord = template.query(LESS_THAN_THREE, namedParameters, new BeanPropertyRowMapper(MobileUserRs.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.stream().count();

            } else {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "");
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    private static final String LESS_THAN_THREE_COMPANY_CODE = "SELECT  usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL from m_certificates crt " +
            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
            "where CERT_STATUS='A' and usr.USER_STATUS='A' " +
            "and DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') <=  DATE_FORMAT((NOW() + INTERVAL 3 MONTH), '%Y-%m-%d') and usr.COMPANY_CODE=:COMPANY_CODE " +
            "group by usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL order by SMART_SEAMAN_ID";
    public Long getLessThanThreeMonthByCompanyCode(String companyCode){
        List<MobileUserRs> listRecord = null;

        Long result;
        logger.info("getLessThanThreeMonthByCompanyCode:" + LESS_THAN_THREE_COMPANY_CODE);
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()

                    .addValue("COMPANY_CODE", companyCode);

            listRecord = template.query(LESS_THAN_THREE_COMPANY_CODE, namedParameters, new BeanPropertyRowMapper(MobileUserRs.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.stream().count();

            } else {
                result = Long.valueOf(0);
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }


    private static final String LESS_THAN_SIX ="SELECT  usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
        "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL from m_certificates crt " +
        "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
        "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
        "left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
        "where CERT_STATUS='A' and usr.USER_STATUS='A' " +
            "and DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') <=  DATE_FORMAT((NOW() + INTERVAL 6 MONTH), '%Y-%m-%d')  " +
        "group by usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
        "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL order by SMART_SEAMAN_ID " +
        "\n";
    public Long getLessThanSixMonth(){
        List<MobileUserRs> listRecord = null;

        Long result;
//logger.info("SQL less than six:" +LESS_THAN_SIX );
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            listRecord = template.query(LESS_THAN_SIX, namedParameters, new BeanPropertyRowMapper(MobileUserRs.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.stream().count();

            } else {
                result = Long.valueOf(0);
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }


    private static final String LESS_THAN_SIX_COMPANY_CODE = "SELECT  usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL from m_certificates crt " +
            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
            "where CERT_STATUS='A' and usr.USER_STATUS='A' " +
            "and DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') <=  DATE_FORMAT((NOW() + INTERVAL 6 MONTH), '%Y-%m-%d') and usr.COMPANY_CODE=:COMPANY_CODE  " +
            "group by usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL order by SMART_SEAMAN_ID " +
            "\n";
    public Long getLessThanSixMonthByCompanyCode(String companyCode){
        List<MobileUserRs> listRecord = null;

        Long result;
//logger.info("SQL less than six:" +LESS_THAN_SIX );
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("COMPANY_CODE", companyCode);


            listRecord = template.query(LESS_THAN_SIX_COMPANY_CODE, namedParameters, new BeanPropertyRowMapper(MobileUserRs.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.stream().count();

            } else {
                result = Long.valueOf(0);
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

private static final String LESS_THAN_YEAR ="SELECT  usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
        "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL from m_certificates crt " +
        "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
        "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
        "left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
        "where CERT_STATUS='A' and  usr.USER_STATUS='A' " +
        "and DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') <=  DATE_FORMAT((NOW() + INTERVAL 1 YEAR), '%Y-%m-%d') " +
        "group by usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
        "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL order by SMART_SEAMAN_ID " +
        "\n";
    public Long getLessThanYear(){
        List<MobileUserRs> listRecord = null;

        Long result;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            listRecord = template.query(LESS_THAN_YEAR, namedParameters, new BeanPropertyRowMapper(MobileUserRs.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.stream().count();

            } else {
                result = Long.valueOf(0);
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }



    private static final String LESS_THAN_YEAR_COMPANY_CODE ="SELECT  usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL from m_certificates crt " +
            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
            "where CERT_STATUS='A' and usr.USER_STATUS='A' and DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') <=  DATE_FORMAT((NOW() + INTERVAL 1 YEAR), '%Y-%m-%d')  and usr.COMPANY_CODE=:COMPANY_CODE  " +
            "group by usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL order by SMART_SEAMAN_ID";
    public Long getLessThanYearByCompanyCode(String companyCode){
        List<MobileUserRs> listRecord = null;

        Long result;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("COMPANY_CODE", companyCode);

            listRecord = template.query(LESS_THAN_YEAR_COMPANY_CODE, namedParameters, new BeanPropertyRowMapper(MobileUserRs.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.stream().count();

            } else {
                result = Long.valueOf(0);
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    private static final String LIST_MOBILE_USER ="SELECT  usr.MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL from m_certificates crt " +
            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
            "where CERT_STATUS='A'  and usr.USER_STATUS='A'  " ;

    private static final String LIST_MOBILE_USER_COUNT ="SELECT  count(usr.MOBILE_UUID)  from m_certificates crt " +
            "left outer join m_mobile_users usr on crt.CERT_MOBILE_UUID = usr.MOBILE_UUID " +
            "left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
            "where CERT_STATUS='A'  and usr.USER_STATUS='A'  " ;

    private static final String LIST_MOBILE_USER_NOT_CERT = " SELECT  usr.MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
            "        com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL " +
            " from  m_mobile_users usr " +
            "    left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "    left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
            " where usr.USER_STATUS = 'A' ";

    private static final String LIST_MOBILE_USER_NOT_CERT_COUNT = " SELECT  count(usr.MOBILE_UUID) from  m_mobile_users usr " +
            "    left outer join m_companys com on usr.COMPANY_CODE=com.COMPANY_CODE " +
            "    left outer join m_positions pos on usr.POSITION_CODE=pos.POSITION_CODE " +
            " where usr.USER_STATUS = 'A' ";
    private static final String threeMonth = " and DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') <=  DATE_FORMAT((NOW() + INTERVAL 3 MONTH), '%Y-%m-%d') " ;
    private static final String sixMonth = " and DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') <=  DATE_FORMAT((NOW() + INTERVAL 6 MONTH), '%Y-%m-%d')  ";
    private static final String yearMonth ="  and DATE_FORMAT(CERT_END_DATE, '%Y-%m-%d') <=  DATE_FORMAT((NOW() + INTERVAL 1 YEAR), '%Y-%m-%d') ";

    public List<MobileUserModel> listMobileUser(String month, String companyCode, Integer start, Integer row){
        List<MobileUserModel> listRecord = null;
        String sql = LIST_MOBILE_USER;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("START", start);
            namedParameters.addValue("ROW", row);

            if (month.equals("3")) {
                sql = sql + threeMonth;
            } else if (month.equals("6")) {
                sql = sql + sixMonth;
            } else if (month.equals("12")) {
                sql = sql + yearMonth;
            } else {
                sql = LIST_MOBILE_USER_NOT_CERT;
            }

            if (companyCode != null) {
                namedParameters.addValue("COMPANY_CODE", companyCode);
                sql = sql + " and usr.COMPANY_CODE=:COMPANY_CODE ";
            }

            sql = sql +" group by usr.MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
                    "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL order by SMART_SEAMAN_ID ";

            sql = sql + " LIMIT :START, :ROW ";

            listRecord = template.query(sql , namedParameters, new BeanPropertyRowMapper(MobileUserModel.class));

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listRecord;
    }

    public List<MobileUserRs> listSmartSeamanCode(String search){
        List<MobileUserRs> listRecord = null;

        try {
            StringBuilder sql =  new StringBuilder();
            sql.append(" select * from m_mobile_users where 1 > 0 ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            if (search != null) {
                sql.append(" AND SMART_SEAMAN_ID like :SMART_SEAMAN_ID ");
                namedParameters.addValue("SMART_SEAMAN_ID", "%" + search + "%");
            }

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(MobileUserRs.class));

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listRecord;
    }

    public Integer mobileUserCount(String month, String companyCode, Integer start, Integer row) {

        Integer totalCount = 0;
        List<MobileUserModel> listRecord = null;
        String sql = LIST_MOBILE_USER;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("START", start);
            namedParameters.addValue("ROW", row);

            if (month.equals("3")) {
                sql = sql + threeMonth;
            } else if (month.equals("6")) {
                sql = sql + sixMonth;
            } else if (month.equals("12")) {
                sql = sql + yearMonth;
            } else {
                sql = LIST_MOBILE_USER_NOT_CERT_COUNT;
            }

            if (companyCode != null) {
                namedParameters.addValue("COMPANY_CODE", companyCode);
                sql = sql + " and usr.COMPANY_CODE=:COMPANY_CODE ";
            }

            sql = sql +" group by usr.MOBILE_UUID,usr.SMART_SEAMAN_ID, usr.FIRST_NAME, usr.LAST_NAME, pos.POSITION_NAME_EN, " +
                    "com.COMPANY_NAME_EN, usr.MOBILE_NUMBER, usr.EMAIL order by SMART_SEAMAN_ID ";

            listRecord = template.query(sql , namedParameters, new BeanPropertyRowMapper(MobileUserModel.class));
            totalCount = listRecord.size();

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return totalCount;
    }
}