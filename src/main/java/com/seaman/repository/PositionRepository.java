package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.PositionsEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class PositionRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String SELECT_ALL = "select * from m_positions";

    public List<PositionsEntity> findAll() {
        List<PositionsEntity> listAll = null;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listAll = template.query(SELECT_ALL, namedParameters, new BeanPropertyRowMapper(PositionsEntity.class));
            log.info("Init load master position code.");
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public PositionsEntity findByCode(String code) {
        List<PositionsEntity> listAll = null;
        PositionsEntity item = new PositionsEntity();

        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_positions where POSITION_CODE = :POSITION_CODE ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("POSITION_CODE", code);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(PositionsEntity.class));
            if(!listAll.isEmpty()) {
                item = listAll.get(0);
            }
            log.info("Init load master position code.");
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return item;
    }


}
