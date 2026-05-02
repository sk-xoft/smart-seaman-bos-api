package com.seaman.filter;

import com.google.gson.Gson;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.exception.CommonException;
import com.seaman.model.response.ExceptionResponse;
import com.seaman.service.JwtTokenService;
import com.seaman.service.MessageCodeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TokenFilter extends GenericFilterBean {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final JwtTokenService jwtTokenService;

    private final MessageCodeService messageCodeService;


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException, CommonException {


        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        ExceptionResponse exceptionResponse = new ExceptionResponse();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

//        String lang = request.getHeader(AppSys.HEADER_ACCEPT_LANGUAGE);
        String lang = "TH";

        try {

            String authorization = request.getHeader("Authorization");

            if (ObjectUtils.isEmpty(authorization)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            if (!authorization.startsWith("Bearer ")) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            // Get JWT token
            String token = authorization.substring(7);
//        if(jwtTokenService.validateToken(token).equals(Boolean.FALSE)){
//            filterChain.doFilter(servletRequest, servletResponse);
//            return;
//        }

            if (!jwtTokenService.verifyToken(token)) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            String username = jwtTokenService.getUsernameFromToken(token);
            if (username == null) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            List<GrantedAuthority> authorities = new ArrayList<>();
            // authorities.add(new SimpleGrantedAuthority(role));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, "(protected)", authorities);

            SecurityContext context = SecurityContextHolder.getContext();
            context.setAuthentication(authentication);

            filterChain.doFilter(servletRequest, servletResponse);
        } catch (CommonException cx) {

            String code = cx.getCode();
            String message = messageCodeService.getMessageDescription(code, lang);

            exceptionResponse.setCode(code);
            exceptionResponse.setDescription(message);
            exceptionResponse.setData(cx.getMessage());

            Gson gson = new Gson();
            String json = gson.toJson(exceptionResponse);
            log.info("{}", json);
            response.getWriter().write(json);

        } catch (Exception ex) {
            String message = messageCodeService.getMessageDescription(AppStatus.EXCEPTION_GLOBAL, AppSys.LANG_TH);

            exceptionResponse.setCode(AppStatus.EXCEPTION_GLOBAL);
            exceptionResponse.setDescription(message);
            exceptionResponse.setData(ex.getMessage());

            Gson gson = new Gson();
            String json = gson.toJson(exceptionResponse);
            log.info("{}", json);
            response.getWriter().write(json);

        }
    }
}
