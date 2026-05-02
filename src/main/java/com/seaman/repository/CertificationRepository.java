package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.CertificateEntity;
import com.seaman.entity.CertificateForEditEntity;
import com.seaman.entity.DocumentEntity;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class CertificationRepository  extends CommonRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<DocumentEntity> listCertByTypeMobileUuid(Integer start, Integer row, String type, String mobileUserUuid) {
        List<DocumentEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select md.* , mc.CERT_START_DATE , mc.CERT_END_DATE, mc.CERT_FILE, mc.ORIGINAL_FILE_NAME as CERT_FILE_NAME from m_documents md ");
        sql.append(" left join m_certificates mc on mc.CERT_DOCUMENT_CODE = md.DOCUMENT_CODE ");
        sql.append(" where");
        sql.append(" md.DOCUMENT_STATUS = 'A' ");
        sql.append(" and md.DOCUMENT_TYPE = :documentType ");
        sql.append(" and mc.CERT_MOBILE_UUID = :userId ");
        sql.append(" order by DOCUMENT_SEQ");
        sql.append(" LIMIT :START, :ROW ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            namedParameters.addValue("documentType", type);
            namedParameters.addValue("userId", mobileUserUuid);
            namedParameters.addValue("START", start);
            namedParameters.addValue("ROW", row);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(DocumentEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public Integer getTotalRecord(String type, String mobileUserUuid) {

        Integer record = 0;

        StringBuilder sql = new StringBuilder();
        sql.append("select count(mc.CERT_ID) as count_cert from m_documents md");
        sql.append(" left join m_certificates mc on mc.CERT_DOCUMENT_CODE = md.DOCUMENT_CODE");
        sql.append(" where");
        sql.append(" md.DOCUMENT_STATUS = 'A'");
        sql.append(" and md.DOCUMENT_TYPE = :documentType ");
        sql.append(" and mc.CERT_MOBILE_UUID = :userId ");
        sql.append(" order by DOCUMENT_SEQ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            namedParameters.addValue("documentType", type);
            namedParameters.addValue("userId", mobileUserUuid);

            record = template.queryForObject(sql.toString(), namedParameters, Integer.class);

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return record;
    }

    public CertificateEntity findByMobileUserUuid(String mobileUserUID, String documentCode) {

        List<CertificateEntity> listAll = new ArrayList<>();
        CertificateEntity item  = new CertificateEntity();

        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select * from m_certificates where CERT_DOCUMENT_CODE = :CERT_DOCUMENT_CODE and CERT_MOBILE_UUID = :CERT_MOBILE_UUID ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("CERT_DOCUMENT_CODE",  documentCode);
            namedParameters.addValue("CERT_MOBILE_UUID",  mobileUserUID);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(CertificateEntity.class));
            if(!listAll.isEmpty()){
                item = listAll.get(0);
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return item;
    }

    public boolean insert(CertificateEntity entity) {

        boolean result = false;
        StringBuilder sql = new StringBuilder();
        sql.append(" insert into m_certificates (CERT_MOBILE_UUID, CERT_DOCUMENT_CODE, CERT_START_DATE, CERT_END_DATE, CERT_FILE, ORIGINAL_FILE_NAME, ");
        sql.append(" CERT_STATUS, CREATE_DATE, CREATE_BY, UPDATE_DATE, UPDATE_BY) ");
        sql.append(" values (:CERT_MOBILE_UUID, :CERT_DOCUMENT_CODE, :CERT_START_DATE, :CERT_END_DATE, :CERT_FILE, :ORIGINAL_FILE_NAME, ");
        sql.append(" :CERT_STATUS, :CREATE_DATE, :CREATE_BY, :UPDATE_DATE, :UPDATE_BY) ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("CERT_MOBILE_UUID", entity.getCertMobileUuid())
                    .addValue("CERT_DOCUMENT_CODE", entity.getCertDocumentCode())
                    .addValue("CERT_START_DATE", entity.getCertStartDate())
                    .addValue("CERT_END_DATE", entity.getCertEndDate())
                    .addValue("CERT_FILE", entity.getCertFile())
                    .addValue("CERT_STATUS", entity.getCertStatus())
                    .addValue("ORIGINAL_FILE_NAME", entity.getOriginalFileName())
                    .addValue("CREATE_DATE", entity.getCreateDate())
                    .addValue("CREATE_BY", entity.getCreateBy())
                    .addValue("UPDATE_DATE", entity.getUpdateDate())
                    .addValue("UPDATE_BY", entity.getUpdateBy());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean update(CertificateEntity entity) {

        boolean result = false;
        StringBuilder sql = new StringBuilder();
        sql.append(" update m_certificates ");
        sql.append(" set ");
        sql.append("    CERT_START_DATE =  :CERT_START_DATE,");
        sql.append("    CERT_END_DATE = :CERT_END_DATE, ");
        sql.append("    CERT_FILE = :CERT_FILE, ");
        sql.append("    ORIGINAL_FILE_NAME = :ORIGINAL_FILE_NAME, ");
        sql.append("    CERT_STATUS = :CERT_STATUS,");
        sql.append("    UPDATE_BY = :UPDATE_BY, ");
        sql.append("    UPDATE_DATE = :UPDATE_DATE");
        sql.append(" where CERT_ID = :CERT_ID ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("CERT_START_DATE", entity.getCertStartDate())
                    .addValue("CERT_END_DATE", entity.getCertEndDate())
                    .addValue("CERT_FILE", entity.getCertFile())
                    .addValue("ORIGINAL_FILE_NAME", entity.getOriginalFileName())
                    .addValue("CERT_STATUS", entity.getCertStatus())
                    .addValue("UPDATE_DATE", entity.getUpdateDate())
                    .addValue("UPDATE_BY", entity.getUpdateBy())
                    .addValue("CERT_ID", entity.getCertId());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public boolean updateNoChangeFile(CertificateEntity entity) {

        boolean result = false;
        StringBuilder sql = new StringBuilder();
        sql.append(" update m_certificates ");
        sql.append(" set ");
        sql.append("    CERT_START_DATE =  :CERT_START_DATE,");
        sql.append("    CERT_END_DATE = :CERT_END_DATE, ");
        sql.append("    CERT_STATUS = :CERT_STATUS,");
        sql.append("    UPDATE_BY = :UPDATE_BY, ");
        sql.append("    UPDATE_DATE = :UPDATE_DATE");
        sql.append(" where CERT_ID = :CERT_ID ");

        try {

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("CERT_START_DATE", entity.getCertStartDate())
                    .addValue("CERT_END_DATE", entity.getCertEndDate())
                    .addValue("CERT_STATUS", entity.getCertStatus())
                    .addValue("CERT_ID", entity.getCertId())
                    .addValue("UPDATE_BY", entity.getUpdateBy())
                    .addValue("UPDATE_DATE", entity.getUpdateDate());

            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public CertificateEntity findBy(String mobileUserUID, String documentCode) {

        List<CertificateEntity> listAll = new ArrayList<>();
        CertificateEntity item  = new CertificateEntity();

        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select * from m_certificates where CERT_DOCUMENT_CODE = :CERT_DOCUMENT_CODE and CERT_MOBILE_UUID = :CERT_MOBILE_UUID ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("CERT_DOCUMENT_CODE",  documentCode);
            namedParameters.addValue("CERT_MOBILE_UUID",  mobileUserUID);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(CertificateEntity.class));
            if(!listAll.isEmpty()){
                item = listAll.get(0);
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return item;
    }

    public List<CertificateEntity> findByUsersAndCertCodeList(String mobileUserUID, String documentCode) {

        List<CertificateEntity> listAll = new ArrayList<>();
        CertificateEntity item  = new CertificateEntity();

        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select * from m_certificates where CERT_DOCUMENT_CODE = :CERT_DOCUMENT_CODE and CERT_MOBILE_UUID = :CERT_MOBILE_UUID ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("CERT_DOCUMENT_CODE",  documentCode);
            namedParameters.addValue("CERT_MOBILE_UUID",  mobileUserUID);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(CertificateEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<CertificateForEditEntity> findByUsersAndCertCodeListForEdit(String mobileUserUID, String documentCode) {

        List<CertificateForEditEntity> listAll = new ArrayList<>();
        CertificateForEditEntity item  = new CertificateForEditEntity();

        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select * from m_certificates where CERT_DOCUMENT_CODE = :CERT_DOCUMENT_CODE and CERT_MOBILE_UUID = :CERT_MOBILE_UUID ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("CERT_DOCUMENT_CODE",  documentCode);
            namedParameters.addValue("CERT_MOBILE_UUID",  mobileUserUID);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(CertificateForEditEntity.class));

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public boolean documentDelete(String userUid, String certCode) {

        boolean result = false;

        StringBuilder sql = new StringBuilder();
        sql.append(" delete from m_certificates where CERT_MOBILE_UUID = :CERT_MOBILE_UUID and CERT_DOCUMENT_CODE = :CERT_DOCUMENT_CODE ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("CERT_MOBILE_UUID", userUid)
                    .addValue("CERT_DOCUMENT_CODE", certCode);


            int rowAffected = template.update(sql.toString(), namedParameters);
            if (rowAffected > 0) {
                result = true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }



}
