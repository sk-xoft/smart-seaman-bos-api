package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.BannerEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BannerRepository extends CommonRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private static final String INSERT =  "insert into m_banners " +
            "(BANNER_NAME, BANNER_FILE_NAME, BANNER_SEQ,CREATE_DATE, CREATE_BY  ) " +
            " values (:BANNER_NAME, :BANNER_FILE_NAME, :BANNER_SEQ, :CREATE_DATE, :CREATE_BY ) ";

    private static final String SELECT_ALL= "SELECT * FROM m_banners LIMIT :START,:ROW;";
    public List<BannerEntity> listAllBanner(Integer start, Integer row ) {
        List<BannerEntity> listAll = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW",row);

            //log.info("sql-list-news:" + SELECT_ALL_NEWS);
            listAll = template.query(SELECT_ALL, namedParameters, new BeanPropertyRowMapper(BannerEntity.class));
            // log.info("Init list all News." + listAll);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public Integer getMaxId() {
        Integer maxId;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxId = template.queryForObject("select max(BANNER_ID) from m_banners", namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxId  =  (maxId == null) ? 1 : maxId;

        return maxId;
    }

    public Integer getTotalRecord() {
        Integer maxMember;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject("SELECT count(BANNER_ID) FROM m_banners ;", namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember  =  (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    public boolean insertBanner(BannerEntity entity) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("BANNER_NAME", entity.getBannerName())
                    .addValue("BANNER_FILE_NAME", entity.getBannerFileName())
                    .addValue("BANNER_SEQ",entity.getBannerSeq())
                    .addValue("CREATE_DATE", entity.getCreateDate())
                    .addValue("CREATE_BY", entity.getCreateBy());

            int rowAffected = template.update(INSERT, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean deleteBanner(String id) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("BANNER_ID",id);
            int rowAffected = template.update("delete from m_banners where BANNER_ID=:BANNER_ID", namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public Boolean listBannerByFileName(String fileName) {
        List<BannerEntity> listRecord = null;

        Boolean result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("BANNER_FILE_NAME", fileName);

            listRecord = template.query("SELECT * FROM m_banners where BANNER_FILE_NAME=:BANNER_FILE_NAME;",
                    namedParameters, new BeanPropertyRowMapper(BannerEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = true;
            }else{
                result = false;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public BannerEntity listById(String id) {
        List<BannerEntity> listRecord = null;

        BannerEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("BANNER_ID", id);

            listRecord = template.query("SELECT * FROM m_banners where BANNER_ID=:BANNER_ID;",
                    namedParameters,
                    new BeanPropertyRowMapper(BannerEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{banner_id} does not exist");
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

}
