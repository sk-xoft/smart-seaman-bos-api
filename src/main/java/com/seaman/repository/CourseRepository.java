package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.CourseEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.request.CourseRq;
import com.seaman.model.response.CourseList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public class CourseRepository extends CommonRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

//    private static final String LIST_ALL_COURSE = "SELECT COURSE_ID,COURSE_CODE,COURSE_COMPANY_CODE,COURSE_ONLINE_DATE, " +
//            " COURSE_ONSITE_DATE,COURSE_TOTAL_DAYS,COURSE_COLOUR,COURSE_PRICE,COURSE_STATUS, " +
//            " c.CREATE_DATE,c.CREATE_BY,c.UPDATE_DATE,c.UPDATE_BY, " +
//            " STR_TO_DATE(c.COURSE_ONLINE_DATE, '%d/%m/%Y') as ONLINE_DATE, " +
//            " STR_TO_DATE(c.COURSE_ONSITE_DATE, '%d/%m/%Y') as ONSITE_DATE , DOCUMENT_FULL_NAME_TH as courseDocumentName, " +
//            " cp.COMPANY_CODE as courseSchoolCode , cp.COMPANY_FULL_NAME_TH as courseSchoolName, COURSE_TYPE " +
//            " FROM m_courses as c " +
//            " left outer join m_documents as d on c.COURSE_CODE = d.DOCUMENT_CODE " +
//            " left outer join m_companys as cp on c.COURSE_COMPANY_CODE = cp.COMPANY_CODE " +
//            " order by ONLINE_DATE, ONSITE_DATE LIMIT :START,:ROW ;";

    private static final String LIST_ALL_COURSE ="SELECT cou.COURSE_ID, cou.COURSE_CODE, cou.COURSE_COMPANY_CODE, CONCAT(cn.COURSE_NAME_TH, ' / ', cn.COURSE_NAME_EN) AS COURSE_NAME, " +
            " cn.COURSE_NAME_TH, cn.COURSE_NAME_EN, " +
            "  cou.COURSE_ONLINE_DATE, cou.COURSE_ONSITE_DATE, cou.COURSE_TOTAL_DAYS, cou.COURSE_COLOUR, cou.COURSE_PRICE, cou.COURSE_STATUS, " +
            "  cou.CREATE_DATE, cou.CREATE_BY, cou.UPDATE_DATE, cou.UPDATE_BY, " +
            "  IF (cou.COURSE_ONLINE_DATE IS NULL, STR_TO_DATE(cou.COURSE_ONSITE_DATE, '%d/%m/%Y'), " +
            "    STR_TO_DATE(cou.COURSE_ONLINE_DATE, '%d/%m/%Y')) AS ONLINE_DATE, " +
            "sch.COMPANY_CODE as courseSchoolCode , sch.COMPANY_FULL_NAME_TH as courseSchoolName,cou.course_type  " +
            "FROM m_courses cou " +
            "  INNER JOIN m_companys sch ON cou.COURSE_COMPANY_CODE = sch.COMPANY_CODE " +
            "  INNER JOIN m_course_name cn ON cou.COURSE_CODE = cn.COURSE_CODE " ;
    private static final String LIST_COURSE_BY_ID ="SELECT cou.COURSE_ID, cou.COURSE_CODE, cou.COURSE_COMPANY_CODE, CONCAT(cn.COURSE_NAME_TH, ' / ', cn.COURSE_NAME_EN) AS COURSE_NAME, " +
            "  cou.COURSE_ONLINE_DATE, cou.COURSE_ONSITE_DATE, cou.COURSE_TOTAL_DAYS, cou.COURSE_COLOUR, cou.COURSE_PRICE, cou.COURSE_STATUS, " +
            "  cou.CREATE_DATE, cou.CREATE_BY, cou.UPDATE_DATE, cou.UPDATE_BY, cn.COURSE_NAME_TH, cn.COURSE_NAME_EN, " +
            "  IF (cou.COURSE_ONLINE_DATE IS NULL, STR_TO_DATE(cou.COURSE_ONSITE_DATE, '%d/%m/%Y'), " +
            "    STR_TO_DATE(cou.COURSE_ONLINE_DATE, '%d/%m/%Y')) AS ONLINE_DATE, " +
            "sch.COMPANY_CODE as courseSchoolCode , sch.COMPANY_FULL_NAME_TH as courseSchoolName,cou.course_type  " +
            "FROM m_courses cou " +
            "  INNER JOIN m_companys sch ON cou.COURSE_COMPANY_CODE = sch.COMPANY_CODE " +
            "  INNER JOIN m_course_name cn ON cou.COURSE_CODE = cn.COURSE_CODE WHERE cou.COURSE_ID=:COURSE_ID" ;
    private static final String GET_TOTAL_DATA = "SELECT count(COURSE_ID) FROM m_courses where course_status='A'";

    private static final String INSERT_COURSE =  "insert into m_courses " +
            "(COURSE_CODE, COURSE_COMPANY_CODE, COURSE_TYPE, COURSE_ONLINE_DATE, " +
            " COURSE_ONSITE_DATE, COURSE_TOTAL_DAYS, COURSE_COLOUR, COURSE_PRICE, COURSE_STATUS,CREATE_DATE, CREATE_BY) " +
            " values (:COURSE_CODE, :COURSE_COMPANY_CODE, :COURSE_TYPE, :COURSE_ONLINE_DATE, " +
            " :COURSE_ONSITE_DATE, :COURSE_TOTAL_DAYS, :COURSE_COLOUR, :COURSE_PRICE, :COURSE_STATUS, :CREATE_DATE, :CREATE_BY) ";
    private static final String DELETE_COURSE = "delete from m_courses where COURSE_ID=:COURSE_ID";
    private static final String SELECT_MAX = "select max(course_id) from m_courses";

    private static final String UPDATE_COURSE = "update m_courses set ";

    public List<CourseList> listAllCourse(Integer start, Integer row ,String courseCode, String schoolCode) {
        List<CourseList> listAll = null;
        String sql= LIST_ALL_COURSE;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW",row);
            if (courseCode !=null){
                namedParameters.addValue("COURSE_CODE",courseCode);

            }
            if (schoolCode !=null){
                namedParameters.addValue("COURSE_COMPANY_CODE", schoolCode);

            }

            if (courseCode !=null & schoolCode!=null){
                sql = sql + " where cou.COURSE_CODE=:COURSE_CODE and cou.COURSE_COMPANY_CODE=:COURSE_COMPANY_CODE  ";
            }else if(courseCode!=null & schoolCode==null){
                sql = sql + " where cou.COURSE_CODE=:COURSE_CODE ";
            }else if (schoolCode!=null & courseCode ==null  ){
               sql = sql + " where cou.COURSE_COMPANY_CODE=:COURSE_COMPANY_CODE  ";
            }

            sql =  sql +  " ORDER BY ONLINE_DATE, sch.COMPANY_SEQ " +
                    "LIMIT :START,:ROW ;";
         //   logger.info("sql-list-course:" + sql);
            listAll = template.query(sql, namedParameters, new BeanPropertyRowMapper(CourseList.class));
           // logger.info("Init list all Course." + listAll);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }
    private static final String LIST_ALL_COURSE_COUNT ="SELECT cou.COURSE_ID, cou.COURSE_CODE, cou.COURSE_COMPANY_CODE, CONCAT(cn.COURSE_NAME_TH, ' / ', cn.COURSE_NAME_EN) AS COURSE_NAME, " +
            " cn.COURSE_NAME_TH, cn.COURSE_NAME_EN, " +
            "  cou.COURSE_ONLINE_DATE, cou.COURSE_ONSITE_DATE, cou.COURSE_TOTAL_DAYS, cou.COURSE_COLOUR, cou.COURSE_PRICE, cou.COURSE_STATUS, " +
            "  cou.CREATE_DATE, cou.CREATE_BY, cou.UPDATE_DATE, cou.UPDATE_BY, " +
            "  IF (cou.COURSE_ONLINE_DATE IS NULL, STR_TO_DATE(cou.COURSE_ONSITE_DATE, '%d/%m/%Y'), " +
            "    STR_TO_DATE(cou.COURSE_ONLINE_DATE, '%d/%m/%Y')) AS ONLINE_DATE, " +
            "sch.COMPANY_CODE as courseSchoolCode , sch.COMPANY_FULL_NAME_TH as courseSchoolName,cou.course_type  " +
            "FROM m_courses cou " +
            "  INNER JOIN m_companys sch ON cou.COURSE_COMPANY_CODE = sch.COMPANY_CODE " +
            "  INNER JOIN m_course_name cn ON cou.COURSE_CODE = cn.COURSE_CODE " ;
    public List<CourseList> listAllCourseCount(Integer start, Integer row ,String courseCode, String schoolCode) {
        List<CourseList> listAll = null;
        String sql= LIST_ALL_COURSE;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("START", start)
                    .addValue("ROW",row);
            if (courseCode !=null){
                namedParameters.addValue("COURSE_CODE",courseCode);

            }
            if (schoolCode !=null){
                namedParameters.addValue("COURSE_COMPANY_CODE", schoolCode);

            }

            if (courseCode !=null & schoolCode!=null){
                sql = sql + " where cou.COURSE_CODE=:COURSE_CODE and cou.COURSE_COMPANY_CODE=:COURSE_COMPANY_CODE  ";
            }else if(courseCode!=null & schoolCode==null){
                sql = sql + " where cou.COURSE_CODE=:COURSE_CODE ";
            }else if (schoolCode!=null & courseCode ==null  ){
                sql = sql + " where cou.COURSE_COMPANY_CODE=:COURSE_COMPANY_CODE  ";
            }

            sql =  sql +  " ORDER BY ONLINE_DATE, sch.COMPANY_SEQ; " ;
           // logger.info("sql-list-course:" + sql);
            listAll = template.query(sql, namedParameters, new BeanPropertyRowMapper(CourseList.class));
           // logger.info("Init list all Course." + listAll);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return listAll;
    }

    public CourseList listCourseById(String courseId) {
        List<CourseList> listRecord = null;

        CourseList result = null;

        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("COURSE_ID", courseId);

            listRecord = template.query(LIST_COURSE_BY_ID, namedParameters, new BeanPropertyRowMapper(CourseList.class));
            if (listRecord != null && !listRecord.isEmpty()) {
                result = listRecord.get(0);
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "Course ID does not exist");
            }


            //logger.info("Init list list Course by ID ." + result);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
            return result;
    }
    public Integer getTotalData() {
        Integer maxMember;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxMember = template.queryForObject(GET_TOTAL_DATA, namedParameters, Integer.class);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxMember  =  (maxMember == null) ? 1 : maxMember;

        return maxMember;
    }

    public Integer getMaxId() {
        Integer maxId;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
            maxId = template.queryForObject(SELECT_MAX, namedParameters, Integer.class);
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        maxId  =  (maxId == null) ? 1 : maxId;

        return maxId;
    }

    public boolean insert(CourseEntity entity) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("COURSE_CODE", entity.getCourseDocumentCode())
                    .addValue("COURSE_COMPANY_CODE", entity.getCourseCompanyCode())
                    .addValue("COURSE_TYPE", entity.getCourseType())
                    .addValue("COURSE_ONLINE_DATE", entity.getCourseOnlineDate())
                    .addValue("COURSE_ONSITE_DATE", entity.getCourseOnsiteDate())
                    .addValue("COURSE_TOTAL_DAYS", entity.getCourseTotalDays())
                    .addValue("COURSE_COLOUR", entity.getCourseColour())
                    .addValue("COURSE_PRICE", entity.getCoursePrise())
                    .addValue("COURSE_STATUS", entity.getCourseStatus())
                    .addValue("CREATE_DATE", entity.getCreateDate())
                    .addValue("CREATE_BY", entity.getCreateBy());
            int rowAffected = template.update(INSERT_COURSE, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean delete(String courseId) {
        boolean result = false;
        try {
            MapSqlParameterSource namedParameters = new MapSqlParameterSource()
                    .addValue("COURSE_ID",courseId);
            int rowAffected = template.update(DELETE_COURSE, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

    public boolean update(CourseRq course, String username) {
        boolean result = false;
        try {
            String sql = UPDATE_COURSE + "";
            MapSqlParameterSource namedParameters = new MapSqlParameterSource();
                if (course.getCourseCode()!=null){
                    namedParameters.addValue("COURSE_CODE", course.getCourseCode());
                    sql = sql + " COURSE_CODE=:COURSE_CODE, ";
                }
                if (course.getCourseSchoolCode()!=null){
                    namedParameters.addValue("COURSE_COMPANY_CODE", course.getCourseSchoolCode());
                    sql = sql + " COURSE_COMPANY_CODE=:COURSE_COMPANY_CODE, ";
                }
                if(course.getCourseType()!=null){
                    namedParameters.addValue("COURSE_TYPE", course.getCourseType());
                    sql = sql + " COURSE_TYPE=:COURSE_TYPE, ";
                }
                if(course.getCourseOnlineDate()!=null){
                    namedParameters.addValue("COURSE_ONLINE_DATE", course.getCourseOnlineDate());
                    sql = sql + " COURSE_ONLINE_DATE=:COURSE_ONLINE_DATE, ";
                }
                if(course.getCourseOnsiteDate()!=null){
                    namedParameters.addValue("COURSE_ONSITE_DATE", course.getCourseOnsiteDate());
                    sql = sql + " COURSE_ONSITE_DATE=:COURSE_ONSITE_DATE, ";
                }
                if(course.getCourseTotalDays()!=null){
                    namedParameters.addValue("COURSE_TOTAL_DAYS", course.getCourseTotalDays());
                    sql = sql + " COURSE_TOTAL_DAYS=:COURSE_TOTAL_DAYS, ";
                }
                if(course.getCourseColour()!=null){
                    namedParameters.addValue("COURSE_COLOUR", course.getCourseColour());
                    sql = sql + " COURSE_COLOUR=:COURSE_COLOUR, ";
                }
                if(course.getCoursePrice()!=null){
                    namedParameters.addValue("COURSE_PRICE", course.getCoursePrice());
                    sql = sql + " COURSE_PRICE=:COURSE_PRICE, ";
                }
                if(course.getCourseStatus()!=null){
                    namedParameters.addValue("COURSE_STATUS", course.getCourseStatus());
                    sql = sql + " COURSE_STATUS=:COURSE_STATUS, ";
                }
                sql = sql + " UPDATE_DATE=:UPDATE_DATE, UPDATE_BY=:UPDATE_BY where COURSE_ID=:COURSE_ID";
                namedParameters.addValue("UPDATE_DATE",new Date());
                namedParameters.addValue("UPDATE_BY",username);
                namedParameters.addValue("COURSE_ID",course.getCourseId());
                //logger.info("SQL:"+ sql);
            int rowAffected = template.update(sql, namedParameters);
            if (rowAffected > 0) {
                result = true;
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "courseId does not exist");
            }
        } catch (Exception ex) {
            logger.error("{}", ex.getMessage());
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, ex.getMessage());
        }
        return result;
    }

}
