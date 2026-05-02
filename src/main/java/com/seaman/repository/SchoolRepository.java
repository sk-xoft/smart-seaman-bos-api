package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.SchoolEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class SchoolRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String SELECT_ALL = "SELECT * FROM m_companys where COMPANY_TYPE='Training School'";

    private static final String SELECT_SCHOOL = "SELECT * FROM m_companys where COMPANY_CODE=:COMPANY_CODE";

    public List<SchoolEntity> findAll(String companyType,String companyCode) {
        List<SchoolEntity> listAll = null;
        try {

            if(companyType.equals("Training School")){
                MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                        .addValue("COMPANY_CODE",companyCode);
                listAll = template.query(SELECT_SCHOOL, namedParameters, new BeanPropertyRowMapper(SchoolEntity.class));
                log.info("listAllTraining:"+listAll);
            }else{
                MapSqlParameterSource namedParameters = new MapSqlParameterSource();
                listAll = template.query(SELECT_ALL, namedParameters, new BeanPropertyRowMapper(SchoolEntity.class));
                log.info("listAll:"+listAll);
            }

            log.info("Init load master schools.");
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }
}
