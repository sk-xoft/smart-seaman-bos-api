package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.DocumentInspectionItemRequest;
import com.seaman.model.response.DocumentAttachment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class DocumentRequestRepository extends CommonRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private static final String SELECT_DOCUMENT_REQUEST_SELECT =
            "SELECT dr.*, " +
            "dr.document_status_id AS document_status_id, " +
            "ds.name_th AS document_status_name_th, " +
            "ds.name_en AS document_status_name_en, " +
            "CONCAT(COALESCE(ds.name_th, ''), ' / ', COALESCE(ds.name_en, '')) AS document_status_display_name, " +
            "ds.css_color AS document_status_color, " +
            "mu.SMART_SEAMAN_ID AS mobile_user_smart_seaman_id, " +
            "mu.FIRST_NAME AS mobile_user_first_name, " +
            "mu.LAST_NAME AS mobile_user_last_name, " +
            "mu.POSITION_CODE AS mobile_user_position_code, " +
            "mp.POSITION_NAME_TH AS mobile_user_position_name_th, " +
            "mp.POSITION_NAME_EN AS mobile_user_position_name_en, " +
            "CONCAT(COALESCE(mp.POSITION_NAME_TH, ''), ' / ', COALESCE(mp.POSITION_NAME_EN, '')) AS mobile_user_position_display_name, " +
            "CONCAT(COALESCE(mu.FIRST_NAME, ''), ' ', COALESCE(mu.LAST_NAME, '')) AS mobile_user_full_name, " +
            "md.DOCUMENT_NAME_TH AS document_name_th, " +
            "md.DOCUMENT_NAME_EN AS document_name_en, " +
            "CONCAT(COALESCE(md.DOCUMENT_NAME_TH, ''), ' / ', COALESCE(md.DOCUMENT_NAME_EN, '')) AS document_display_name ";

    private static final String SELECT_DOCUMENT_REQUEST_FROM =
            "FROM m_document_request dr " +
            "LEFT JOIN m_document_status ds ON ds.id COLLATE utf8mb4_unicode_ci = dr.document_status_id COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN m_mobile_users mu ON mu.MOBILE_UUID COLLATE utf8mb4_unicode_ci = dr.mobile_user_uuid COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN m_positions mp ON mp.POSITION_CODE COLLATE utf8mb4_unicode_ci = mu.POSITION_CODE COLLATE utf8mb4_unicode_ci " +
            "LEFT JOIN m_documents md ON md.DOCUMENT_CODE COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ";

    private static final String SELECT_DOCUMENT_REQUEST_COUNT = "SELECT count(*) " + SELECT_DOCUMENT_REQUEST_FROM;

            private static final String SELECT_DOCUMENT_REQUEST_STATUS_COUNTS_SELECT =
            "SELECT dr.document_status_id AS document_status_id, " +
            "ds.name_th AS document_status_name_th, " +
            "ds.name_en AS document_status_name_en, " +
            "CONCAT(COALESCE(ds.name_th, ''), ' / ', COALESCE(ds.name_en, '')) AS document_status_display_name, " +
            "ds.css_color AS document_status_color, " +
                "COUNT(*) AS total ";

    public List<Map<String, Object>> findAll(
            Integer start,
            Integer row,
            String status,
            String smartSeamanId,
            String firstName,
            String requestNo
    ) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW", row);

            StringBuilder sql = new StringBuilder();
            sql.append(SELECT_DOCUMENT_REQUEST_SELECT);
            sql.append(SELECT_DOCUMENT_REQUEST_FROM);

            appendFilters(sql, namedParameters, status, smartSeamanId, firstName, requestNo);
            sql.append(" ORDER BY dr.submitted_at DESC, dr.created_at DESC LIMIT :START, :ROW");

            return template.queryForList(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Integer countAll(String status, String smartSeamanId, String firstName, String requestNo) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            StringBuilder sql = new StringBuilder();
            sql.append(SELECT_DOCUMENT_REQUEST_COUNT);
            appendFilters(sql, namedParameters, status, smartSeamanId, firstName, requestNo);

            Integer total = template.queryForObject(sql.toString(), namedParameters, Integer.class);
            return total == null ? 0 : total;
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public List<Map<String, Object>> countByStatus(String smartSeamanId, String firstName, String requestNo) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            StringBuilder sql = new StringBuilder();
            sql.append(SELECT_DOCUMENT_REQUEST_STATUS_COUNTS_SELECT);
            sql.append(SELECT_DOCUMENT_REQUEST_FROM);
            appendFilters(sql, namedParameters, null, smartSeamanId, firstName, requestNo);
            sql.append(" GROUP BY dr.document_status_id, ds.name_th, ds.name_en, ds.css_color");
            sql.append(" ORDER BY total DESC");

            return template.queryForList(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public List<DocumentAttachment> findDetailItemsByRequestNo(String requestNo) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo);

            StringBuilder sql = new StringBuilder();

            sql.append("SELECT ");
            sql.append("ri.id AS id, ");
            sql.append("dr.mobile_user_uuid AS mobile_user_uuid, ");
            sql.append("COALESCE(mri.document_master_items_name, CONCAT('เอกสาร ', dsr.sort_order)) AS document_name, ");
            sql.append("dsr.sort_order AS sort_order, ");
            sql.append("0 AS file_uploaded, ");
            sql.append("NULL AS file_path, ");
            sql.append("NULL AS file_uploaded_at, ");
            sql.append("CASE ");
            sql.append("WHEN ri.approve_status = 'PASS' THEN 'pass' ");
            sql.append("WHEN ri.approve_status = 'FIX' THEN 'fix' ");
            sql.append("ELSE '' ");
            sql.append("END AS check_result, ");
            sql.append("ri.note AS check_note, ");
            sql.append("0 AS is_updated, ");
            sql.append("NULL AS checked_at, ");
            sql.append("NULL AS checked_by, ");
            sql.append("COALESCE(ri.created_at, dsr.created_at) AS created_at, ");
            sql.append("COALESCE(ri.updated_at, dsr.updated_at) AS updated_at ");
            sql.append("FROM m_document_request dr ");
            sql.append("LEFT JOIN m_document_setting_requires dsr ");
            sql.append("ON dsr.document_code COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dsr.is_active = 'YES' ");
            sql.append("LEFT JOIN m_document_master_request_item mri ");
            sql.append("ON mri.document_master_items_code COLLATE utf8mb4_unicode_ci = dsr.document_master_request_item_code COLLATE utf8mb4_unicode_ci ");
            sql.append("AND mri.is_active = 'YES' ");
            sql.append("LEFT JOIN m_document_request_items ri ");
            sql.append("ON ri.request_no COLLATE utf8mb4_unicode_ci = dr.request_no COLLATE utf8mb4_unicode_ci ");
            sql.append("AND ri.document_master_request_item_code COLLATE utf8mb4_unicode_ci = dsr.document_master_request_item_code COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dsr.sort_order IS NOT NULL ");
            sql.append("ORDER BY dsr.sort_order ASC, mri.sort_order ASC");

            return template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper<>(DocumentAttachment.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findMobileUserProfile(String mobileUserUuid) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MOBILE_USER_UUID", mobileUserUuid);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT mu.* ");
            sql.append("FROM m_mobile_users mu ");
            sql.append("WHERE mu.MOBILE_UUID COLLATE utf8mb4_unicode_ci = :MOBILE_USER_UUID COLLATE utf8mb4_unicode_ci ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> profiles = template.queryForList(sql.toString(), namedParameters);
            if (profiles.isEmpty()) {
                return null;
            }

            return profiles.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> ensureDeliveryAddress(String mobileUserUuid) {
        try {
            Map<String, Object> deliveryAddress = findDeliveryAddress(mobileUserUuid);
            if (deliveryAddress != null) {
                return deliveryAddress;
            }

            insertDeliveryAddress(mobileUserUuid);
            return findDeliveryAddress(mobileUserUuid);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public String findLatestRequestNoByMobileUserUuid(String mobileUserUuid) {
        Map<String, Object> requestSummary = findLatestDocumentRequestSummaryByMobileUserUuid(mobileUserUuid);
        if (requestSummary == null) {
            return null;
        }

        Object requestNo = requestSummary.get("request_no");
        return requestNo == null ? null : String.valueOf(requestNo);
    }

    public Map<String, Object> findDocumentRequestSummaryByRequestNo(String requestNo) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dr.id AS request_id, dr.request_no AS request_no, dr.document_code AS document_code, dr.created_at AS created_at, dr.mobile_user_uuid AS mobile_user_uuid, ");
            sql.append("dr.document_status_id AS document_status_id, ds.name_en AS document_status_name_en, ds.name_th AS document_status_name_th, ");
            sql.append("COALESCE(md.DOCUMENT_NAME_EN, md.DOCUMENT_NAME_TH, dr.document_code) AS document_name ");
            sql.append("FROM m_document_request dr ");
            sql.append("LEFT JOIN m_document_status ds ON ds.id COLLATE utf8mb4_unicode_ci = dr.document_status_id COLLATE utf8mb4_unicode_ci ");
            sql.append("LEFT JOIN m_documents md ON md.DOCUMENT_CODE COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci ");
            sql.append("ORDER BY dr.submitted_at DESC, dr.created_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findAttachmentTargetByRequestNoAndSortOrder(String requestNo, Integer sortOrder) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo)
                    .addValue("SORT_ORDER", sortOrder);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dr.request_no AS request_no, dr.mobile_user_uuid AS mobile_user_uuid, ");
            sql.append("dri.id AS request_item_id, ");
            sql.append("dri.document_master_request_item_code AS document_master_request_item_code, ");
            sql.append("mri.document_master_items_name AS document_name, ");
            sql.append("dsr.sort_order AS sort_order ");
            sql.append("FROM m_document_request dr ");
            sql.append("LEFT JOIN m_document_setting_requires dsr ");
            sql.append("ON dsr.document_code COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dsr.is_active = 'YES' ");
            sql.append("AND dsr.sort_order = :SORT_ORDER ");
            sql.append("LEFT JOIN m_document_master_request_item mri ");
            sql.append("ON mri.document_master_items_code COLLATE utf8mb4_unicode_ci = dsr.document_master_request_item_code COLLATE utf8mb4_unicode_ci ");
            sql.append("AND mri.is_active = 'YES' ");
            sql.append("JOIN m_document_request_items dri ");
            sql.append("ON dri.request_no COLLATE utf8mb4_unicode_ci = dr.request_no COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dri.document_master_request_item_code COLLATE utf8mb4_unicode_ci = dsr.document_master_request_item_code COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dsr.sort_order IS NOT NULL ");
            sql.append("ORDER BY dri.updated_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public int upsertRequestItemFile(String requestItemId,
                                     String documentMasterRequestItemCode,
                                     Integer sortOrder,
                                     String filePath,
                                     String originalFileName,
                                     String mimeType,
                                     long fileSize) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_ITEM_ID", requestItemId)
                    .addValue("DOCUMENT_MASTER_REQUEST_ITEM_CODE", documentMasterRequestItemCode)
                    .addValue("SORT_ORDER", sortOrder)
                    .addValue("FILE_PATH", filePath)
                    .addValue("ORIGINAL_FILE_NAME", originalFileName)
                    .addValue("MIME_TYPE", mimeType)
                    .addValue("FILE_SIZE", fileSize);

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO m_document_request_item_files (");
            sql.append("request_item_id, document_master_request_item_code, document_type, slot_code, sort_order, ");
            sql.append("file_uploaded, file_path, original_file_name, mime_type, file_size, file_uploaded_at, is_updated");
            sql.append(") VALUES (");
            sql.append(":REQUEST_ITEM_ID, :DOCUMENT_MASTER_REQUEST_ITEM_CODE, 'GENERAL', 'MAIN', :SORT_ORDER, ");
            sql.append("1, :FILE_PATH, :ORIGINAL_FILE_NAME, :MIME_TYPE, :FILE_SIZE, NOW(), 1");
            sql.append(") ON DUPLICATE KEY UPDATE ");
            sql.append("sort_order = VALUES(sort_order), file_uploaded = 1, file_path = VALUES(file_path), ");
            sql.append("original_file_name = VALUES(original_file_name), mime_type = VALUES(mime_type), ");
            sql.append("file_size = VALUES(file_size), file_uploaded_at = NOW(), check_result = NULL, ");
            sql.append("check_note = NULL, checked_at = NULL, checked_by = NULL, is_updated = 1");

            return template.update(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findLatestUploadedAttachmentByRequestNoAndSortOrder(String requestNo, Integer sortOrder) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo)
                    .addValue("SORT_ORDER", sortOrder);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT f.file_path, f.original_file_name, f.mime_type ");
            sql.append("FROM m_document_request dr ");
            sql.append("JOIN m_document_request_items ri ");
            sql.append("  ON ri.request_id COLLATE utf8mb4_unicode_ci = dr.id COLLATE utf8mb4_unicode_ci ");
            sql.append("JOIN m_document_setting_requires dsr ");
            sql.append("  ON dsr.document_code COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ");
            sql.append("  AND dsr.document_master_request_item_code COLLATE utf8mb4_unicode_ci = ri.document_master_request_item_code COLLATE utf8mb4_unicode_ci ");
            sql.append("  AND dsr.is_active = 'YES' ");
            sql.append("JOIN m_document_request_item_files f ");
            sql.append("  ON f.request_item_id COLLATE utf8mb4_unicode_ci = ri.id COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dsr.sort_order = :SORT_ORDER ");
            sql.append("AND f.file_uploaded = 1 ");
            sql.append("ORDER BY f.file_uploaded_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findDocumentRequestSummaryByUserDocumentAndStatus(
            String mobileUserUuid,
            String documentCode,
            String documentStatusId
    ) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MOBILE_USER_UUID", mobileUserUuid)
                    .addValue("DOCUMENT_CODE", documentCode)
                    .addValue("DOCUMENT_STATUS_ID", documentStatusId);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dr.request_no AS request_no, dr.created_at AS created_at, dr.mobile_user_uuid AS mobile_user_uuid, ");
            sql.append("dr.document_code AS document_code, dr.document_status_id AS document_status_id, ");
            sql.append("ds.name_en AS document_status_name_en, ds.name_th AS document_status_name_th, ");
            sql.append("COALESCE(md.DOCUMENT_NAME_EN, md.DOCUMENT_NAME_TH, dr.document_code) AS document_name ");
            sql.append("FROM m_document_request dr ");
            sql.append("LEFT JOIN m_document_status ds ON ds.id COLLATE utf8mb4_unicode_ci = dr.document_status_id COLLATE utf8mb4_unicode_ci ");
            sql.append("LEFT JOIN m_documents md ON md.DOCUMENT_CODE COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dr.mobile_user_uuid COLLATE utf8mb4_unicode_ci = :MOBILE_USER_UUID COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dr.document_code COLLATE utf8mb4_unicode_ci = :DOCUMENT_CODE COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dr.document_status_id COLLATE utf8mb4_unicode_ci = :DOCUMENT_STATUS_ID COLLATE utf8mb4_unicode_ci ");
            sql.append("ORDER BY dr.submitted_at DESC, dr.created_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findLatestDocumentRequestSummaryByItemId(String id) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("ID", id);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dr.request_no AS request_no, dr.created_at AS created_at, dr.mobile_user_uuid AS mobile_user_uuid, ");
            sql.append("dr.document_status_id AS document_status_id, ds.name_en AS document_status_name_en, ds.name_th AS document_status_name_th, ");
            sql.append("COALESCE(md.DOCUMENT_NAME_EN, md.DOCUMENT_NAME_TH, dr.document_code) AS document_name ");
            sql.append("FROM m_document_request_items dri ");
            sql.append("JOIN m_document_request dr ON dr.request_no COLLATE utf8mb4_unicode_ci = dri.request_no COLLATE utf8mb4_unicode_ci ");
            sql.append("LEFT JOIN m_document_status ds ON ds.id COLLATE utf8mb4_unicode_ci = dr.document_status_id COLLATE utf8mb4_unicode_ci ");
            sql.append("LEFT JOIN m_documents md ON md.DOCUMENT_CODE COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dri.id COLLATE utf8mb4_unicode_ci = :ID COLLATE utf8mb4_unicode_ci ");
            sql.append("ORDER BY dr.submitted_at DESC, dr.created_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findLatestDocumentRequestSummaryByMobileUserUuid(String mobileUserUuid) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MOBILE_USER_UUID", mobileUserUuid);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dr.request_no AS request_no, dr.created_at AS created_at, dr.mobile_user_uuid AS mobile_user_uuid, ");
            sql.append("dr.document_status_id AS document_status_id, ds.name_en AS document_status_name_en, ds.name_th AS document_status_name_th, ");
            sql.append("COALESCE(md.DOCUMENT_NAME_EN, md.DOCUMENT_NAME_TH, dr.document_code) AS document_name ");
            sql.append("FROM m_document_request dr ");
            sql.append("LEFT JOIN m_document_status ds ON ds.id COLLATE utf8mb4_unicode_ci = dr.document_status_id COLLATE utf8mb4_unicode_ci ");
            sql.append("LEFT JOIN m_documents md ON md.DOCUMENT_CODE COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dr.mobile_user_uuid COLLATE utf8mb4_unicode_ci = :MOBILE_USER_UUID COLLATE utf8mb4_unicode_ci ");
            sql.append("ORDER BY dr.submitted_at DESC, dr.created_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> requestSummaries = template.queryForList(sql.toString(), namedParameters);
            if (requestSummaries.isEmpty()) {
                return null;
            }

            return requestSummaries.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public List<Map<String, Object>> findDocumentStatusMasterForStepper() {
        try {
            String orderColumn = findDocumentStatusOrderColumn();

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ds.id AS status_id, ds.name_en AS status_name_en, ds.name_th AS status_name_th ");
            sql.append("FROM m_document_status ds ");

            if (orderColumn != null) {
                sql.append("ORDER BY ds.").append(orderColumn).append(" ASC, ds.id ASC");
            } else {
                sql.append("ORDER BY ds.id ASC");
            }

            return template.queryForList(sql.toString(), new MapSqlParameterSource());
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public int updateInspectionResults(String requestNo, List<DocumentInspectionItemRequest> inspections) {
        try {
            if (inspections == null || inspections.isEmpty()) {
                return 0;
            }

            List<Integer> sortOrders = inspections.stream()
                .map(DocumentInspectionItemRequest::getSortOrder)
                .distinct()
                .collect(java.util.stream.Collectors.toList());
            MapSqlParameterSource targetParameters = new MapSqlParameterSource()
                .addValue("REQUEST_NO", requestNo)
                .addValue("SORT_ORDERS", sortOrders);
            String targetSql = "SELECT COUNT(DISTINCT dsr.sort_order) " +
                "FROM m_document_request dr " +
                "JOIN m_document_setting_requires dsr " +
                "ON dsr.document_code COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci " +
                "AND dsr.is_active = 'YES' " +
                "WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci " +
                "AND dsr.sort_order IN (:SORT_ORDERS)";
            Integer targetCount = template.queryForObject(targetSql, targetParameters, Integer.class);
            if (targetCount == null || targetCount != inspections.size()) {
            return targetCount == null ? 0 : targetCount;
            }

            int updatedRows = 0;

            for (DocumentInspectionItemRequest item : inspections) {
                String approveStatus = toApproveStatus(item.getCheckResult());
                MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                        .addValue("REQUEST_NO", requestNo)
                        .addValue("SORT_ORDER", item.getSortOrder())
                        .addValue("APPROVE_STATUS", approveStatus)
                        .addValue("NOTE", item.getCheckNote());

                StringBuilder sql = new StringBuilder();
                sql.append("INSERT INTO m_document_request_items ");
                sql.append("(request_id, request_no, document_master_request_item_code, approve_status, note, created_at, updated_at) ");
                sql.append("SELECT dr.id, dr.request_no, dsr.document_master_request_item_code, ");
                sql.append(":APPROVE_STATUS, :NOTE, NOW(), NOW() ");
                sql.append("FROM m_document_request dr ");
                sql.append("JOIN m_document_setting_requires dsr ");
                sql.append("ON dsr.document_code COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ");
                sql.append("AND dsr.is_active = 'YES' ");
                sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci ");
                sql.append("AND dsr.sort_order = :SORT_ORDER ");
                sql.append("ON DUPLICATE KEY UPDATE approve_status = VALUES(approve_status), ");
                sql.append("note = VALUES(note), updated_at = NOW()");

                template.update(sql.toString(), namedParameters);
                updatedRows++;
            }

            return updatedRows;
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public String findDocumentStatusIdByThaiName(String statusNameTh) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("STATUS_NAME_TH", statusNameTh);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ds.id ");
            sql.append("FROM m_document_status ds ");
            sql.append("WHERE ds.name_th COLLATE utf8mb4_unicode_ci = :STATUS_NAME_TH COLLATE utf8mb4_unicode_ci ");
            sql.append("LIMIT 1");

            List<String> ids = template.queryForList(sql.toString(), namedParameters, String.class);
            return ids.isEmpty() ? null : ids.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public String findDocumentStatusIdByCode(String statusCode) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("STATUS_CODE", statusCode);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT ds.id ");
            sql.append("FROM m_document_status ds ");
            sql.append("WHERE ds.document_status_code = :STATUS_CODE ");
            sql.append("AND ds.is_active = 'YES' ");
            sql.append("LIMIT 1");

            List<String> ids = template.queryForList(sql.toString(), namedParameters, String.class);
            return ids.isEmpty() ? null : ids.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public int updateDocumentRequestStatus(String requestNo, String statusId, boolean resetResubmit) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo)
                    .addValue("STATUS_ID", statusId)
                    .addValue("RESET_RESUBMIT", resetResubmit ? 1 : 0);

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE m_document_request dr ");
            sql.append("SET dr.document_status_id = :STATUS_ID, ");
            sql.append("dr.is_resubmit = CASE WHEN :RESET_RESUBMIT = 1 THEN 0 ELSE dr.is_resubmit END, ");
            sql.append("dr.updated_at = NOW() ");
            sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci");

            return template.update(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public int markDocumentRequestDeliveredIfDelivering(String requestNo, String deliveredStatusId) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo)
                    .addValue("DELIVERED_STATUS_ID", deliveredStatusId);

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE m_document_request dr ");
            sql.append("JOIN m_document_status current_status ");
            sql.append("ON current_status.id COLLATE utf8mb4_unicode_ci = dr.document_status_id COLLATE utf8mb4_unicode_ci ");
            sql.append("SET dr.document_status_id = :DELIVERED_STATUS_ID, dr.updated_at = NOW() ");
            sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci ");
            sql.append("AND current_status.document_status_code = 'DELIVERING'");

            return template.update(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public int markDeliveryDeliveredByRequestNo(String requestNo) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo);

            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE m_delivery d ");
            sql.append("JOIN m_document_request dr ");
            sql.append("ON dr.id COLLATE utf8mb4_unicode_ci = d.request_id COLLATE utf8mb4_unicode_ci ");
            sql.append("SET d.delivery_status = 'delivered', ");
            sql.append("d.delivered_at = COALESCE(d.delivered_at, NOW()), ");
            sql.append("d.updated_at = NOW() ");
            sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci");

            return template.update(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public List<Map<String, Object>> findDeliveringRequestsWithTrackingNo() {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dr.request_no, d.tracking_no ");
            sql.append("FROM m_document_request dr ");
            sql.append("JOIN m_document_status ds ");
            sql.append("ON ds.id COLLATE utf8mb4_unicode_ci = dr.document_status_id COLLATE utf8mb4_unicode_ci ");
            sql.append("JOIN m_delivery d ");
            sql.append("ON d.request_id COLLATE utf8mb4_unicode_ci = dr.id COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dr.is_active = 'YES' ");
            sql.append("AND ds.document_status_code = 'DELIVERING' ");
            sql.append("AND d.tracking_no IS NOT NULL ");
            sql.append("AND TRIM(d.tracking_no) <> ''");

            return template.queryForList(sql.toString(), new MapSqlParameterSource());
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public int insertDocumentTransaction(
            String requestId,
            String action,
            String fromStatus,
            String toStatus,
            String note,
            String actionedBy
    ) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId)
                    .addValue("ACTION", action)
                    .addValue("FROM_STATUS", fromStatus)
                    .addValue("TO_STATUS", toStatus)
                    .addValue("NOTE", note)
                    .addValue("ACTIONED_BY", actionedBy);

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO m_document_transaction (");
            sql.append("request_id, action, from_status, to_status, note, actioned_at, actioned_by");
            sql.append(") VALUES (");
            sql.append(":REQUEST_ID, :ACTION, :FROM_STATUS, :TO_STATUS, :NOTE, NOW(), :ACTIONED_BY");
            sql.append(")");

            return template.update(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findLatestDepartmentSubmissionInfo(String requestId) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dt.action AS action, dt.from_status AS from_status, dt.to_status AS to_status, ");
            sql.append("dt.note AS note, dt.actioned_at AS actioned_at, dt.actioned_by AS actioned_by, ");
            sql.append("au.USERNAME AS actioned_by_username, au.FIRST_NAME AS actioned_by_first_name, ");
            sql.append("au.LAST_NAME AS actioned_by_last_name, au.MOBILE_NUMBER AS actioned_by_mobile_number ");
            sql.append("FROM m_document_transaction dt ");
            sql.append("LEFT JOIN m_admin_users au ");
            sql.append("ON au.ADMIN_UUID COLLATE utf8mb4_unicode_ci = dt.actioned_by COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dt.request_id COLLATE utf8mb4_unicode_ci = :REQUEST_ID COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dt.action IN ('SUBMIT_TO_DEPT', 'CHECK_DOCS') ");
            sql.append("ORDER BY dt.actioned_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findLatestDepartmentResultInfo(String requestId) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dt.action AS action, dt.note AS note, dt.actioned_at AS actioned_at, dt.actioned_by AS actioned_by, ");
            sql.append("au.USERNAME AS actioned_by_username, au.FIRST_NAME AS actioned_by_first_name, ");
            sql.append("au.LAST_NAME AS actioned_by_last_name, au.MOBILE_NUMBER AS actioned_by_mobile_number ");
            sql.append("FROM m_document_transaction dt ");
            sql.append("LEFT JOIN m_admin_users au ");
            sql.append("ON au.ADMIN_UUID COLLATE utf8mb4_unicode_ci = dt.actioned_by COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dt.request_id COLLATE utf8mb4_unicode_ci = :REQUEST_ID COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dt.action = 'RECORD_DEPT_RESULT' ");
            sql.append("ORDER BY dt.actioned_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findLatestDepartmentReceiveInfo(String requestId) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dt.action AS action, dt.note AS note, dt.actioned_at AS actioned_at, dt.actioned_by AS actioned_by, ");
            sql.append("au.USERNAME AS actioned_by_username, au.FIRST_NAME AS actioned_by_first_name, ");
            sql.append("au.LAST_NAME AS actioned_by_last_name, au.MOBILE_NUMBER AS actioned_by_mobile_number ");
            sql.append("FROM m_document_transaction dt ");
            sql.append("LEFT JOIN m_admin_users au ");
            sql.append("ON au.ADMIN_UUID COLLATE utf8mb4_unicode_ci = dt.actioned_by COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dt.request_id COLLATE utf8mb4_unicode_ci = :REQUEST_ID COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dt.action = 'RECEIVE_FROM_DEPT' ");
            sql.append("ORDER BY dt.actioned_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public int upsertDeliveryInfoByRequestNo(String requestNo, String trackingNo, String shippedDate, String shippedBy) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo)
                    .addValue("TRACKING_NO", trackingNo)
                    .addValue("SHIPPED_DATE", shippedDate)
                    .addValue("SHIPPED_BY", shippedBy);

            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO m_delivery (");
            sql.append("request_id, delivery_address_id, tracking_no, carrier, shipped_date, delivery_status, shipped_recorded_at, shipped_by, delivered_at, created_at, updated_at");
            sql.append(") ");
            sql.append("SELECT dr.id, NULL, :TRACKING_NO, 'Thailand Post', STR_TO_DATE(:SHIPPED_DATE, '%Y-%m-%d'), 'in_transit', NOW(), :SHIPPED_BY, NULL, NOW(), NOW() ");
            sql.append("FROM m_document_request dr ");
            sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci ");
            sql.append("ON DUPLICATE KEY UPDATE ");
            sql.append("tracking_no = VALUES(tracking_no), ");
            sql.append("carrier = VALUES(carrier), ");
            sql.append("shipped_date = VALUES(shipped_date), ");
            sql.append("delivery_status = 'in_transit', ");
            sql.append("shipped_recorded_at = NOW(), ");
            sql.append("shipped_by = VALUES(shipped_by), ");
            sql.append("updated_at = NOW()");

            return template.update(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findLatestDeliveryInfo(String requestId) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT d.tracking_no AS tracking_no, d.shipped_date AS shipped_date, d.shipped_recorded_at AS shipped_recorded_at, ");
            sql.append("d.shipped_by AS shipped_by, d.delivery_status AS delivery_status, au.USERNAME AS shipped_by_username, ");
            sql.append("au.FIRST_NAME AS shipped_by_first_name, au.LAST_NAME AS shipped_by_last_name, au.MOBILE_NUMBER AS shipped_by_mobile_number ");
            sql.append("FROM m_delivery d ");
            sql.append("LEFT JOIN m_admin_users au ");
            sql.append("ON au.ADMIN_UUID COLLATE utf8mb4_unicode_ci = d.shipped_by COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE d.request_id COLLATE utf8mb4_unicode_ci = :REQUEST_ID COLLATE utf8mb4_unicode_ci ");
            sql.append("ORDER BY d.updated_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public Map<String, Object> findLatestDeliveryTransactionInfo(String requestId) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_ID", requestId);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT dt.action AS action, dt.note AS note, dt.actioned_at AS actioned_at, dt.actioned_by AS actioned_by, ");
            sql.append("au.USERNAME AS actioned_by_username, au.FIRST_NAME AS actioned_by_first_name, ");
            sql.append("au.LAST_NAME AS actioned_by_last_name, au.MOBILE_NUMBER AS actioned_by_mobile_number ");
            sql.append("FROM m_document_transaction dt ");
            sql.append("LEFT JOIN m_admin_users au ");
            sql.append("ON au.ADMIN_UUID COLLATE utf8mb4_unicode_ci = dt.actioned_by COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dt.request_id COLLATE utf8mb4_unicode_ci = :REQUEST_ID COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dt.action = 'RECORD_DELIVERY' ");
            sql.append("ORDER BY dt.actioned_at DESC ");
            sql.append("LIMIT 1");

            List<Map<String, Object>> rows = template.queryForList(sql.toString(), namedParameters);
            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public boolean areAllRequiredDocumentItemsPassed(String requestNo) {
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("REQUEST_NO", requestNo);

            StringBuilder sql = new StringBuilder();
            sql.append("SELECT CASE WHEN COUNT(*) > 0 AND COUNT(*) = SUM(CASE WHEN ri.approve_status = 'PASS' THEN 1 ELSE 0 END) THEN 1 ELSE 0 END AS all_passed ");
            sql.append("FROM m_document_request dr ");
            sql.append("JOIN m_document_setting_requires dsr ");
            sql.append("ON dsr.document_code COLLATE utf8mb4_unicode_ci = dr.document_code COLLATE utf8mb4_unicode_ci ");
            sql.append("AND dsr.is_active = 'YES' ");
            sql.append("AND dsr.is_required = 1 ");
            sql.append("LEFT JOIN m_document_request_items ri ");
            sql.append("ON ri.request_no COLLATE utf8mb4_unicode_ci = dr.request_no COLLATE utf8mb4_unicode_ci ");
            sql.append("AND ri.document_master_request_item_code COLLATE utf8mb4_unicode_ci = dsr.document_master_request_item_code COLLATE utf8mb4_unicode_ci ");
            sql.append("WHERE dr.request_no COLLATE utf8mb4_unicode_ci = :REQUEST_NO COLLATE utf8mb4_unicode_ci");

            Integer allPassed = template.queryForObject(sql.toString(), namedParameters, Integer.class);
            return allPassed != null && allPassed == 1;
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    private String toApproveStatus(String checkResult) {
        if (checkResult == null || checkResult.trim().isEmpty()) {
            return "PENDING";
        }

        String normalized = checkResult.trim().toLowerCase();
        if ("pass".equals(normalized)) {
            return "PASS";
        }

        if ("fix".equals(normalized)) {
            return "FIX";
        }

        return "PENDING";
    }

    private String findDocumentStatusOrderColumn() {
        List<String> candidateColumns = new ArrayList<>();
        candidateColumns.add("sort_order");
        candidateColumns.add("display_order");
        candidateColumns.add("sequence_no");
        candidateColumns.add("seq_no");
        candidateColumns.add("step_order");
        candidateColumns.add("order_no");

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < candidateColumns.size(); i++) {
            if (i > 0) {
                inClause.append(",");
            }
            inClause.append(":C").append(i);
        }

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();
        for (int i = 0; i < candidateColumns.size(); i++) {
            namedParameters.addValue("C" + i, candidateColumns.get(i));
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.column_name ");
        sql.append("FROM information_schema.columns c ");
        sql.append("WHERE c.table_schema = DATABASE() ");
        sql.append("AND c.table_name = 'm_document_status' ");
        sql.append("AND c.column_name IN (").append(inClause).append(") ");
        sql.append("ORDER BY CASE c.column_name ");
        for (int i = 0; i < candidateColumns.size(); i++) {
            sql.append("WHEN :C").append(i).append(" THEN ").append(i + 1).append(" ");
        }
        sql.append("ELSE 999 END ");
        sql.append("LIMIT 1");

        List<String> columns = template.queryForList(sql.toString(), namedParameters, String.class);
        if (columns.isEmpty()) {
            return null;
        }

        return columns.get(0);
    }

    private Map<String, Object> findDeliveryAddress(String mobileUserUuid) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("MOBILE_USER_UUID", mobileUserUuid);

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT da.*, ");
        sql.append("p.name_in_thai AS province_name, ");
        sql.append("d.name_in_thai AS district_name, ");
        sql.append("s.name_in_thai AS sub_district_name ");
        sql.append("FROM m_delivery_address da ");
        sql.append("LEFT JOIN provinces p ON BINARY CAST(p.code AS CHAR) = BINARY da.province ");
        sql.append("LEFT JOIN districts d ON BINARY CAST(d.code AS CHAR) = BINARY da.district ");
        sql.append("  AND d.province_id = p.id ");
        sql.append("LEFT JOIN subdistricts s ON BINARY CAST(s.code AS CHAR) = BINARY da.sub_district ");
        sql.append("  AND s.district_id = d.id ");
        sql.append("WHERE da.mobile_user_uuid COLLATE utf8mb4_unicode_ci = :MOBILE_USER_UUID COLLATE utf8mb4_unicode_ci ");
        sql.append("AND da.is_active = 'YES' ");
        sql.append("ORDER BY da.is_default DESC, da.updated_at DESC ");
        sql.append("LIMIT 1");

        List<Map<String, Object>> deliveryAddressList = template.queryForList(sql.toString(), namedParameters);
        if (deliveryAddressList.isEmpty()) {
            return null;
        }

        return deliveryAddressList.get(0);
    }

    private void insertDeliveryAddress(String mobileUserUuid) {
        MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                .addValue("MOBILE_USER_UUID", mobileUserUuid);

        StringBuilder sqlFromMobileUser = new StringBuilder();
        sqlFromMobileUser.append("INSERT INTO m_delivery_address (");
        sqlFromMobileUser.append("mobile_user_uuid, first_name, last_name, address_line, province, district, sub_district, postal_code, is_default, is_active, created_at, updated_at");
        sqlFromMobileUser.append(") ");
        sqlFromMobileUser.append("SELECT ");
        sqlFromMobileUser.append("mu.MOBILE_UUID, ");
        sqlFromMobileUser.append("COALESCE(mu.FIRST_NAME, ''), ");
        sqlFromMobileUser.append("COALESCE(mu.LAST_NAME, ''), ");
        sqlFromMobileUser.append("'', '', '', '', '', 0, 'YES', NOW(), NOW() ");
        sqlFromMobileUser.append("FROM m_mobile_users mu ");
        sqlFromMobileUser.append("WHERE mu.MOBILE_UUID COLLATE utf8mb4_unicode_ci = :MOBILE_USER_UUID COLLATE utf8mb4_unicode_ci ");
        sqlFromMobileUser.append("LIMIT 1");

        int insertedRows = template.update(sqlFromMobileUser.toString(), namedParameters);
        if (insertedRows > 0) {
            return;
        }

        StringBuilder sqlFallback = new StringBuilder();
        sqlFallback.append("INSERT INTO m_delivery_address (");
        sqlFallback.append("mobile_user_uuid, first_name, last_name, address_line, province, district, sub_district, postal_code, is_default, is_active, created_at, updated_at");
        sqlFallback.append(") VALUES (");
        sqlFallback.append(":MOBILE_USER_UUID, '', '', '', '', '', '', '', 0, 'YES', NOW(), NOW())");

        template.update(sqlFallback.toString(), namedParameters);
    }

    private void appendFilters(
            StringBuilder sql,
            MapSqlParameterSource namedParameters,
            String status,
            String smartSeamanId,
                String firstName,
            String requestNo
    ) {
        List<String> conditions = new ArrayList<>();
        conditions.add("dr.is_active = 'YES'");

        if (status != null && !status.trim().isEmpty()) {
            conditions.add("(ds.name_th LIKE CONCAT('%', :STATUS, '%') OR ds.name_en LIKE CONCAT('%', :STATUS, '%') OR dr.document_status_id = :STATUS)");
            namedParameters.addValue("STATUS", status.trim());
        }

        if (smartSeamanId != null && !smartSeamanId.trim().isEmpty()) {
            conditions.add("mu.SMART_SEAMAN_ID LIKE CONCAT('%', :SMART_SEAMAN_ID, '%')");
            namedParameters.addValue("SMART_SEAMAN_ID", smartSeamanId.trim());
        }

        if (firstName != null && !firstName.trim().isEmpty()) {
            conditions.add("mu.FIRST_NAME LIKE CONCAT('%', :FIRST_NAME, '%')");
            namedParameters.addValue("FIRST_NAME", firstName.trim());
        }

        if (requestNo != null && !requestNo.trim().isEmpty()) {
            conditions.add("dr.request_no LIKE CONCAT('%', :REQUEST_NO, '%')");
            namedParameters.addValue("REQUEST_NO", requestNo.trim());
        }

        sql.append(" WHERE ");
        sql.append(String.join(" AND ", conditions));
    }
}
