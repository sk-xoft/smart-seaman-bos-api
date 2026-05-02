package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.*;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.CourseRq;
import com.seaman.model.request.GroupRequest;
import com.seaman.model.request.GroupRoleRq;
import com.seaman.model.response.CourseList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.swing.*;
import java.util.Date;
import java.util.List;

@Repository
public class GroupRepository extends CommonRepository {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    public GroupEntity findById(String s1) {

        List<GroupEntity> listAll = null;
        GroupEntity entity = null;
        StringBuilder sql = new StringBuilder();
        sql.append("  select * from m_groups where GROUP_ID = :GROUP_ID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            namedParameters.addValue("GROUP_ID", s1);

            listAll = template.query(sql.toString(), namedParameters, new BeanPropertyRowMapper(GroupEntity.class));
            if(!listAll.isEmpty()){
                entity =  listAll.get(0);
            }

        } catch (Exception ex) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return entity;
    }

    private static final String SELECT_ALL = "select * from m_groups where group_status='A'";
    public List<GroupEntity> findAll() {
        List<GroupEntity> listAll = null;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            listAll = template.query(SELECT_ALL, namedParameters, new BeanPropertyRowMapper(GroupEntity.class));
            log.info("Init load master GROUP.");
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    private static final String INSERT_GROUP = "insert into m_groups " +
            "(GROUP_NAME, GROUP_DESC, GROUP_STATUS,CREATE_DATE,CREATE_BY) " +
            " values (:GROUP_NAME, :GROUP_DESC, :GROUP_STATUS,:CREATE_DATE,:CREATE_BY)";
    public boolean insert(GroupEntity entity) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_NAME", entity.getGroupName())
                    .addValue("GROUP_DESC", entity.getGroupDesc())
                    .addValue("GROUP_STATUS", entity.getGroupStatus())
                    .addValue("CREATE_DATE", entity.getCreateDate())
                    .addValue("CREATE_BY", entity.getCreateBy());

            int rowAffected = template.update(INSERT_GROUP, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    private static final String INSERT_GROUP_ROLE = "insert into m_groups_map_autholist " +
            "(GROUP_ID, MENU_ID, AUTHOLIST_STATUS,CREATE_DATE,CREATE_BY) " +
            " values (:GROUP_ID, :MENU_ID, :AUTHOLIST_STATUS,NOW(),:CREATE_BY)";


    public boolean insertRole(Integer groupId, Integer menuId, String status, String username) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_ID", groupId)
                    .addValue("MENU_ID", menuId)
                    .addValue("AUTHOLIST_STATUS",status)
                    .addValue("CREATE_BY", username);

            int rowAffected = template.update(INSERT_GROUP_ROLE, namedParameters);
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
            maxId = template.queryForObject("select max(group_id) from m_groups", namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxId  =  (maxId == null) ? 1 : maxId;

        return maxId;
    }

    private static final String GET_TOTAL_DATA = "SELECT count(GROUP_ID) FROM m_groups where group_status='A'";
    public Integer getTotalData() {
        Integer maxMember;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject(GET_TOTAL_DATA, namedParameters, Integer.class);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember  =  (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    private static final String DELETE_GROUP = "delete from m_groups where GROUP_ID=:GROUP_ID";
    public boolean delete(String groupId) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_ID",groupId);
            int rowAffected = template.update(DELETE_GROUP, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    private static final String DELETE_GROUP_ROLE = "delete from m_groups_map_autholist where GROUP_ID=:GROUP_ID";
    public boolean deleteRole(String groupId) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_ID",groupId);
            int rowAffected = template.update(DELETE_GROUP_ROLE, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    private static final String UPDATE_GROUP = "update m_groups set ";
    public boolean update(GroupRoleRq group, String username) {
        boolean result = false;
        try {
            String sql = UPDATE_GROUP + "";
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            if (group.getGroupName()!=null){
                namedParameters.addValue("GROUP_NAME", group.getGroupName());
                sql = sql + " GROUP_NAME=:GROUP_NAME, ";
            }
            if (group.getGroupDesc()!=null){
                namedParameters.addValue("GROUP_DESC", group.getGroupDesc());
                sql = sql + " GROUP_DESC=:GROUP_DESC, ";
            }
            if(group.getGroupName()!=null){
                namedParameters.addValue("GROUP_STATUS", group.getGroupStatus());
                sql = sql + " GROUP_STATUS=:GROUP_STATUS, ";
            }

            sql = sql + " UPDATE_DATE=:UPDATE_DATE, UPDATE_BY=:UPDATE_BY where GROUP_ID=:GROUP_ID";
            namedParameters.addValue("UPDATE_DATE",new Date());
            namedParameters.addValue("UPDATE_BY",username);
            namedParameters.addValue("GROUP_ID",group.getGroupId());
          //  log.info("SQL:"+ sql);
            int rowAffected = template.update(sql, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "groupId does not exist");
            }
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }
    private static final String LIST_GROUP_BY_ID ="SELECT GROUP_ID,GROUP_NAME, GROUP_DESC,GROUP_STATUS " +
            "FROM m_groups where GROUP_ID=:GROUP_ID";

    public GroupEntity listById(String groupId) {
        List<GroupEntity> listRecord = null;

        GroupEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_ID", groupId);

            listRecord = template.query(LIST_GROUP_BY_ID, namedParameters, new BeanPropertyRowMapper(GroupEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Group ID does not exist");
            }

           // log.info("Init list GROUP by ID ." + result);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    private static final String LIST_GROUP_BY_ID_ROLE ="SELECT * FROM m_groups_map_autholist where GROUP_ID=:GROUP_ID";
    public GroupEntity listByIdRole(String groupId) {
        List<GroupEntity> listRecord = null;

        GroupEntity result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_ID", groupId);

            listRecord = template.query(LIST_GROUP_BY_ID_ROLE, namedParameters, new BeanPropertyRowMapper(GroupEntity.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Group ID does not exist");
            }

            //log.info("Init list GROUP by ID ." + result);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean countUserUseByGroupId(String groupId) {

        try {
            String sql = "select count(*) as b  from m_admin_users where GROUP_ID = :GROUP_ID";

            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_ID", groupId);

            Integer listRecord = template.queryForObject(sql, namedParameters, Integer.class);
            if(listRecord >= 1) {
                return true;
            }

        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }

        return false;
    }
    private static final String LIST_ALL_GROUP ="SELECT * " +
            "FROM m_groups where 1>0 " ;
    public List<GroupEntity> listAllGroup(Integer start, GroupRequest request ) {
        Integer row = request.getSize();
        List<GroupEntity> listAll = null;
        String sql= LIST_ALL_GROUP;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW",row);

            if(request.getGroupName()!=null){
                namedParameters.addValue("GROUP_NAME", "%"+ request.getGroupName()+"%");
                sql = sql + " and GROUP_NAME LIKE :GROUP_NAME   ";
            }

            if(request.getGroupStatus()!=null){
                namedParameters.addValue("GROUP_STATUS",   request.getGroupStatus());
                sql = sql + " and GROUP_STATUS=:GROUP_STATUS ";
            }

            sql =  sql +  " ORDER BY GROUP_NAME " +
                    "LIMIT :START,:ROW ;";
         //   log.info("sql-list-group:" + sql);
            listAll = template.query(sql, namedParameters, new BeanPropertyRowMapper(GroupEntity.class));
         //  log.info("Init list all group." + listAll);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    private static final String LIST_ALL_GROUP_COUNT ="SELECT * " +
            "FROM m_groups where 1>0 " ;
    public List<GroupEntity> listAllGroupCount(Integer start, GroupRequest request ) {
        Integer row = request.getSize();
        List<GroupEntity> listAll = null;
        String sql= LIST_ALL_GROUP;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW",row);

            if(request.getGroupName()!=null){
                namedParameters.addValue("GROUP_NAME", "%"+ request.getGroupName()+"%");
                sql = sql + " and GROUP_NAME LIKE :GROUP_NAME   ";
            }

            if(request.getGroupStatus()!=null){
                namedParameters.addValue("GROUP_STATUS",   request.getGroupStatus());
                sql = sql + " and GROUP_STATUS=:GROUP_STATUS ";
            }

            sql =  sql +  " ORDER BY GROUP_NAME " ;
         //   log.info("sql-list-group:" + sql);
            listAll = template.query(sql, namedParameters, new BeanPropertyRowMapper(GroupEntity.class));
         //   log.info("Init list all group." + listAll);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }
    private static final String LIST_GROUP_NAME ="SELECT GROUP_ID,GROUP_NAME FROM m_groups where group_status='A' " ;
    public List<GroupEntity> listGroupName() {
        List<GroupEntity> listAll = null;
        String sql= LIST_ALL_GROUP;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();

            listAll = template.query(sql , namedParameters, new BeanPropertyRowMapper(GroupEntity.class));
         //   log.info("listAllDocument:" + listAll);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    //private static final String LIST_ROLE_NAME ="SELECT GROUPS_MAP_AUTHOLIST_ID,MENU_ID,GROUP_ID,AUTHOLIST_STATUS as menu_status FROM m_groups_map_autholist where GROUP_ID=:GROUP_ID" ;

    public List<GroupRoleEntity> listRoleName(String groupId) {
        List<GroupRoleEntity> listAll = null;

        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT m_groups.GROUP_ID, group_name,AUTHOLIST_STATUS as menu_status, m_groups_map_autholist.MENU_ID  FROM m_groups ");
        sql.append(" left join m_groups_map_autholist on m_groups.GROUP_ID=m_groups_map_autholist.GROUP_ID ");
        sql.append(" where  m_groups.GROUP_ID=:GROUP_ID ");

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("GROUP_ID",groupId);

            listAll = template.query(sql.toString() , namedParameters, new BeanPropertyRowMapper(GroupRoleEntity.class));
            //log.info("listAllDocument:" + listAll);
        } catch (Exception ex) {
            log.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

}
