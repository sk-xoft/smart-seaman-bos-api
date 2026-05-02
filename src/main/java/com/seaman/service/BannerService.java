package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.google.gson.Gson;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.BannerEntity;
import com.seaman.entity.FormEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.BannerRq;
import com.seaman.model.request.FormRq;
import com.seaman.model.response.BannerRs;
import com.seaman.model.response.FormRs;
import com.seaman.repository.BannerRepository;
import com.seaman.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;
    private final BannerRepository bannerRepository;
    private  final TransactionLogsService transactionLogsService;
    private final JwtTokenService jwtTokenUtil;
    @Value("${object.store.bucket}")
    private String bucketName;

    @Value("${object.store.path.banner}")
    private String pathBanner;

    private final AmazonS3 getS3;

    public BannerRs getAllBanner(BannerRq request) {
        BannerRs response = new BannerRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET ALL BANNER";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            Integer startNum = request.getLastNum() - request.getSize();

            if (startNum < 0){
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL," {size} can not more than {lastNum}.");
            }
            List<BannerEntity> formLists = new ArrayList<>();
            formLists = bannerRepository.listAllBanner(startNum,request.getSize());
            log.info("startNum:"+startNum );

            Integer num = 0;

            List<BannerEntity> formRsList = new ArrayList<>();

            for(BannerEntity item :formLists){
                BannerEntity form = new BannerEntity();
                ++num;
                form.setBannerNum(startNum+num);
                form.setBannerId(item.getBannerId());
                form.setBannerName(item.getBannerName());
                form.setBannerSeq(item.getBannerSeq());
                form.setCreateDate(item.getCreateDate());
                form.setCreateBy(item.getCreateBy());
                form.setUpdateDate(item.getUpdateDate());
                form.setUpdateBy(item.getUpdateBy());
                formRsList.add(form);
            }

            Integer totalData = bannerRepository.getTotalRecord();
            response.setSize(request.getSize());
            response.setLastNum(request.getLastNum());
            response.setTotalData(totalData);
            response.setBannerList(formRsList);
            response.setCountList(formRsList.size());

            log.info("Get all banner");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get banner -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;

    }

    public BannerRs insertBanner(BannerRq request) {

        BannerRs response = new BannerRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "INSERT BANNER";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Insert table
            BannerEntity entity  = new BannerEntity();
            entity.setBannerName(request.getBannerName());
            entity.setBannerFileName(request.getBannerFileName());
            entity.setBannerSeq(request.getBannerSeq());
            entity.setCreateDate(new Date());
            entity.setCreateBy(username);
            log.info("entity before insert : {} ", entity );

            if(!bannerRepository.listBannerByFileName(request.getBannerFileName())){
                if(bannerRepository.insertBanner(entity)) {
                    if(!"".equals(request.getBannerFromFile()) || null != request.getBannerFromFile()) {
                        String keyName =  pathBanner + "/" + request.getBannerFileName();
                        getS3.putObject(bucketName, keyName, request.getBannerFromFile());
                        log.info("put object {} is success.", keyName);
                    } else {
                        log.info("Not have send file 'Banner'.");
                    }
                    response.setBannerId(bannerRepository.getMaxId());
                }
            }else{
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL," Banner name already exist. ");
            }

            log.info("Insert Banner is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Document create Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String deleteBanner(Integer bannerId) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "DELETE_BANNER";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            transactionLogsService.insert(transId, bodyReqJson, serviceName,username);
            if(bannerRepository.listById(bannerId.toString())!=null){
                log.info("Delete banner is success.");
                bannerRepository.deleteBanner(bannerId.toString());
            }

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Delete banner -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public String previewBanner(String id) {

        String imageBase64 = "";

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "PREVIEW  BANNER";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            BannerEntity entity = bannerRepository.listById(id);

            String keyName = pathBanner + "/" + entity.getBannerFileName();
            imageBase64 = getS3.getObjectAsString(bucketName, keyName);

            log.info("Load preview is success.");
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
