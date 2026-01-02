package com.blog.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Security过滤链配置
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF（使用JWT不需要CSRF保护）
                .csrf(csrf -> csrf.disable())

                // 配置CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 配置会话管理（无状态）
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 配置授权规则
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI 和 OpenAPI 文档路径（公开访问）
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 所有页面路径允许访问（前端JavaScript会检查token）
                        .requestMatchers(
                                "/",                         // 首页
                                "/login",                    // 登录页
                                "/register",                 // 注册页
                                "/article/**",               // 文章详情页
                                "/user/**",                  // 用户中心页面
                                "/admin",                    // 管理后台首页
                                "/admin/**",                 // 管理后台子页面
                                "/static/**",                // 静态资源
                                "/css/**",                   // CSS
                                "/js/**",                    // JavaScript
                                "/images/**",                // 图片
                                "/uploads/**",               // 上传文件
                                "/error"                     // 错误页面
                        ).permitAll()

                        // 公开API接口（不需要认证）
                        .requestMatchers(
                                "/api/health",               // 健康检查
                                "/api/auth/**",              // 认证接口
                                "/api/articles",             // 文章列表
                                "/api/articles/*/view",      // 文章浏览量
                                "/api/articles/*",           // 文章详情
                                "/api/articles/search",      // 文章搜索
                                "/api/comments/article/*",   // 评论列表
                                "/api/users/*",              // 用户信息
                                "/api/access-log/**"         // 访问日志
                        ).permitAll()

                        // 管理员API接口（需要ADMIN角色）
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // 其他API接口需要认证
                        .requestMatchers("/api/**").authenticated()

                        // 其他请求允许访问
                        .anyRequest().permitAll()
                )

                // 添加JWT过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8080", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
