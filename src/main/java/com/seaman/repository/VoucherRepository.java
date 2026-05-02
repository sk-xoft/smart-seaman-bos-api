package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.NewsEntity;
import com.seaman.entity.VoucherEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.VoucherRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class VoucherRepository extends CommonRepository {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public List<VoucherEntity> findAll(Integer start, Integer row, VoucherRequest request) {
        List<VoucherEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_vouchers  ");
        sql.append(" where 1 > 0 ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            if(null != request.getVoucherTitle() && !request.getVoucherTitle().equals("")) {
                sql.append(" and VOUCHER_TITLE like :VOUCHER_TITLE ");
                namedParameters.addValue("VOUCHER_TITLE", "%" + request.getVoucherTitle() + "%");
            }
            sql.append(" LIMIT :START, :ROW ");
            namedParameters.addValue("START", start);
            namedParameters.addValue("ROW", row);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(VoucherEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<VoucherEntity> findAllCount(Integer start, Integer row, VoucherRequest request) {
        List<VoucherEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_vouchers  ");
        sql.append(" where 1 > 0 ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            if(null != request.getVoucherTitle() && !request.getVoucherTitle().equals("")) {
                sql.append(" and VOUCHER_TITLE like :VOUCHER_TITLE ");
                namedParameters.addValue("VOUCHER_TITLE", "%" + request.getVoucherTitle() + "%");
            }

            namedParameters.addValue("START", start);
            namedParameters.addValue("ROW", row);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(VoucherEntity.class));
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public VoucherEntity findById(String id) {

        List<VoucherEntity> listRecord = null;

        VoucherEntity result = null;
        StringBuilder sql = new StringBuilder();
        sql.append(" select * from m_vouchers where VOUCHER_ID = :VOUCHER_ID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("VOUCHER_ID", id);

            listRecord = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(VoucherEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "{voucher_id} does not exist");
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

    public String findSmartSeamanId(String id) {

        String smartSeamanId = "";

        StringBuilder sql = new StringBuilder();
        sql.append(" select SMART_SEAMAN_ID from m_voucher_details where VOUCHER_ID = :VOUCHER_ID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("VOUCHER_ID", id);
            smartSeamanId = template.queryForObject(sql.toString(), namedParameters, String.class);

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            // throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return smartSeamanId;
    }

    public Integer getTotalRecord() {
        Integer maxMember;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" select count(*) from m_vouchers ");
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject(sql.toString(), namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember = (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    public int insertVoucher(VoucherEntity entity) {

        int result = 0;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" insert into m_vouchers (VOUCHER_TITLE, VOUCHER_PICTURE, VOUCHER_DETAILS, VOUCHER_QRCODE, CREATE_DATE, CREATE_BY, VOUCHER_TYPE) ");
            sql.append(" value (:VOUCHER_TITLE, :VOUCHER_PICTURE, :VOUCHER_DETAILS, :VOUCHER_QRCODE, now(), :CREATE_BY, :VOUCHER_TYPE) ");

            // The GeneratedKeyHolder object is used to get the auto-incrementing ID.
            GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("VOUCHER_TITLE", entity.getVoucherTitle())
                    .addValue("VOUCHER_PICTURE", entity.getVoucherPicture())
                    .addValue("VOUCHER_DETAILS", entity.getVoucherDetails())
                    .addValue("VOUCHER_QRCODE", entity.getVoucherQrcode())
                    .addValue("CREATE_BY", entity.getCreateBy())
                    .addValue("VOUCHER_TYPE", entity.getVoucherType());

            int rowAffected = template.update(sql.toString(), namedParameters, generatedKeyHolder);

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

    public boolean insertVoucherDetail(String voucherId, String smartseamanId) {
        boolean result = false;
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("  insert into m_voucher_details (VOUCHER_ID, SMART_SEAMAN_ID) VALUE (:VOUCHER_ID, :SMART_SEAMAN_ID) ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("VOUCHER_ID", voucherId)
                    .addValue("SMART_SEAMAN_ID", smartseamanId);

            int rowAffected = template.update(sql.toString(), namedParameters);

            if (rowAffected != 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public int deleteVoucher(VoucherEntity entity) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" delete from m_vouchers where VOUCHER_ID = :VOUCHER_ID ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("VOUCHER_ID", entity.getVoucherId());
            return  template.update(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }

    public int deleteVoucherDetails(String voucherId) {
        try {
            StringBuilder sql = new StringBuilder();
            sql.append(" delete from m_voucher_details where VOUCHER_ID = :VOUCHER_ID ");  // and SMART_SEAMAN_ID = :SMART_SEAMAN_ID ");

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("VOUCHER_ID", voucherId);
                    // .addValue("SMART_SEAMAN_ID", smartseamanId);
            return  template.update(sql.toString(), namedParameters);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
    }
}
