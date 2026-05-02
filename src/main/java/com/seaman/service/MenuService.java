package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.PermissionCode;
import com.seaman.entity.GroupEntity;
import com.seaman.entity.MenuEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.request.MenuAuthorizedRequest;
import com.seaman.model.request.MenuRequest;
import com.seaman.model.response.MenuAuthorizedResponse;
import com.seaman.model.response.MenuInfo;
import com.seaman.model.response.MenuPermission;
import com.seaman.model.response.MenuResponse;
import com.seaman.repository.MenuRepository;
import com.seaman.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final HttpServletRequest httpServletRequest;
    private final TransactionLogsService transactionLogsService;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    private final MessageCodeService messageCodeService;

    public MenuResponse menuList(MenuRequest menuRequest) {

        MenuResponse response = new MenuResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "MENU LIST";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // -- 1. find username in table, And check password
            GroupEntity groupEntity = new GroupEntity();
            groupEntity = userRepository.getAllGroupByUsername(menuRequest.getUsername());

            Optional<MenuEntity> menuEntityOptional = Optional.ofNullable(menuRepository.findByUsername(groupEntity.getGroupId().toString()));

            if (menuEntityOptional.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
            }

            response.setRoleId(groupEntity.getGroupId().toString());
            response.setRoleName(groupEntity.getGroupName());

            List<MenuInfo> menuEntityList = new ArrayList<>();
            menuEntityList = menuRepository.findAll(groupEntity.getGroupId().toString());
          //  log.info("list menu : {} ", menuEntityList);

            // Company
            List<MenuInfo> menuInfoList = new ArrayList<>();
            for (MenuInfo item : menuEntityList) {
                MenuInfo menuInfo = new MenuInfo();
                menuInfo.setMenuIcon(item.getMenuIcon());
                menuInfo.setMenuCode(item.getMenuCode());
                menuInfo.setMenuNameTh(item.getMenuNameTh());
                menuInfo.setMenuUrl(item.getMenuUrl());
                menuInfo.setMenuGroup(item.getMenuGroup());
                menuInfoList.add(menuInfo);
            }
            response.setMenu(menuInfoList);

            log.info("List menu is success.");
        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("List menu Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public MenuAuthorizedResponse getAuthorised(MenuAuthorizedRequest menuAuthorizedRequest) {
        MenuAuthorizedResponse response = new MenuAuthorizedResponse();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET AUTHORISES";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            Optional<List<MenuPermission>> menuEntityOptional = Optional.ofNullable(menuRepository.listAuthorized(menuAuthorizedRequest.getGroupId(), menuAuthorizedRequest.getMenuCode()));

            if (menuEntityOptional.isEmpty()) {
                throw new BusinessException(AppStatus.EXCEPTION_USERNAME_PASSWORD_INCORRECT, "");
            }

            List<MenuPermission> menuPermissionEntities = menuEntityOptional.get();
            if (menuPermissionEntities.isEmpty()) {
                String descriptionFail = messageCodeService.getMessageDescription(AppStatus.PERMISSION_DENINED, (String) httpServletRequest.getAttribute(AppSys.LANGUAGE));
                throw new BusinessException(AppStatus.PERMISSION_DENINED, descriptionFail, null);
            }

            log.info("list menu Permission_db : {} ", menuPermissionEntities);
            List<MenuPermission> menuList = new ArrayList<>();

            for (MenuPermission item : menuPermissionEntities) {
                MenuPermission menu = new MenuPermission();
                log.info("getPermissionValue:" + item.getPermissionCode());
                if (PermissionCode.CREATE.equals(item.getPermissionCode())) {
                    menu.setPermissionCode(PermissionCode.CREATE);
                    menu.setPermissionValue("true");
                } else if (PermissionCode.EDIT.equals(item.getPermissionCode())) {
                    menu.setPermissionCode(PermissionCode.EDIT);
                    menu.setPermissionValue("true");
                } else if (PermissionCode.DELETE.equals(item.getPermissionCode())) {
                    menu.setPermissionCode(PermissionCode.DELETE);
                    menu.setPermissionValue("true");
                } else if (PermissionCode.DOWNLOAD.equals(item.getPermissionCode())) {
                    menu.setPermissionCode(PermissionCode.DOWNLOAD);
                    menu.setPermissionValue("true");
                } else if (PermissionCode.SEARCH.equals(item.getPermissionCode())) {
                    menu.setPermissionCode(PermissionCode.SEARCH);
                    menu.setPermissionValue("true");
                }
//                else{
//                    menu.setPermissionCode(item.getPermissionCode());
//                    menu.setPermissionValue("false");
//                }
                menuList.add(menu);
            }


//            log.info("menuList:before: {}", menuList);
            MenuPermission menu = new MenuPermission();

            if (!menuList.contains("CREATE")) {
                menu.setPermissionCode("CREATE");
                menu.setPermissionValue("false");
            }

            if (!menuList.contains("EDIT")) {
                menu.setPermissionCode("EDIT");
                menu.setPermissionValue("false");
            }

            if (!menuList.contains("DELETE")) {
                menu.setPermissionCode("DELETE");
                menu.setPermissionValue("false");
            }

            if (!menuList.contains("DOWNLOAD")) {
                menu.setPermissionCode("DOWNLOAD");
                menu.setPermissionValue("false");
            }

            if (!menuList.contains("SEARCH")) {
                menu.setPermissionCode("SEARCH");
                menu.setPermissionValue("false");
            }

            if (!menuList.contains("DOWNLOAD")) {
                menu.setPermissionCode("DOWNLOAD");
                menu.setPermissionValue("false");
            }

            menuList.add(menu);

            log.info("menuList:after:" + menuList);
            response.setPermission(menuList);
            response.setMenuCode(menuAuthorizedRequest.getMenuCode());

            log.info("Get Authorization is success.");

        } catch (CommonException ce) {
            statusCode = ce.getCode();
            throw ce;
        } catch (Exception ex) {
            log.error("Get authorization -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

}
