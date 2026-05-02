package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.*;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.LoginRequest;
import com.seaman.model.request.RegisterRequest;
import com.seaman.model.response.*;
import com.seaman.repository.*;
import com.seaman.utils.DateUtil;
import com.seaman.utils.FrameworkUtils;
import com.seaman.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Period;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;

    private final JwtTokenService jwtTokenUtil;

    private final FrameworkUtils frameworkUtils;

    private final UserRepository userRepository;

    private final SessionRepository sessionRepository;

    private final TransactionLogsService transactionLogsService;

    private final GroupRepository groupRepository;

    private final PasswordEncoder passwordEncoder;

    private final DateUtil dateUtil;

    private final CompanyRepository companyRepository;

    private final PositionRepository positionRepository;

    public LoginResponse login(LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();
        String correlationId = httpServletRequest.getHeader(AppSys.HEADER_CORRELATION_ID);

        try {
            // -- 1. find username in table, And check password
            Optional<UsersEntity> usersEntityOptional = Optional.ofNullable(userRepository.findByUsername(loginRequest.getUsername()));

//            if (usersEntityOptional.isEmpty()) {
//                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, null);
//            }
//
//            UsersEntity usersEntity = usersEntityOptional.get();
//            if (this.matchPassword(loginRequest.getUsername(), usersEntity.getPassword())) {
//                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, null);
//            }
            if (usersEntityOptional.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
            }

            UsersEntity usersEntity = usersEntityOptional.get();
            if (!this.matchPassword(loginRequest.getPassword(), usersEntity.getPassword())) {
                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
            }

            if(!"A".equals(usersEntity.getUserStatus())) {
                throw new BusinessException(AppStatus.DATA_NOT_FOUND, "");
            }

            // -- 2. Generate JWT
            // Ref : https://www.rfc-editor.org/rfc/rfc7519#section-4.1
            String clientSessionId = frameworkUtils.generateUUID();
            Map<String, Object> claims = new HashMap<>();
            claims.put(AppSys.CLAIMS_ISSUER, AppSys.APPLICATION_NAME);
            claims.put(AppSys.CLAIMS_JTI, clientSessionId);
            claims.put(AppSys.CLAIMS_SUBJECT, loginRequest.getUsername());

            // Create JWT TOKEN
            String jwtToken = jwtTokenUtil.generateToken(claims, loginRequest.getUsername());

            // Store table session
            SessionEntity sessionEntity = new SessionEntity();
            sessionEntity.setClientSessionId(clientSessionId);
            sessionEntity.setToken(jwtToken);
            sessionEntity.setDeviceModel("WEB_ADMIN"); // must use form header request.
            sessionEntity.setUserId(usersEntity.getAdminUuid());
            sessionEntity.setCreateBy(loginRequest.getUsername());
            sessionEntity.setLoginTime(new Date());
            sessionEntity.setCreateDate(new Date());
            sessionEntity.setUpdateBy(loginRequest.getUsername());
            sessionEntity.setUpdateDate(new Date());
            sessionEntity.setCorrelationId(correlationId);
            sessionEntity.setIsOnline("YES");
            sessionRepository.insert(sessionEntity);

            // Update Login LAST_LOGIN
            userRepository.updateLastLogin(usersEntity);

            // Mark response
            response.setToken(jwtToken);
            response.setRefToken(clientSessionId);
            response.setUsername(loginRequest.getUsername());
            response.setLastLoginDateTime(dateUtil.convertTime(new Date().getTime()));

            log.info("{}", "Process login is success.");
        } catch (CommonException ce){
            throw ce;
        } catch (Exception ex){
            log.error("Login Exception {}", ex.getMessage());
            throw ex;
        }

        return response;
    }

    public RegisterResponse register(RegisterRequest request){

        String statusCode  =  AppStatus.SUCCESS_CODE;
        RegisterResponse response  = new RegisterResponse();

        String userUUID = frameworkUtils.generateUUID();
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "REGISTER";
        String createBy = request.getEmail();

        try {

            // Store trans
            transactionLogsService.insert(transId, bodyReqJson, serviceName, createBy);

            Optional<UsersEntity> isEmail = Optional.ofNullable(userRepository.findByEmail(request.getEmail()));
            if (isEmail.isEmpty()) {
                UsersEntity entity = new UsersEntity();
                entity.setUsername(request.getEmail());
                entity.setEmail(request.getEmail());
                entity.setPassword(passwordEncoder.encode(request.getPassword()));
                entity.setAdminUuid(userUUID);
                entity.setFirstName(request.getFirstName());
                entity.setLastName(request.getLastName());
                entity.setMobileNumber(request.getMobileNumber());
                entity.setCompanyCode(request.getCompanyCode());
                entity.setPositions(request.getPositionCode());
                entity.setProfilePicture("/default.png");
                // entity.setSmartSeamanId(frameworkUtils.padLeftZeros(smartSeaManId, 5));
                entity.setUserStatus("A");
                entity.setCreateBy(request.getEmail());
                entity.setCreateDate(new Date());
                entity.setUpdateBy(request.getEmail());
                entity.setUpdateDate(new Date());
                userRepository.insert(entity);

                // Set Response
                response.setUsername(request.getEmail());
                response.setEmail(request.getEmail());
            } else {
                throw new BusinessException(AppStatus.EMAIL_IS_EXISTING, "");
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            statusCode = AppStatus.EXCEPTION_TECHNICAL;
            log.error("Register Exception {}", ex.getMessage());
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, request.getEmail());
        }

        return response;
    }

    private boolean matchPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public RefreshTokenResponse refreshToken() {

        String statusCode  =  AppStatus.SUCCESS_CODE;
        RefreshTokenResponse response  = new RefreshTokenResponse();

        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "REFRESH_TOKEN";
        String userId = "";

        try {

            Optional<String> opt = SecurityUtils.getCurrentUserId();
            if (opt.isEmpty()) {
                throw new BusinessException("MA00004", "Not found username in context security.");
            }

            userId = opt.get();
            UsersEntity usersEntityOptional =  userRepository.findByUsername(userId);

            if (usersEntityOptional == null) {
                throw new BusinessException("MA00004", "Not found username in context security.");
            }

            // Store trans
            transactionLogsService.insert(transId, bodyReqJson, serviceName, userId);

            // -- 2. Generate JWT
            // Ref : https://www.rfc-editor.org/rfc/rfc7519#section-4.1
            String sessionId = frameworkUtils.generateUUID();
            Map<String, Object> claims = new HashMap<>();
            claims.put(AppSys.CLAIMS_ISSUER, AppSys.APPLICATION_NAME);
            claims.put(AppSys.CLAIMS_JTI, sessionId);
            claims.put(AppSys.CLAIMS_SUBJECT, userId);

            // Create JWT TOKEN
            String jwtToken = jwtTokenUtil.generateToken(claims, userId);
            response.setToken(jwtToken);

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            statusCode = AppStatus.EXCEPTION_TECHNICAL;
            log.error("Refresh Exception {}", ex.getMessage());
            throw ex;
        } finally {
            String resJson = frameworkUtils.toObjectToJson(response);
            transactionLogsService.update(transId, resJson, statusCode, userId);
        }

        return response;

    }

    public LoginResponse getGroupId(LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();
        String correlationId = httpServletRequest.getHeader(AppSys.HEADER_CORRELATION_ID);

        try {
            // -- 1. find username in table, And check password
            Optional<UsersEntity> usersEntityOptional = Optional.ofNullable(userRepository.findByUsername(loginRequest.getUsername()));

            if (usersEntityOptional.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
            }

            UsersEntity usersEntity = usersEntityOptional.get();
            if (!this.matchPassword(loginRequest.getPassword(), usersEntity.getPassword())) {
                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
            }

            String sessionId = frameworkUtils.generateUUID();
            String clientSessionId = frameworkUtils.generateUUID();
            Map<String, Object> claims = new HashMap<>();
            claims.put(AppSys.CLAIMS_ISSUER, AppSys.APPLICATION_NAME);
            claims.put(AppSys.CLAIMS_JTI, clientSessionId);
            claims.put(AppSys.CLAIMS_SUBJECT, loginRequest.getUsername());

            response.setUsername(loginRequest.getUsername());

            log.info("{}", "Process login is success.");
        } catch (CommonException ce){
            throw ce;
        } catch (Exception ex){
            log.error("Login Exception {}", ex.getMessage());
            throw ex;
        }

        return response;
    }

    public UserAdminProfileResponse getUserAdminProfile() {

        UserAdminProfileResponse profileResponse = new UserAdminProfileResponse();

        try {

            String lang = httpServletRequest.getHeader(AppSys.HEADER_LANGUAGE);

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");

            profileResponse.setFirstName(usersEntity.getFirstName());
            profileResponse.setLastName(usersEntity.getLastName());



//            if (null != usersEntity.getDateOfBirth() && !"".equals(usersEntity.getDateOfBirth())) {
////                Date dateOfBirth = dateUtil.parseStringToDate(usersEntity.getDateOfBirth(), DateUtil.YEAR_MONTH_DATE);
////                String dateOfBirthStr =  dateUtil.formatDateToString(dateOfBirth, DateUtil.DATE_OF_BIRTH);
//                profileResponse.setDateOfBirth(usersEntity.getDateOfBirth());
//            }

//            Period age = dateUtil.calculateDisplayAge(usersEntity.getDateOfBirth());
//            profileResponse.setAge(String.valueOf(age.getYears()));

            profileResponse.setMobile(usersEntity.getMobileNumber());
            profileResponse.setEmail(usersEntity.getEmail());

            CompanyEntity companyEntity = companyRepository.findByCode(usersEntity.getCompanyCode());
            profileResponse.setCompanyCode(companyEntity.getCompanyCode());
            profileResponse.setCompanyDescription(AppSys.LANG_EN.equals(lang) ? companyEntity.getCompanyNameEn() : companyEntity.getCompanyNameTh());

            PositionsEntity positionsEntity = positionRepository.findByCode(usersEntity.getPositions());
            profileResponse.setPositionCode(positionsEntity.getPositionCode());
            profileResponse.setPositionDescription(AppSys.LANG_EN.equals(lang) ? positionsEntity.getPositionNameEn() : positionsEntity.getPositionNameTh());

            // Short name
            String var1 = usersEntity.getFirstName().substring(0, 1).toUpperCase();
            String var2 = usersEntity.getLastName().substring(0, 1).toUpperCase();
            profileResponse.setShortName(var1 + var2);

            GroupEntity groupEntity = groupRepository.findById(usersEntity.getGroupId());
            profileResponse.setRoleName(groupEntity.getGroupName());
            profileResponse.setGroupId(groupEntity.getGroupId());

        } catch (BusinessException be) {
            throw be;
        } catch (Exception ex) {
            log.error("{}", String.valueOf(ex));
        }

        return profileResponse;
    }
}
