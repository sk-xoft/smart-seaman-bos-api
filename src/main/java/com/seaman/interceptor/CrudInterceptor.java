package com.seaman.interceptor;

import com.seaman.entity.MenuEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.repository.MenuRepository;
import com.seaman.repository.UserRepository;
import com.seaman.service.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class CrudInterceptor implements HandlerInterceptor {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final JwtTokenService jwtTokenUtil;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("start validate CRUD.");
        String permissionCode = "";

        String authorization = request.getHeader("Authorization");
        String token = authorization.substring(7);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        UsersEntity usersEntity = userRepository.findByUsername(username);
        String groupId = usersEntity.getGroupId();
        String uriMethod = request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1);

        if (uriMethod.equals("add")) {
            permissionCode = "CREATE";
        } else if (uriMethod.equals("update")) {
            permissionCode = "EDIT";
        } else if (uriMethod.equals("delete")) {
            permissionCode = "DELETE";
        } else if (uriMethod.contains("list")) {
            permissionCode = "SEARCH";
        } else if (uriMethod.equals("download")) {
            permissionCode = "DOWNLOAD";
        } else {
            throw new BusinessException("WA00018", "");
        }

        String menuCode = request.getHeader("menuCode");
        log.info("menuCode:" + menuCode);

        if (menuCode == null) {
            throw new BusinessException("WA00017", "");
        }

        MenuEntity menuEntity = menuRepository.getMenuInterceptor(groupId, request.getHeader("menuCode"), permissionCode);

        log.info("countRecord:" + menuEntity);

        if (menuEntity == null) {
            throw new BusinessException("WA00014", "");
        }

        log.info("getRequestURI:" + uriMethod);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }
}
