package com.agentedu.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 在 Servlet Filter 层兜底鉴权，避免 MVC Interceptor 未覆盖时出现无 token 访问。
     */
    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude("/auth/login", "/auth/register")
                .setAuth(obj -> {
                    if ("OPTIONS".equalsIgnoreCase(SaHolder.getRequest().getMethod())) {
                        return;
                    }
                    StpUtil.checkLogin();
                })
                .setError(this::buildAuthErrorResponse);
    }

    /**
     * 前端与后端本地端口不同，浏览器会先发送 OPTIONS 预检请求。
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /**
     * MVC 层继续保留鉴权，确保业务 Controller 均需要登录后访问。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/**")
                        .notMatch("/auth/login", "/auth/register")
                        .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**");
    }

    private String buildAuthErrorResponse(Throwable throwable) {
        int status = throwable instanceof NotPermissionException ? 403 : 401;
        String message;
        if (throwable instanceof NotPermissionException) {
            message = "无权限访问该资源";
        } else if (throwable instanceof NotLoginException) {
            message = "未登录或登录状态已失效，请重新登录";
        } else {
            message = "认证失败，请重新登录";
        }
        SaHolder.getResponse()
                .setStatus(status)
                .setHeader("Content-Type", "application/json;charset=UTF-8");
        return "{\"code\":" + status + ",\"message\":\"" + message + "\",\"data\":null}";
    }
}
