package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.dto.Account;
import com.example.entity.vo.response.AuthorizeVO;
import com.example.filter.JwtAuthorizeFilter;
import com.example.service.AccountService;
import com.example.service.impl.AccountServiceImpl;
import com.example.util.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {
    @Resource
    JwtUtils jwtUtils;
    @Resource
    JwtAuthorizeFilter jwtAuthorizeFilter;

    @Resource
    AccountServiceImpl accountService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/api/auth/**").permitAll();
                    auth.anyRequest().authenticated();
                })
                .formLogin(conf -> {
                    conf.loginProcessingUrl("/api/auth/login")
                        .failureHandler(this::authenFailure)
                        .successHandler(this::anthenSuccess)
                        .permitAll();
                })
                .logout(conf ->{
                    conf.logoutUrl("/api/auth/logout")
                            .logoutSuccessHandler(this::logoutSuccess);
                })
                .exceptionHandling(conf -> conf//其余各种异常处理
                        .authenticationEntryPoint(this::unAuthorized)//非认证成功下访问其他页面时，进行处理
                        .accessDeniedHandler(this::accessDenied))
                .csrf(conf -> {
                    conf.disable();   //此方法可以直接关闭全部的csrf校验，一步到位
                })
                .sessionManagement(conf -> {
                    conf.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .addFilterBefore(jwtAuthorizeFilter, UsernamePasswordAuthenticationFilter.class)//加入自定义的jwtfilter在其他filter之前
                .build();
    }

    //无权访问
    private void accessDenied(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(RestBean.forbidden(400, e.getMessage()).asJsonString());
    }

    //非认证下访问
    private void unAuthorized(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(RestBean.unauthorized(401, e.getMessage()).asJsonString());
    }

    //退出成功
    //获得header中jwt，使其失效
    private void logoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        PrintWriter printWriter= httpServletResponse.getWriter();
        String authorization = httpServletRequest.getHeader("Authorization");//获得header中jwt
        //让jwt失效
        if(jwtUtils.invalidateJwt(authorization)){
            printWriter.write(RestBean.success().asJsonString());
        } else {//这里会包含一种特殊情况：该jwt已经在黑名单中。也会返回false
            printWriter.write(RestBean.failure(400, "退出登录失败").asJsonString());
        }
    }
    //认证成功
    //为该userdetail创建jwt
    //将用户信息、jwt等信息，封装到vo返回前端
    private void anthenSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        User userDetail = (User)authentication.getPrincipal();//security自带的UserDetail
        Account account = accountService.findAccountByNameOrEmail(userDetail.getUsername());

        String jwtToken = jwtUtils.createJwt(userDetail, account.getUid(), account.getUsername());//为成功登录的user创建token并返回

        AuthorizeVO authorizeVO = new AuthorizeVO();

        authorizeVO.setUsername(account.getUsername());
        authorizeVO.setRole(account.getRole());
        authorizeVO.setToken(jwtToken);
        authorizeVO.setExpire(jwtUtils.expireTime());

        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(RestBean.success(authorizeVO).asJsonString());
    }

    //认证失败
    private void authenFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        httpServletResponse.setContentType("application/json;charset=utf-8");
        httpServletResponse.getWriter().write(RestBean.failure(401, e.getMessage()).asJsonString());
    }

}
