package com.agentedu.config;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * Servlet Filter 层兜底鉴权，避免 MVC Interceptor 未覆盖时出现无 token 访问。
     */
    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude("/auth/login", "/auth/register")
                .setAuth(obj -> StpUtil.checkLogin());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> SaRouter.match("/**")
                        .notMatch("/auth/login", "/auth/register")
                        .check(r -> StpUtil.checkLogin())))
                .addPathPatterns("/**");
    }
}
