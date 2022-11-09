package com.yhl.gj.config;

import com.yhl.gj.interceptor.JWTInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 配置 跨域设置 配置
 */
@Configuration
public class ResourceConfig implements WebMvcConfigurer {


//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        //注册拦截器JWTInterceptor，对用户名密码登入进行权限验证
//        registry.addInterceptor(new JWTInterceptor())
//                //指定拦截器注册拦截器JWTInterceptor要拦截的请求(支持*通配符)
//                .addPathPatterns("/**")
//                //指定拦截器JWTInterceptor不拦截的请求(支持*通配符)
//                .excludePathPatterns("/sys/userLogin","sys/loginOut");
//    }

    /**
     * 配置跨域
     *
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry
                // 允许访问的接口地址
                .addMapping("/**")
                //允许发起跨域访问的域名
                .allowedOriginPatterns("*")
                // 允许的请求头，默认允许所有的请求头
                .allowedHeaders("*")
                // 允许的方法，默认允许GET、POST、HEAD
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                // 是否带上cookie信息
                .allowCredentials(true)
                // 探测请求有效时间，单位秒
                .maxAge(3600);
    }

    // 配置byteArray消息转换器在json转换器之前，解决下载文件损坏问题
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0,new ByteArrayHttpMessageConverter());
    }

    /*

    @Value("${paramDirConfig.paramLEAP_Path}")
    private String paramLEAP_Path;
    @Value("${paramDirConfig.paramEOP_Path}")
    private String paramEOP_Path;
    @Value("${paramDirConfig.paramSWD_Path}")
    private String paramSWD_Path;
    @Value("${paramDirConfig.paramERR_Path}")
    private String paramERR_Path;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/param/LEAP/**").addResourceLocations("file:"+paramLEAP_Path);
        registry.addResourceHandler("/param/EOP/**").addResourceLocations("file:"+paramEOP_Path);
        registry.addResourceHandler("/param/SWT/**").addResourceLocations("file:"+paramSWD_Path);
        registry.addResourceHandler("/param/ERR/**").addResourceLocations("file:"+paramERR_Path);
    }
    */

}
