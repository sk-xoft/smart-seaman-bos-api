package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.MenuEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.response.MenuInfo;
import com.seaman.model.response.MenuPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class MenuRepository  extends CommonRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

//    private static final String FIND_BY_USERNAME = "select a.GROUPS_MAP_AUTHOLIST_ID, a.GROUP_ID, a.AUTHOLIST_STATUS, m.MENU_CODE, m.PERMISSION_CODE, mn.MENU_NAME_EN, mn.MENU_NAME_TH , " +
//            "mn.MENU_URL, mn.MENU_SEQ, mn.MENU_PARENT_CODE, mn.MENU_ICON, mn.MENU_GROUP " +
//            "from m_groups_map_autholist as a  " +
//            "left outer join m_menus_map_permission as m on a.GROUPS_MAP_AUTHOLIST_ID=m.MENU_MAP_PERMISSION_ID " +
//            "left outer join m_menus as mn on m.MENU_CODE = mn.MENU_CODE " +
//            "where a.GROUP_ID = :USERNAME and a.AUTHOLIST_STATUS='A' and mn.MENU_STATUS='A'";

        private static final String FIND_BY_USERNAME = "select a.GROUPS_MAP_AUTHOLIST_ID, a.GROUP_ID, a.AUTHOLIST_STATUS, mn.MENU_CODE, mn.MENU_NAME_EN, mn.MENU_NAME_TH , " +
            "mn.MENU_URL, mn.MENU_SEQ, mn.MENU_PARENT_CODE, mn.MENU_ICON, mn.MENU_GROUP " +
            "from m_groups_map_autholist as a  " +
            "left outer join m_menus as mn on mn.MENU_ID = mn.MENU_ID " +
            "where a.GROUP_ID = :USERNAME and a.AUTHOLIST_STATUS='A' and mn.MENU_STATUS='A'";


//    private static final String LIST_MENU = "SELECT mn.MENU_ICON,m.MENU_CODE,mn.MENU_NAME_TH,mn.MENU_URL,mn.MENU_GROUP " +
//            "FROM m_groups_map_autholist AS a " +
//            "LEFT OUTER JOIN m_menus_map_permission AS m ON a.GROUPS_MAP_AUTHOLIST_ID=m.MENU_MAP_PERMISSION_ID " +
//            "LEFT OUTER JOIN m_menus AS mn ON m.MENU_CODE = mn.MENU_CODE " +
//            "WHERE a.GROUP_ID = :GROUPID AND a.AUTHOLIST_STATUS='A' AND mn.MENU_STATUS='A' " +
//            "GROUP BY mn.MENU_ICON, m.MENU_CODE, mn.MENU_NAME_TH, mn.MENU_URL, mn.MENU_GROUP";

    private static final String LIST_MENU = "SELECT mn.MENU_ICON,mn.MENU_CODE,mn.MENU_NAME_TH,mn.MENU_URL,mn.MENU_GROUP " +
            "FROM m_groups_map_autholist AS a " +
            "LEFT OUTER JOIN m_menus AS mn ON a.MENU_ID = mn.MENU_ID " +
            "WHERE a.GROUP_ID = :GROUPID AND a.AUTHOLIST_STATUS='A' AND mn.MENU_STATUS='A' " +
            "GROUP BY mn.MENU_ICON, mn.MENU_CODE, mn.MENU_NAME_TH, mn.MENU_URL, mn.MENU_GROUP";

    private static final String LIST_PERMISSION = "select  m.MENU_CODE, m.PERMISSION_CODE from m_groups_map_autholist as a  " +
            "left outer join m_menus_map_permission as m on a.GROUPS_MAP_AUTHOLIST_ID=m.MENU_MAP_PERMISSION_ID " +
            "left outer join m_menus as mn on m.MENU_CODE = mn.MENU_CODE " +
            "where a.GROUP_ID = :GROUPID and m.MENU_CODE= :MENUCODE and a.AUTHOLIST_STATUS='A' and mn.MENU_STATUS='A'";
    private static final String GET_PERMISSION = "SELECT * FROM seaman.m_groups_map_autholist auth " +
            "left outer join seaman.m_menus_map_permission permission on auth.GROUPS_MAP_AUTHOLIST_ID=permission.MENU_MAP_PERMISSION_ID " +
            "where auth.AUTHOLIST_STATUS='A' " +
            "and GROUP_ID=:GROUP_ID and permission.MENU_CODE=:MENU_CODE and permission.PERMISSION_CODE=:PERMISSION_CODE; ";
    public MenuEntity findByUsername(String username) {

        List<MenuEntity> listRecord = null;

        MenuEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME", username);
            listRecord = template.query(FIND_BY_USERNAME, namedParameters, new BeanPropertyRowMapper(MenuEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }


    public List<MenuInfo> findAll(String groupid) {
        List<MenuInfo> listAll = null;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUPID", groupid);
            listAll = template.query(LIST_MENU, namedParameters, new BeanPropertyRowMapper(MenuInfo.class));
            logger.info("Init load master position code.");
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<MenuInfo> getMenuName() {
        List<MenuInfo> listAll = null;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listAll = template.query("select * from m_menus where menu_status='A'", namedParameters, new BeanPropertyRowMapper(MenuInfo.class));
            logger.info("Init load menu name.");
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public List<MenuPermission> listAuthorized(String groupId, String menuCode) {
        List<MenuPermission> listAll = null;
//        logger.info("groupid:"+groupId);
//        logger.info("menuCode:"+menuCode);
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("MENUCODE", menuCode)
                    .addValue("GROUPID", groupId);
            listAll = template.query(LIST_PERMISSION, namedParameters, new BeanPropertyRowMapper(MenuPermission.class));
            logger.info("get menu permission code from db");
            //logger.info("listAll: "+ listAll);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }


    public MenuEntity listGroupById(String username) {

        List<MenuEntity> listRecord = null;

        MenuEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("USERNAME", username);
            listRecord = template.query(FIND_BY_USERNAME, namedParameters, new BeanPropertyRowMapper(MenuEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }





    public MenuEntity getMenuInterceptor(String groupId, String menuCode, String permissionCode) {

        List<MenuEntity> listRecord = null;

        MenuEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_ID",groupId)
                    .addValue("MENU_CODE",menuCode)
                    .addValue("PERMISSION_CODE",permissionCode);
            listRecord = template.query(GET_PERMISSION, namedParameters, new BeanPropertyRowMapper(MenuEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }

        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return result;
    }


}