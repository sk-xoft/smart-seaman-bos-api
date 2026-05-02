package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.InterceptorEntity;
import com.seaman.exception.BusinessException;
import com.seaman.service.MessageCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class InterceptorRepository extends CommonRepository {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    private static final String GET_INTERCEPTOR = "select m.PERMISSION_CODE  from m_groups_map_autholist as a " +
            "left outer join m_menus_map_permission as m on a.GROUPS_MAP_AUTHOLIST_ID=m.MENU_MAP_PERMISSION_ID  " +
            "left outer join m_menus as mn on m.MENU_CODE = mn.MENU_CODE " +
            "where a.GROUP_ID = :GROUP_ID and m.MENU_CODE= :MENU_CODE and a.AUTHOLIST_STATUS='A' and mn.MENU_STATUS='A' and m.PERMISSION_CODE= :PERMISSION_CODE";

    private static final String TEST = "select PERMISSION_CODE from m_menus_map_permission group by PERMISSION_CODE";



    public InterceptorEntity isInterceptor(String groupId, String menuCode,String permissionCode) {

        List<InterceptorEntity> listRecord = null;

        InterceptorEntity result = new InterceptorEntity();

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_ID", groupId)
                    .addValue("MENU_CODE", menuCode)
                    .addValue("PERMISSION_CODE", permissionCode);
            listRecord = template.query(GET_INTERCEPTOR, namedParameters, new BeanPropertyRowMapper(InterceptorEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }
            else{
                result.setPermissionCode("false");

            }
           // logger.info("result:"+result);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }

}






