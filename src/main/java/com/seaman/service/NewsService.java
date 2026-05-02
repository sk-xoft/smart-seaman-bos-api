package com.seaman.service;

import com.amazonaws.services.s3.AmazonS3;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.NewsEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.NewsRq;
import com.seaman.model.response.NewsRs;
import com.seaman.repository.NewsRepository;
import com.seaman.repository.SendNotificationRepository;
import com.seaman.utils.FrameworkUtils;
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
public class NewsService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final FrameworkUtils frameworkUtils;
    private final HttpServletRequest httpServletRequest;
    private final NewsRepository newsRepository;
    private final TransactionLogsService transactionLogsService;
    private final SendNotificationService sendNotificationService;
    private final SendNotificationRepository sendNotificationRepository;
    private final AmazonS3 getS3;
    @Value("${object.store.bucket}")
    private String bucketName;
    @Value("${object.store.path.news}")
    private String pathUploadFilePicture;

    public NewsRs getAllNews(NewsRq request) {

        NewsRs response = new NewsRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET ALL NEWS";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            Integer startNum = request.getLastNum() - request.getSize();
            Integer totalRecord = newsRepository.getTotalRecord();

            if (startNum < 0) {
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, " {size} can not more than {lastNum}.");
            }

            List<NewsEntity> newsLists = newsRepository.listAllNews(startNum, request.getSize(), request);
            List<NewsEntity> newsListsCount =newsRepository.listAllNewsCount(startNum, request.getSize(), request);
            List<NewsEntity> rsLists = new ArrayList<>();
            int index = 0;
            for (NewsEntity item : newsLists) {

                NewsEntity rs = new NewsEntity();
                index++;
                rs.setNewsSeq(startNum+index);
                rs.setNewsPictureFileName(item.getNewsPictureFileName());
                rs.setNewsStatus(item.getNewsStatus());
                rs.setNewsDetails(item.getNewsDetails());
                rs.setNewsType(item.getNewsType());
                rs.setNewsTitle(item.getNewsTitle());
                rs.setNewsId(item.getNewsId());
                rs.setCreateDate(item.getCreateDate());
                rs.setCreateBy(item.getCreateBy());
                rs.setUpdateDate(item.getUpdateDate());
                rs.setUpdateBy(item.getUpdateBy());
                rsLists.add(rs);

            }

            Integer totalData = totalRecord;
            response.setSize(request.getSize());
            response.setLastNum(request.getLastNum());
            response.setTotalData(totalData);
            response.setNewsList(rsLists);
            response.setCountList(newsListsCount.size());

            log.info("List all news is success.");
        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Get all news -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public NewsRs insertNews(NewsRq request) {

        NewsRs response = new NewsRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "NEWS_CREATE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Insert table
            NewsEntity entity = new NewsEntity();
            entity.setNewsType(request.getNewsType());
            entity.setNewsTitle(request.getNewsTitle());
            entity.setNewsDetails(request.getNewsDetails());
            entity.setNewsStatus(request.getNewsStatus());
            entity.setNewsPictureFileName(request.getNewsPictureFileName());
            entity.setCreateDate(new Date());
            entity.setCreateBy(username);
            log.info("entity before insert : {} ", entity);

            String uuid = frameworkUtils.generateUUID();

            if (!"".equals(request.getNewsPictureFileName()) && null != request.getNewsPictureFileName()) {
                uuid = request.getNewsPictureFileName();
            }

            entity.setNewsPictureFileName(uuid);

            if (!"".equals(request.getNewsPictureFromFile()) || null != request.getNewsPictureFromFile()) {

                int newsId = newsRepository.insertNews(entity);

                if (newsId != 0) {
                    String keyName = pathUploadFilePicture + "/" + uuid;
                    getS3.putObject(bucketName, keyName, request.getNewsPictureFromFile());
                    log.info("put object {} is success.", keyName);
                } else {
                    log.info("Not have send file 'Picture File'.");
                }

                response.setNewsId(newsRepository.getMaxId());

                if (request.getNewsStatus().equals("A")) {
                    String notiType = AppSys.NOTI_TYPE_NEWS_GENERAL;
                    String titleMessage = "บทความทั่วไป";
                    if (request.getNewsType().equals("SHIP")) {
                        notiType = AppSys.NOTI_TYPE_NEWS_SHIP;
                        titleMessage = "ข่าวสารงานเรือ";
                    }

                    log.info("Start send noti news");
                    sendNotificationService.senderFcmNews(
                            notiType,
                            String.valueOf(newsId),
                            titleMessage,
                            request.getNewsTitle()
                    );
                }
            }
        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Insert news Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public NewsRs insertNewsManual(NewsRq request) {

        NewsRs response = new NewsRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "NEWS_CREATE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Insert table
            NewsEntity entity = new NewsEntity();
            entity.setNewsType(request.getNewsType());
            entity.setNewsTitle(request.getNewsTitle());
            entity.setNewsDetails(request.getNewsDetails());
            entity.setNewsStatus(request.getNewsStatus());
            entity.setNewsPictureFileName(request.getNewsPictureFileName());
            entity.setCreateDate(new Date());
            entity.setCreateBy(username);
            log.info("entity before insert : {} ", entity);

            String uuid = frameworkUtils.generateUUID();

            if (!"".equals(request.getNewsPictureFileName()) && null != request.getNewsPictureFileName()) {
                uuid = request.getNewsPictureFileName();
            }

            entity.setNewsPictureFileName(uuid);

            if (!"".equals(request.getNewsPictureFromFile()) || null != request.getNewsPictureFromFile()) {

                int newsId = newsRepository.insertNews(entity);

                if (newsId != 0) {
                    String keyName = pathUploadFilePicture + "/" + uuid;
                    getS3.putObject(bucketName, keyName, request.getNewsPictureFromFile());
                    log.info("put object {} is success.", keyName);
                } else {
                    log.info("Not have send file 'Picture File'.");
                }

                response.setNewsId(newsRepository.getMaxId());

                if (request.getNewsStatus().equals("A")) {
                    String notiType = AppSys.NOTI_TYPE_NEWS_GENERAL;
                    String titleMessage = "บทความทั่วไป";
                    if (request.getNewsType().equals("SHIP")) {
                        notiType = AppSys.NOTI_TYPE_NEWS_SHIP;
                        titleMessage = "ข่าวสารงานเรือ";
                    }

                    String userMobileId = request.getUserMobileId();
                    String tokenFcm = request.getTokenFcm();

                    log.info("Start send noti news");
                    sendNotificationService.senderFcmNewsManual(
                            userMobileId,
                            tokenFcm,
                            notiType,
                            String.valueOf(newsId),
                            titleMessage,
                            request.getNewsTitle()
                    );
                }
            }
        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Insert news Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String deleteNews(Integer id) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "DELETE_NEWS";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            NewsEntity entity = newsRepository.listById(String.valueOf(id));

            if (entity != null) {
                boolean isStatus = newsRepository.deleteNews(String.valueOf(id));
                if (isStatus) {
                    log.info("Is delete m_send_notification [news] value id -> {}, noti type -> {}", id, entity.getNewsType());
                    sendNotificationRepository.deleteNotiWhenNotFound(String.valueOf(id), entity.getNewsType());
                    log.info("NEWS is delete and notification is success.");
                }
            }

            log.info("Delete news is success.");

        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Delete news Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public String updateNews(NewsRq request) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "UPDATE_NEWS";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            String uuid = frameworkUtils.generateUUID();

            if (!"".equals(request.getNewsPictureFileName()) && null != request.getNewsPictureFileName()) {
                uuid = request.getNewsPictureFileName();
            }

            request.setNewsPictureFileName(uuid);

            if (newsRepository.updateNews(request, username)) {
                if (!"".equals(request.getNewsPictureFromFile()) || null != request.getNewsPictureFromFile()) {
                    String keyName = pathUploadFilePicture + "/" + uuid;
                    getS3.putObject(bucketName, keyName, request.getNewsPictureFromFile());
                    log.info("put object {} is success.", keyName);
                } else {
                    log.info("Not have send file 'Picture File'.");
                }

                if (request.getNewsStatus().equals("A")) {
                    String notiType = AppSys.NOTI_TYPE_NEWS_GENERAL;
                    String titleMessage = "บทความทั่วไป";
                    if (request.getNewsType().equals("SHIP")) {
                        notiType = AppSys.NOTI_TYPE_NEWS_SHIP;
                        titleMessage = "ข่าวสารงานเรือ";
                    }

                    log.info("Start send noti news -> update");
                    // sendNotificationService.sendNotiNews("/topics/news", notiType, request.getNewsTitle(), String.valueOf(request.getNewsId()), titleMessage);
                    sendNotificationService.senderFcmNews(
                            notiType,
                            String.valueOf(request.getNewsId()),
                            titleMessage,
                            request.getNewsTitle()
                    );

                } else {
                    log.info("Is delete m_send_notification [news] value id -> {}, noti type -> {}", request.getNewsId(), request.getNewsType());
                    sendNotificationRepository.deleteNotiWhenNotFound(String.valueOf(request.getNewsId()),request.getNewsType());
                    log.info("NEWS is update and notification is success.");
                }

            }

        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public NewsRs getNewsById(String id) {
        NewsRs response = new NewsRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET NEWS BY ID";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            NewsEntity form = new NewsEntity();
            List<NewsEntity> rsLists = new ArrayList<>();
            form = newsRepository.listById(id);

            String keyName = pathUploadFilePicture + "/" + form.getNewsPictureFileName();

            // วีธี Load file form BASE64
            String fileBase64 = getS3.getObjectAsString(bucketName, keyName);
            form.setImageBase64(fileBase64);

            rsLists.add(form);
            response.setSize(1);
            response.setLastNum(1);
            response.setTotalData(1);
            response.setNewsList(rsLists);

            log.info("Get news by id. Is success.");
        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Get news by Id ->  Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String previewNews(String id) {

        String imageBase64 = "";

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "PREVIEW  NEWS";
        String username = "";

        try {
            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            NewsEntity entity = newsRepository.listById(id);

            String keyName = pathUploadFilePicture + "/" + entity.getNewsPictureFileName();
            imageBase64 = getS3.getObjectAsString(bucketName, keyName);

            log.info("Load preview news is success.");

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
