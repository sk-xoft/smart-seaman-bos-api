package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.CourseNameEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.entity.FormEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.FormRq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
public class DocumentRepository  extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String SELECT_ALL_FORM = "SELECT * FROM m_forms LIMIT :START,:ROW;";

    private static final String INSERT_FORM =  "insert into m_forms " +
            "(FORM_NAME, FORM_FILE_NAME, CREATE_DATE,CREATE_BY ) " +
            " values (:FORM_NAME, :FORM_FILE_NAME, :CREATE_DATE, :CREATE_BY) ";
    public List<CourseNameEntity> listCourseName() {
        List<CourseNameEntity> listAll = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT COURSE_CODE, CONCAT(COURSE_NAME_TH, ' / ', COURSE_NAME_EN) AS COURSE_DISPLAY_NAME, COURSE_NAME_TH, COURSE_NAME_EN " +
                "FROM m_course_name " +
                "WHERE COURSE_STATUS = 'A' " +
                "ORDER BY COURSE_SEQ; ");
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            listAll = template.query(sql.toString() , namedParameters, new BeanPropertyRowMapper(CourseNameEntity.class));
            log.info("listAllDocument:" + listAll);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<FormEntity> listAllForm(Integer start, Integer row ) {
        List<FormEntity> listAll = null;
        String sql= SELECT_ALL_FORM;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW",row);

            log.info("sql-list-form:" + SELECT_ALL_FORM);
            listAll = template.query(SELECT_ALL_FORM, namedParameters, new BeanPropertyRowMapper(FormEntity.class));
            log.info("Init list all Form." + listAll);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public Integer getTotalForm() {
        Integer maxMember;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject("SELECT count(FORM_ID) FROM m_forms ;", namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember  =  (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    public boolean insertForm(FormEntity entity) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("FORM_NAME", entity.getFormName())
                    .addValue("FORM_FILE_NAME", entity.getFormFileName())
                    .addValue("CREATE_DATE", entity.getCreateDate())
                    .addValue("CREATE_BY", entity.getCreateBy());

            int rowAffected = template.update(INSERT_FORM, namedParameters);
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
            maxId = template.queryForObject("select max(form_id) from m_forms", namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxId  =  (maxId == null) ? 1 : maxId;

        return maxId;
    }

    public FormEntity listFormById(String formId) {
        List<FormEntity> listRecord = null;

        FormEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("FORM_ID", formId);

            listRecord = template.query("SELECT * FROM m_forms where form_id= :FORM_ID", namedParameters, new BeanPropertyRowMapper(FormEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{formId} does not exist");
            }
            log.info("Init list list Form by ID. Result ID -> {}", result.getFormId());
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean deleteForm(String formId) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("FORM_ID",formId);
            int rowAffected = template.update("delete from m_forms where FORM_ID=:FORM_ID", namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean updateForm(FormRq form, String username) {
        boolean result = false;
        try {
            String sql = "update m_forms set ";
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            if (form.getFormName()!=null){
                namedParameters.addValue("FORM_NAME", form.getFormName());
                sql = sql + " FORM_NAME=:FORM_NAME, ";
            }

            if(form.getFormFileName()!=null){
                namedParameters.addValue("FORM_FILE_NAME", form.getFormFileName());
                sql = sql + " FORM_FILE_NAME=:FORM_FILE_NAME, ";
            }
            sql = sql + " UPDATE_DATE=:UPDATE_DATE, UPDATE_BY=:UPDATE_BY where FORM_ID=:FORM_ID";
            namedParameters.addValue("UPDATE_DATE",new Date());
            namedParameters.addValue("UPDATE_BY",username);
            namedParameters.addValue("FORM_ID",form.getFormId());
            log.info("SQL:"+sql);
            int rowAffected = template.update(sql, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{formId} does not exist");
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public Boolean listFormByFormFileName(String fromFileName) {
        List<FormEntity> listRecord = null;

        Boolean result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("FORM_FILE_NAME", fromFileName);

            listRecord = template.query("SELECT * FROM m_forms where FORM_FILE_NAME=:FORM_FILE_NAME;", namedParameters, new BeanPropertyRowMapper(FormEntity.class));
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

    public List<DocumentEntity> findByType(String docType) {
        List<DocumentEntity> listAll = null;
        StringBuilder sql = new StringBuilder();

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            if(!"ALL".equals(docType)){
                sql.append(" select * from m_documents  where DOCUMENT_TYPE = :DOCUMENT_TYPE  order by DOCUMENT_SEQ ");
                namedParameters.addValue("DOCUMENT_TYPE", docType);
            } else {
                sql.append(" select * from m_documents  order by DOCUMENT_SEQ ");
            }

            listAll = template.query(sql.toString() , namedParameters, new BeanPropertyRowMapper(DocumentEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

}
