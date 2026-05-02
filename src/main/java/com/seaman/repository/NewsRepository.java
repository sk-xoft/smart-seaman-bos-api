package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.NewsEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.NewsRq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class NewsRepository extends CommonRepository {
    private static final String SELECT_ALL_NEWS = "SELECT * FROM m_news ";
    private static final String INSERT_NEWS = "insert into m_news " +
            "(NEWS_TITLE, NEWS_PICTURE_FILE_NAME, NEWS_TYPE,NEWS_DETAILS,NEWS_STATUS,CREATE_DATE, CREATE_BY  ) " +
            " values (:NEWS_TITLE, :NEWS_PICTURE_FILE_NAME, :NEWS_TYPE, :NEWS_DETAILS,:NEWS_STATUS, NOW(), :CREATE_BY ) ";
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<NewsEntity> listAllNews(Integer start, Integer row, NewsRq request) {
        List<NewsEntity> listAll = null;
        String sql = SELECT_ALL_NEWS;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW", row);

            if (request.getNewsType() != null & request.getNewsTitle() != null) {
                namedParameters.addValue("NEWS_TYPE", request.getNewsType());
                namedParameters.addValue("NEWS_TITLE", request.getNewsTitle());
                sql = sql + " where NEWS_TYPE=:NEWS_TYPE AND NEWS_TITLE LIKE '%'||:NEWS_TITLE||'%' ";
            } else if (request.getNewsType() != null & request.getNewsTitle() == null) {
                namedParameters.addValue("NEWS_TYPE", request.getNewsType());
                sql = sql + " where NEWS_TYPE LIKE '%'||:NEWS_TYPE||'%' ";
            } else if (request.getNewsType() == null & request.getNewsTitle() != null) {
                namedParameters.addValue("NEWS_TITLE", request.getNewsTitle());
                sql = sql + " where NEWS_TITLE LIKE '%'||:NEWS_TITLE||'%' ";
            }

            sql = sql + " LIMIT :START,:ROW;";


            log.info("sql-list-news:" + sql);
            listAll = template.query(sql, namedParameters, new BeanPropertyRowMapper(NewsEntity.class));
            log.info("Init list all News." + listAll);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<NewsEntity> listAllNewsCount(Integer start, Integer row, NewsRq request) {
        List<NewsEntity> listAll = null;
        String sql = SELECT_ALL_NEWS;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW", row);

            if (request.getNewsType() != null & request.getNewsTitle() != null) {
                namedParameters.addValue("NEWS_TYPE", request.getNewsType());
                namedParameters.addValue("NEWS_TITLE", request.getNewsTitle());
                sql = sql + " where NEWS_TYPE=:NEWS_TYPE AND NEWS_TITLE LIKE '%'||:NEWS_TITLE||'%' ";
            } else if (request.getNewsType() != null & request.getNewsTitle() == null) {
                namedParameters.addValue("NEWS_TYPE", request.getNewsType());
                sql = sql + " where NEWS_TYPE LIKE '%'||:NEWS_TYPE||'%' ";
            } else if (request.getNewsType() == null & request.getNewsTitle() != null) {
                namedParameters.addValue("NEWS_TITLE", request.getNewsTitle());
                sql = sql + " where NEWS_TITLE LIKE '%'||:NEWS_TITLE||'%' ";
            }



            log.info("sql-list-news:" + sql);
            listAll = template.query(sql, namedParameters, new BeanPropertyRowMapper(NewsEntity.class));
            log.info("Init list all News." + listAll);
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
            maxId = template.queryForObject("select max(news_id) from m_news", namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxId = (maxId == null) ? 1 : maxId;

        return maxId;
    }

    public Integer getTotalRecord() {
        Integer maxMember;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject("SELECT count(NEWS_ID) FROM m_news ;", namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember = (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    public int insertNews(NewsEntity entity) {

        int result = 0;
        try {

            // The GeneratedKeyHolder object is used to get the auto-incrementing ID.
            GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("NEWS_TITLE", entity.getNewsTitle())
                    .addValue("NEWS_PICTURE_FILE_NAME", entity.getNewsPictureFileName())
                    .addValue("NEWS_TYPE", entity.getNewsType())
                    .addValue("NEWS_DETAILS", entity.getNewsDetails())
                    .addValue("NEWS_STATUS", entity.getNewsStatus())
                    // .addValue("CREATE_DATE", entity.getCreateDate())
                    .addValue("CREATE_BY", entity.getCreateBy());

            int rowAffected = template.update(INSERT_NEWS, namedParameters, generatedKeyHolder);

            if (rowAffected == 0) {
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

    public boolean deleteNews(String id) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("NEWS_ID", id);
            int rowAffected = template.update("delete from m_news where NEWS_ID=:NEWS_ID", namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean updateNews(NewsRq request, String username) {
        boolean result = false;

        try {

            // The GeneratedKeyHolder object is used to get the auto-incrementing ID.
            GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

            String sql = "update m_news set ";

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            if (request.getNewsDetails() != null) {
                namedParameters.addValue("NEWS_DETAILS", request.getNewsDetails());
                sql = sql + " NEWS_DETAILS=:NEWS_DETAILS, ";
            }

            if (request.getNewsPictureFileName() != null) {
                namedParameters.addValue("NEWS_PICTURE_FILE_NAME", request.getNewsPictureFileName());
                sql = sql + " NEWS_PICTURE_FILE_NAME=:NEWS_PICTURE_FILE_NAME, ";
            }
            if (request.getNewsTitle() != null) {
                namedParameters.addValue("NEWS_TITLE", request.getNewsTitle());
                sql = sql + " NEWS_TITLE=:NEWS_TITLE, ";
            }
            if (request.getNewsType() != null) {
                namedParameters.addValue("NEWS_TYPE", request.getNewsType());
                sql = sql + " NEWS_TYPE=:NEWS_TYPE, ";
            }
            if (request.getNewsStatus() != null) {
                namedParameters.addValue("NEWS_STATUS", request.getNewsStatus());
                sql = sql + " NEWS_STATUS=:NEWS_STATUS, ";
            }

            sql = sql + " UPDATE_DATE=NOW() , UPDATE_BY=:UPDATE_BY where NEWS_ID=:NEWS_ID";
//            namedParameters.addValue("UPDATE_DATE", new Date());
            namedParameters.addValue("UPDATE_BY", username);
            namedParameters.addValue("NEWS_ID", request.getNewsId());

            int rowAffected = template.update(sql, namedParameters);

            if (rowAffected > 0) {
                result = true;
            } else {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{newsId} does not exist");
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public NewsEntity listById(String id) {
        List<NewsEntity> listRecord = null;

        NewsEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("NEWS_ID", id);

            listRecord = template.query("SELECT * FROM m_news where NEWS_ID=:NEWS_ID;",
                    namedParameters,
                    new BeanPropertyRowMapper(NewsEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            } else {
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{newsId} does not exist");
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public Boolean listNewsPicByFileName(String fileName) {
        List<NewsEntity> listRecord = null;

        Boolean result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("NEWS_PICTURE_FILE_NAME", fileName);

            listRecord = template.query("SELECT * FROM m_news where NEWS_PICTURE_FILE_NAME=:NEWS_PICTURE_FILE_NAME;",
                    namedParameters, new BeanPropertyRowMapper(NewsEntity.class));
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
