package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.entity.GroupEntity;
import com.seaman.entity.GroupRoleEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.exception.CommonException;
import com.seaman.model.common.ErrorMessage;
import com.seaman.model.request.GroupRequest;
import com.seaman.model.request.GroupRoleRq;
import com.seaman.model.response.GroupRs;
import com.seaman.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final HttpServletRequest httpServletRequest;
    private final GroupRepository groupRepository;
    private  final TransactionLogsService transactionLogsService;

    public GroupRs createGroup(GroupRequest request) {

        GroupRs response = new GroupRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CREATE GROUP";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // Insert table
            GroupEntity entity  = new GroupEntity();
            entity.setGroupStatus(request.getGroupStatus());
            entity.setGroupName(request.getGroupName());
            entity.setGroupDesc(request.getGroupDescription());

            entity.setCreateDate(new Date());
            entity.setCreateBy(username);
            if(groupRepository.insert(entity)) {
                response.setGroupId(groupRepository.getMaxId());
            }

            log.info("Insert group is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Create group Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }
    public GroupRs createGroupRole(GroupRoleRq request) {

        GroupRs response = new GroupRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "CREATE GROUP ROLE";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            List<GroupRoleEntity> listsRequest = request.getMenuAuthorlist();

            Integer num = 0;
            for(GroupRoleEntity item :listsRequest){
                groupRepository.insertRole(request.getGroupId(),item.getMenuId(),item.getMenuStatus(),username);
                ++num;
            }
            response.setTotalData(num);

            log.info("Insert group is success.");
        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Create group Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public String deleteGroup(String groupId) {

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "DELETE_GROUP";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            // check user is has this group id.
            boolean hasUserByGroupId = groupRepository.countUserUseByGroupId(groupId);

            if(hasUserByGroupId){
                GroupEntity entity = groupRepository.findById(groupId);
                log.error("User is has use group id.");
                ErrorMessage errorMessage = new ErrorMessage();
                errorMessage.setCharReplace("{group_name}");
                errorMessage.setValReplace(entity.getGroupName());
                throw new BusinessException(AppStatus.USER_IS_USE_GROUP_BY_ID, errorMessage);
            }

            if(groupRepository.listByIdRole(groupId)!=null){
                groupRepository.deleteRole(groupId);
            }

            if(groupRepository.listById(groupId)!=null){
                groupRepository.delete(groupId);
            }

            log.info("Delete group is success.");

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Delete group Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public String updateGroup(GroupRoleRq request) {
        GroupRs response = new GroupRs();
        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "UPDATE_GROUP";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            groupRepository.update(request,username);
            if(groupRepository.listByIdRole(request.getGroupId().toString())!=null){
                groupRepository.deleteRole(request.getGroupId().toString());
            }

            List<GroupRoleEntity> listsRequest = request.getMenuAuthorlist();

            Integer num = 0;
            for(GroupRoleEntity item :listsRequest){
                groupRepository.insertRole(request.getGroupId(),item.getMenuId(),item.getMenuStatus(),username);
                ++num;
            }
            response.setTotalData(num);

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Update Group -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return "success";
    }

    public GroupRs getAllGroup(GroupRequest groupRequest) {
        GroupRs response = new GroupRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET ALL GROUP";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            Integer lastNum = groupRequest.getLastNum() - groupRequest.getSize();

            if (lastNum < 0){
                throw new BusinessException(AppStatus.EXCEPTION_GLOBAL," {size} can not more than {lastNum}.");
            }
            List<GroupEntity> groupList = groupRepository.listAllGroup(lastNum,groupRequest);
            List<GroupEntity> groupListCount = groupRepository.listAllGroupCount(lastNum,groupRequest);
         //   log.info("last num : {}. ", lastNum );

            Integer num = 0;

            List<GroupEntity> groupLists = new ArrayList<>();
            for(GroupEntity item :groupList){
                ++num;
                GroupEntity group = new GroupEntity();
                group.setGroupNum(lastNum+num);
                group.setGroupId(item.getGroupId());
                group.setGroupDesc(item.getGroupDesc());
                group.setGroupName(item.getGroupName());
                group.setGroupStatus(item.getGroupStatus());
                group.setCreateDate(item.getCreateDate());
                group.setUpdateDate(item.getUpdateDate());
                groupLists.add(group);
            }

            Integer totalData = groupRepository.getTotalData();
            response.setSize(groupRequest.getSize());
            response.setLastNum(groupRequest.getLastNum());
            response.setTotalData(totalData);
            response.setGroupList(groupLists);
            response.setCountList(groupListCount.size());

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get all group Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

    public GroupRs getGroupById(String groupId) {
        GroupRs response = new GroupRs();

        String statusCode = AppStatus.SUCCESS_CODE;
        String transId = (String) httpServletRequest.getAttribute(AppSys.TRACE_ID);
        String bodyReqJson = (String) httpServletRequest.getAttribute(AppSys.REQUEST_BODY);
        String serviceName = "GET GROUP BY ID";
        String username = "";

        try {

            UsersEntity usersEntity = (UsersEntity) httpServletRequest.getAttribute("userObject");
            username = usersEntity.getUsername();

            // Insert Trans logs.
            transactionLogsService.insert(transId, bodyReqJson, serviceName, usersEntity.getUsername());

            GroupEntity group = groupRepository.listById(groupId);
            List<GroupEntity> groupLists = new ArrayList<>();

            List<GroupRoleEntity> roles = groupRepository.listRoleName(groupId);

            group.setGroupNum(1);
            groupLists.add(group);
            response.setSize(1);
            response.setLastNum(1);
            response.setTotalData(1);
            response.setGroupList(groupLists);
            response.setMenuAuthorlist(roles);

            log.info("Get group by id. Is success");

        } catch (CommonException ce){
            statusCode = ce.getCode();
            throw  ce;
        } catch(Exception ex){
            log.error("Get group by id -> Exception {}", ex.getMessage());
            statusCode = AppStatus.EXCEPTION_GLOBAL;
            throw new BusinessException(AppStatus.EXCEPTION_GLOBAL, ex.getMessage());
        } finally {
            String resJson = "{}"; // Not print log in table.
            transactionLogsService.update(transId, resJson, statusCode, username);
        }

        return response;
    }

}
