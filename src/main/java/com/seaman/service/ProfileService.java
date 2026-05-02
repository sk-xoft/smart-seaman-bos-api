package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.CompanyEntity;
import com.seaman.entity.PositionsEntity;
import com.seaman.entity.UserMobileEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.response.ProfileResponse;
import com.seaman.repository.UserMobileRepository;
import com.seaman.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Period;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class ProfileService  {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;

    private final TransactionLogsService transactionLogsService;

    private final UserMobileRepository userMobileRepository;

    private final DateUtil dateUtil;

    private final AmazonS3 getS3;
    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.profiles.images}")
    private String pathImageProfiles;

    public ProfileResponse getProfile(String mobileUuid) {

        String lang =  httpServletRequest.getHeader(AppSys.HEADER_LANGUAGE);
        ProfileResponse profileResponse = new ProfileResponse();

        try {

            UserMobileEntity userMobileEntity  = userMobileRepository.findUserMobileByUUID(mobileUuid);

            profileResponse.setFirstName(userMobileEntity.getFirstName());
            profileResponse.setLastName(userMobileEntity.getLastName());

            if(null != userMobileEntity.getDateOfBirth() && !"".equals(userMobileEntity.getDateOfBirth())) {
                Date dateOfBirth = dateUtil.parseStringToDate(userMobileEntity.getDateOfBirth(), DateUtil.YEAR_MONTH_DATE);
                String dateOfBirthStr =  dateUtil.formatDateToString(dateOfBirth, DateUtil.DATE_OF_BIRTH);
                profileResponse.setDateOfBirth(dateOfBirthStr);

                Period age = dateUtil.calculateDisplayAge(userMobileEntity.getDateOfBirth());
                profileResponse.setAge(String.valueOf(age.getYears()));

            } else  {
                profileResponse.setDateOfBirth("");
                profileResponse.setAge("");
            }

            if(null != userMobileEntity.getMobileNumber()){
                profileResponse.setMobile(userMobileEntity.getMobileNumber());
            } else {
                profileResponse.setMobile("");
            }

            if(null != userMobileEntity.getEmail()){
                profileResponse.setEmail(userMobileEntity.getEmail());
            } else {
                profileResponse.setEmail("");
            }

            if(null != userMobileEntity.getCompanyCode()){
                profileResponse.setCompanyCode(userMobileEntity.getCompanyCode());
                profileResponse.setCompanyDescription(AppSys.LANG_EN.equals(lang) ?  userMobileEntity.getCompanyNameEn() : userMobileEntity.getCompanyNameTh());
            } else {
                profileResponse.setCompanyCode("");
                profileResponse.setCompanyDescription("");
            }

            if(null != userMobileEntity.getPositionCode()){
                profileResponse.setPositionCode(userMobileEntity.getPositionCode());
                profileResponse.setPositionDescription(AppSys.LANG_EN.equals(lang) ? userMobileEntity.getPositionNameEn() : userMobileEntity.getPositionNameTh());
            } else {
                profileResponse.setPositionCode("");
                profileResponse.setPositionDescription("");
            }

            profileResponse.setSmartSeamanId(userMobileEntity.getSmartSeamanId());

            // Short name
            profileResponse.setShortName(userMobileEntity.getDisplayName());

        } catch (BusinessException be) {
            throw  be;
        } catch (Exception ex){
            log.error("{}" , ex);
        }
        return profileResponse;
    }

    public String getProfileImage(String mobileUuid) {

        String imageBase64 = "";

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "PROFILE_PIC";
        String username = "";

        try {
            UserMobileEntity userMobileEntity  = userMobileRepository.findUserMobileByUUID(mobileUuid);
            username = userMobileEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, userMobileEntity.getUsername());

            String keyName = pathImageProfiles + "/" + userMobileEntity.getProfilePicture();
            imageBase64 = getS3.getObjectAsString(bucketName, keyName);

        } catch (CommonException ce) {
            log.error("{}", ce.getMessage());
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw ex;
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return imageBase64;
    }

}
