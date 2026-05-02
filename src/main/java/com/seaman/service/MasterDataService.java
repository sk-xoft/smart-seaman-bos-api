package com.seaman.service;

import com.seaman.constant.AppSys;
import com.seaman.entity.*;
import com.seaman.exception.CommonException;
import com.seaman.model.response.*;
import com.seaman.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MasterDataService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final HttpServletRequest httpServletRequest;
    private final JwtTokenService jwtTokenUtil;
    private final CompanyRepository companyRepository;
    private final PositionRepository positionRepository;
    private final DocumentRepository documentRepository;
    private final SchoolRepository schoolRepository;

    private final GroupRepository groupRepository;

    private final MenuRepository menuRepository;

    public MasterDataResponse list() {

        String lang = httpServletRequest.getHeader(AppSys.HEADER_LANGUAGE);

        MasterDataResponse response = new MasterDataResponse();
        List<CompanyEntity> companyEntityList = new ArrayList<>();
        List<PositionsEntity> positionsEntityList = new ArrayList<>();

        try {

            companyEntityList = companyRepository.findAll();
            positionsEntityList = positionRepository.findAll();

            // Company
            List<CompanyResponse> companyResponseList = new ArrayList<>();
            for (CompanyEntity item : companyEntityList) {
                CompanyResponse companyResponse = new CompanyResponse();
                companyResponse.setCompanyCode(item.getCompanyCode());
                companyResponse.setCompanyName(AppSys.LANG_EN.equals(lang) ? item.getCompanyNameEn() : item.getCompanyNameTh());
                companyResponseList.add(companyResponse);
            }
            response.setCompany(companyResponseList);

            // Position
            List<PositionResponse> positionResponseList = new ArrayList<>();
            for (PositionsEntity item : positionsEntityList) {
                PositionResponse positionResponse = new PositionResponse();
                positionResponse.setPositionCode(item.getPositionCode());
                positionResponse.setPositionName(AppSys.LANG_EN.equals(lang) ? item.getPositionNameEn() : item.getPositionNameTh());
                positionResponseList.add(positionResponse);
            }
            response.setPosition(positionResponseList);

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }

        return response;
    }

    public MasterDataResponse listSchools() {
        String authorization = httpServletRequest.getHeader(AppSys.HEADER_AUTHORIZATION);
        String token = authorization.substring(7);
        String lang = httpServletRequest.getHeader(AppSys.HEADER_LANGUAGE);
        String username = jwtTokenUtil.getUsernameFromToken(token);

        CompanyEntity companyEntity = companyRepository.getCompanyCode(username);

        MasterDataResponse response = new MasterDataResponse();
        List<SchoolEntity> schoolEntityList = new ArrayList<>();

        log.info("companyType : {},  companyCode:{} ", companyEntity.getCompanyType(), companyEntity.getCompanyCode());

        try {
            schoolEntityList = schoolRepository.findAll(companyEntity.getCompanyType(), companyEntity.getCompanyCode());
            List<SchoolRs> schoolRsList = new ArrayList<>();
            for (SchoolEntity item : schoolEntityList) {
                SchoolRs schoolResponse = new SchoolRs();
                schoolResponse.setSchoolCode(item.getCompanyCode());
                schoolResponse.setSchoolName(AppSys.LANG_EN.equals(lang) ? item.getCompanyNameEn() : item.getCompanyNameTh());
                schoolRsList.add(schoolResponse);
            }
            response.setSchools(schoolRsList);
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }

        return response;
    }

    public MasterDataResponse listCompanys() {
        String authorization = httpServletRequest.getHeader(AppSys.HEADER_AUTHORIZATION);
        String token = authorization.substring(7);
        String lang = httpServletRequest.getHeader(AppSys.HEADER_LANGUAGE);
        String username = jwtTokenUtil.getUsernameFromToken(token);

        CompanyEntity companyEntity = companyRepository.getCompanyCode(username);

        MasterDataResponse response = new MasterDataResponse();
        List<SchoolEntity> schoolEntityList = new ArrayList<>();

       // log.info("companyType : {},  companyCode:{} ", companyEntity.getCompanyType(), companyEntity.getCompanyCode());

        try {
            schoolEntityList = schoolRepository.findAll(companyEntity.getCompanyType(), companyEntity.getCompanyCode());
            List<SchoolRs> schoolRsList = new ArrayList<>();
            for (SchoolEntity item : schoolEntityList) {
                SchoolRs schoolResponse = new SchoolRs();
                schoolResponse.setSchoolCode(item.getCompanyCode());
                schoolResponse.setSchoolName(AppSys.LANG_EN.equals(lang) ? item.getCompanyNameEn() : item.getCompanyNameTh());
                schoolRsList.add(schoolResponse);
            }
            response.setSchools(schoolRsList);
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }

        return response;
    }

    public MasterDataResponse getCourseName() {

        MasterDataResponse response = new MasterDataResponse();
        List<CourseNameEntity> courseNameEntities = new ArrayList<>();

        try {
            courseNameEntities = documentRepository.listCourseName();
            List<CourseRs> courseRsList = new ArrayList<>();
            for (CourseNameEntity item : courseNameEntities) {
                CourseRs courseResponse = new CourseRs();
                courseResponse.setCourseCode(item.getCourseCode());
                courseResponse.setCourseNameEn(item.getCourseNameEn());
                courseResponse.setCourseNameTh(item.getCourseNameTh());
                courseResponse.setCourseDisplayName(item.getCourseDisplayName());
                courseRsList.add(courseResponse);
            }
            response.setAllCourses(courseRsList);
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }

        return response;
    }

    public MasterDataDocumentResponse masterDataDocuments() {

        String lang = httpServletRequest.getHeader(AppSys.HEADER_LANGUAGE);

        MasterDataDocumentResponse response =  new MasterDataDocumentResponse();
        List<DocumentResponse> documentEntities =  new ArrayList<>();
        List<DocumentResponse> documentCOT =  new ArrayList<>();

        try {
                for(DocumentEntity item : documentRepository.findByType("Document")) {
                    DocumentResponse documentResponse  = new DocumentResponse();
                    documentResponse.setDocumentCode(item.getDocumentCode());
                    documentResponse.setDocumentName(AppSys.LANG_EN.equals(lang) ? item.getDocumentNameEn(): item.getDocumentNameTh());
                    documentResponse.setDocumentNameTh(item.getDocumentNameTh());
                    documentEntities.add(documentResponse);
                }
                response.setDocuments(documentEntities);

                for(DocumentEntity item : documentRepository.findByType("COT")) {
                    DocumentResponse documentResponse  = new DocumentResponse();
                    documentResponse.setDocumentCode(item.getDocumentCode());
                    documentResponse.setDocumentName(AppSys.LANG_EN.equals(lang) ? item.getDocumentNameEn(): item.getDocumentNameTh());
                    documentResponse.setDocumentNameTh(item.getDocumentNameTh());
                    documentCOT.add(documentResponse);
                }

                response.setCot(documentCOT);

            log.info("Get master data documents is success.");
        } catch (CommonException ce){
            log.error("{}",  ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw  ex;
        }

        return response;
    }

    public MasterCompanyResponse masterCompany() {
        MasterCompanyResponse response =  new MasterCompanyResponse();
        List<CompanyEntity> items =  new ArrayList<>();
        try {
            items = companyRepository.findAll();
            response.setCompany(items);
            log.info("Get master data company is success.");
        } catch (CommonException ce){
            log.error("{}",  ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw  ex;
        }
        return response;
    }

    public MasterDataResponse getGroupName() {

        MasterDataResponse response = new MasterDataResponse();
        List<GroupEntity> groupEntities = new ArrayList<>();

        try {
            groupEntities = groupRepository.listGroupName();
            List<GroupRs> rsList = new ArrayList<>();
            for (GroupEntity item : groupEntities) {
                GroupRs groupRs = new GroupRs();
                groupRs.setGroupId(item.getGroupId());
                groupRs.setGroupName(item.getGroupName());
                rsList.add(groupRs);
            }
            response.setGroups(rsList);
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }

        return response;
    }

    public MasterDataResponse getMenuName() {

        MasterDataResponse response = new MasterDataResponse();
        List<MenuInfo> entities = new ArrayList<>();

        try {
            entities = menuRepository.getMenuName();
            List<MenuInfo> rsList = new ArrayList<>();
            for (MenuInfo item : entities) {
                MenuInfo rs = new MenuInfo();
                rs.setMenuId(item.getMenuId());
                rs.setMenuNameTh(item.getMenuNameTh());

                rsList.add(rs);
            }
            response.setMenus(rsList);
        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            throw ce;
        } catch (Exception ex) {
            throw ex;
        }

        return response;
    }
}
