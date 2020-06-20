package com.atguigu.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * 解决全局跨域问题
 *
 * NOTICE: 同时使用全局跨域和局部跨域会导致两次跨域，最终客户端访问失败，所以全局跨域和局部跨域不能同时使用
 *         如果选择使用全局跨域，就必须把每个Controller上的 @CrossOrigin注解 注释掉
 *         如果选择使用局部跨越，就必须把当前类中的 corsFilter()方法 注释掉
 */
@Configuration
public class CorsConfig {

//    @Bean
//    public CorsWebFilter corsFilter() {
//        CorsConfiguration config = new CorsConfiguration();
//        config.addAllowedMethod("*");
//        config.addAllowedOrigin("*");
//        config.addAllowedHeader("*");
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
//        source.registerCorsConfiguration("/**", config);
//
//        return new CorsWebFilter(source);
//    }
}
