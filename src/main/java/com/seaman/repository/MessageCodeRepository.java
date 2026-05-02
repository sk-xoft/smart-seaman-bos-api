package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.constant.BusinessConstant;
import com.seaman.entity.MessageCodeEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MessageCodeRepository extends CommonRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String SELECT_ALL = "select * from m_message_code";

    public List<MessageCodeEntity> findAll() {
        List<MessageCodeEntity> listAll = new ArrayList<>();
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listAll = template.query(SELECT_ALL, namedParameters, new BeanPropertyRowMapper(MessageCodeEntity.class));
            log.info("Init load master message code.");
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }
}
