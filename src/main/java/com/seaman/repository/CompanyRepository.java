package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.CompanyEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class CompanyRepository extends CommonRepository{
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String SELECT_ALL = "select * from m_companys";

    private static final String FIND_COMPANY_CODE = "select company_type,m_admin_users.company_code from m_admin_users " +
            "left outer join m_companys on m_admin_users.COMPANY_CODE = m_companys.COMPANY_CODE   " +
            "where username = :USERNAME ";

    public List<CompanyEntity> findAll() {
        List<CompanyEntity> listAll = null;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listAll = template.query(SELECT_ALL, namedParameters, new BeanPropertyRowMapper(CompanyEntity.class));
            log.info("Init load master company code.");
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public CompanyEntity getCompanyCode(String username) {

        List<CompanyEntity> listAll = null;
        CompanyEntity result = null;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME", username);
            listAll = template.query(FIND_COMPANY_CODE, namedParameters, new BeanPropertyRowMapper(CompanyEntity.class));
            if (listAll != null && !listAll.isEmpty()) {
                result = listAll.get(0);
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }


        return result;
    }

    private static final String GET_COMPANY_TYPE="SELECT company_type FROM m_admin_users " +
            "left outer join m_companys on m_admin_users.company_code=m_companys.COMPANY_CODE " +
            "where username=:USERNAME;";

    public String getCompanyType(String username){
        String result = "";
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME",username);
            result = template.queryForObject(GET_COMPANY_TYPE, namedParameters, String.class);

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;

    }

    public CompanyEntity findByCode(String var) {
        List<CompanyEntity> listAll = null;
        CompanyEntity companyEntity = null;
        StringBuilder sql = new StringBuilder();
        sql.append("select * from m_companys where COMPANY_CODE = :COMPANY_CODE");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("COMPANY_CODE", var);


            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(CompanyEntity.class));
            if(!listAll.isEmpty()){
                companyEntity =  listAll.get(0);
            }
            log.info("Init load master company code.");
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return companyEntity;
    }

}
