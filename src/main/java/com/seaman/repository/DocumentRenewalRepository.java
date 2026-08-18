package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DocumentRenewalRepository extends CommonRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public Map<String, Object> findByRequestNo(String requestNo) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo);

            String sql = "SELECT dr.id, dr.request_no, dr.mobile_user_uuid, dr.document_code, " +
                    "dr.amount, dr.is_resubmit, dr.submitted_at, " +
                    "ds.id AS status_id, ds.document_status_code, " +
                    "ds.document_mobile_status_name_th AS status_name_th, " +
                    "ds.document_mobile_status_name_en AS status_name_en, " +
                    "ds.css_color AS status_css_color, " +
                    "md.DOCUMENT_NAME_TH AS document_name_th, " +
                    "md.DOCUMENT_NAME_EN AS document_name_en " +
                    "FROM m_document_request dr " +
                    "LEFT JOIN m_document_status ds ON ds.id COLLATE utf8mb4_unicode_ci = dr.document_status_id COLLATE utf8mb4_unicode_ci " +
                    "LEFT JOIN m_documents md ON md.DOCUMENT_CODE COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci " +
                    "WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci " +
                    "AND dr.is_active = 'YES' " +
                    "LIMIT 1";

            List<Map<String, Object>> results = template.queryForList(sql, params);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public List<Map<String, Object>> findItemsByRequestId(String requestId, String documentCode) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId)
                    .addValue("DOCUMENT_CODE", documentCode);

            String sql = "SELECT ri.id, ri.document_master_request_item_code, " +
                    "ri.approve_status, ri.note AS check_note, " +
                    "mri.document_master_items_name, mri.storage_scope, " +
                    "dsr.sort_order " +
                    "FROM m_document_request_items ri " +
                    "LEFT JOIN m_document_master_request_item mri " +
                    "  ON mri.document_master_items_code COLLATE utf8mb4_unicode_ci = ri.document_master_request_item_code COLLATE utf8mb4_unicode_ci " +
                    "  AND mri.is_active = 'YES' " +
                    "LEFT JOIN m_document_setting_requires dsr " +
                    "  ON dsr.document_code COLLATE utf8mb4_unicode_ci = :DOCUMENT_CODE COLLATE utf8mb4_unicode_ci " +
                    "  AND dsr.document_master_request_item_code COLLATE utf8mb4_unicode_ci = ri.document_master_request_item_code COLLATE utf8mb4_unicode_ci " +
                    "  AND dsr.is_active = 'YES' " +
                    "WHERE ri.request_id COLLATE utf8mb4_unicode_ci = :REQUEST_ID COLLATE utf8mb4_unicode_ci " +
                    "ORDER BY dsr.sort_order ASC";

            return template.queryForList(sql, params);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    // Fetch all uploaded files for the request in one query, grouped later in service layer
    public List<Map<String, Object>> findFilesByRequestId(String requestId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId);

            String sql = "SELECT f.id, f.request_item_id, f.document_type, f.slot_code, " +
                    "f.original_file_name, f.mime_type, f.file_size, f.file_uploaded_at, " +
                    "f.file_path, f.is_updated " +
                    "FROM m_document_request_item_files f " +
                    "JOIN m_document_request_items ri " +
                    "  ON ri.id COLLATE utf8mb4_unicode_ci = f.request_item_id COLLATE utf8mb4_unicode_ci " +
                    "WHERE ri.request_id COLLATE utf8mb4_unicode_ci = :REQUEST_ID COLLATE utf8mb4_unicode_ci " +
                    "AND f.file_uploaded = 1 " +
                    "ORDER BY f.sort_order ASC";

            return template.queryForList(sql, params);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findDeptSubmissionByRequestId(String requestId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId);

            String sql = "SELECT submitted_to_dept_date, available_from_date, " +
                    "received_from_dept_date, recorded_at " +
                    "FROM m_dept_submission " +
                    "WHERE request_id COLLATE utf8mb4_unicode_ci = :REQUEST_ID COLLATE utf8mb4_unicode_ci " +
                    "LIMIT 1";

            List<Map<String, Object>> results = template.queryForList(sql, params);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findDeliveryByRequestId(String requestId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId);

            String sql = "SELECT tracking_no, carrier, shipped_date, delivery_status, " +
                    "shipped_recorded_at, delivered_at " +
                    "FROM m_delivery " +
                    "WHERE request_id COLLATE utf8mb4_unicode_ci = :REQUEST_ID COLLATE utf8mb4_unicode_ci " +
                    "LIMIT 1";

            List<Map<String, Object>> results = template.queryForList(sql, params);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }
}
